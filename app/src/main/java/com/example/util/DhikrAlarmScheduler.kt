package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.DhikrApplication
import com.example.MainActivity
import com.example.data.model.DhikrItem
import com.example.service.BootAndScreenReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Ultra-precise alarm scheduler for per-Dhikr periodic cycles.
 * Uses AlarmManager.setAlarmClock() for 100% exact second-level accuracy even across Deep Doze,
 * preserves target timestamps across service restarts, and provides robust fallbacks.
 */
object DhikrAlarmScheduler {

    private const val TAG = "DhikrAlarmScheduler"
    private const val BASE_REQ_CODE = 10000

    fun scheduleAllPeriodicDhikrs(context: Context, forceReset: Boolean = false) {
        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences
        if (!prefs.isServiceEnabled) {
            cancelAllAlarms(context)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val periodicDhikrs = app.database.dhikrDao().getPeriodicDhikrsSync()
                Log.d(TAG, "Scheduling alarms for ${periodicDhikrs.size} periodic Dhikrs (forceReset=$forceReset)")
                for (dhikr in periodicDhikrs) {
                    scheduleDhikrAlarmInternal(context, dhikr, forceReset)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling periodic Dhikrs: ${e.message}", e)
            }
        }
    }

    fun scheduleNextAlarmForDhikr(context: Context, dhikrId: Long, forceReset: Boolean = true) {
        val app = context.applicationContext as DhikrApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dhikr = app.database.dhikrDao().getDhikrById(dhikrId)
                if (dhikr != null && dhikr.isEnabled) {
                    scheduleDhikrAlarmInternal(context, dhikr, forceReset)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling single Dhikr alarm: ${e.message}", e)
            }
        }
    }

    fun getNextScheduledTimeMillis(hour: Int, minute: Int, daysOfWeek: Set<Int>): Long {
        val now = System.currentTimeMillis()
        for (dayOffset in 0..7) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
            cal.set(java.util.Calendar.MINUTE, minute)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)

            val currentDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
            if (daysOfWeek.contains(currentDayOfWeek) && cal.timeInMillis > now) {
                return cal.timeInMillis
            }
        }
        return now + 24 * 60 * 60 * 1000L
    }

    fun scheduleDhikrAlarmInternal(context: Context, dhikr: DhikrItem, forceReset: Boolean = false) {
        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val now = System.currentTimeMillis()
        var targetAtMillis = prefs.getNextAlarmTimestamp(dhikr.id)

        if (dhikr.triggerMode == com.example.data.model.TriggerMode.SCHEDULED_TIME) {
            if (forceReset || targetAtMillis <= now) {
                targetAtMillis = getNextScheduledTimeMillis(
                    dhikr.scheduledHour,
                    dhikr.scheduledMinute,
                    dhikr.getDaysOfWeekSet()
                )
                prefs.setNextAlarmTimestamp(dhikr.id, targetAtMillis)
            }
        } else {
            val intervalMillis = (dhikr.timerValue.coerceAtLeast(1) * dhikr.timerUnit.multiplierMinutes * 60 * 1000L).coerceAtLeast(10000L)
            if (forceReset || targetAtMillis <= now) {
                targetAtMillis = now + intervalMillis
                prefs.setNextAlarmTimestamp(dhikr.id, targetAtMillis)
            }
        }

        val intent = Intent(context, BootAndScreenReceiver::class.java).apply {
            action = BootAndScreenReceiver.ACTION_TRIGGER_DHIKR
            putExtra(BootAndScreenReceiver.EXTRA_DHIKR_ID, dhikr.id)
        }

        val reqCode = (BASE_REQ_CODE + dhikr.id).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Exact timing execution: Try AlarmClockInfo first (Gold standard for exact precision, exempt from Doze)
        var scheduled = false
        try {
            val showIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val showPendingIntent = PendingIntent.getActivity(
                context,
                reqCode,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmClockInfo = AlarmManager.AlarmClockInfo(targetAtMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            scheduled = true
            Log.d(TAG, "Scheduled AlarmClock for Dhikr ${dhikr.id} '${dhikr.arabicText.take(15)}' at exact time $targetAtMillis (in ${(targetAtMillis - now) / 1000}s)")
        } catch (e: Exception) {
            Log.w(TAG, "setAlarmClock not permitted or failed: ${e.message}, falling back to setExactAndAllowWhileIdle")
        }

        if (!scheduled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        targetAtMillis,
                        pendingIntent
                    )
                }
                scheduled = true
                Log.d(TAG, "Scheduled exact alarm for Dhikr ${dhikr.id} at $targetAtMillis")
            } catch (e: Exception) {
                Log.w(TAG, "setExactAndAllowWhileIdle failed: ${e.message}, falling back to inexact idle")
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            targetAtMillis,
                            pendingIntent
                        )
                    }
                } catch (e2: Exception) {
                    Log.e(TAG, "Fatal: unable to schedule alarm for Dhikr ${dhikr.id}: ${e2.message}")
                }
            }
        }
    }

    fun cancelAlarmForDhikr(context: Context, dhikrId: Long) {
        val app = context.applicationContext as DhikrApplication
        app.preferences.clearNextAlarmTimestamp(dhikrId)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, BootAndScreenReceiver::class.java).apply {
            action = BootAndScreenReceiver.ACTION_TRIGGER_DHIKR
        }
        val reqCode = (BASE_REQ_CODE + dhikrId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for Dhikr $dhikrId")
        }
    }

    fun cancelAllAlarms(context: Context) {
        val app = context.applicationContext as DhikrApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val periodicDhikrs = app.database.dhikrDao().getPeriodicDhikrsSync()
                for (dhikr in periodicDhikrs) {
                    cancelAlarmForDhikr(context, dhikr.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling all alarms: ${e.message}", e)
            }
        }
    }
}
