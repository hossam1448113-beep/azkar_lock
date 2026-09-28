package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.DhikrApplication
import com.example.MainActivity
import com.example.R
import com.example.ui.overlay.DhikrLockActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DhikrForegroundService : Service() {

    companion object {
        const val TAG = "DhikrForegroundService"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, DhikrForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DhikrForegroundService::class.java)
            context.stopService(intent)
        }

        fun scheduleNextAlarm(context: Context) {
            com.example.util.DhikrAlarmScheduler.scheduleAllPeriodicDhikrs(context, forceReset = false)
        }

        fun cancelAlarm(context: Context) {
            com.example.util.DhikrAlarmScheduler.cancelAllAlarms(context)
        }
    }

    private var screenReceiver: BroadcastReceiver? = null
    private val serviceJob = kotlinx.coroutines.SupervisorJob()
    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + serviceJob)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Foreground Service onCreate")
        startForegroundWithNotification()
        registerScreenStateReceiver()
        scheduleNextAlarm(this)
        startPeriodicFailsafeLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Foreground Service onStartCommand")
        startForegroundWithNotification()
        scheduleNextAlarm(this)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Foreground Service onDestroy")
        unregisterScreenStateReceiver()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun startPeriodicFailsafeLoop() {
        serviceScope.launch {
            while (isActive) {
                delay(15000L) // 15-second heartbeat check for extreme precision and session resilience
                val app = applicationContext as? DhikrApplication ?: continue
                val prefs = app.preferences
                if (!prefs.isServiceEnabled || prefs.isAppPaused()) continue

                // Check active session persistence: if session is active and screen is interactive (ON), reassert if lost
                val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                if (prefs.hasActiveSession && !DhikrLockActivity.isActivityVisible && powerManager?.isInteractive == true) {
                    Log.d(TAG, "Active session detected in background while screen is ON, reasserting lock screen")
                    com.example.util.DhikrTriggerManager.triggerOnUnlock(this@DhikrForegroundService)
                }

                // Precision Watchdog: Guarantee time cycle triggers at exact specified time
                val now = System.currentTimeMillis()
                try {
                    val periodicDhikrs = app.database.dhikrDao().getPeriodicDhikrsSync()
                    for (dhikr in periodicDhikrs) {
                        val nextAlarm = prefs.getNextAlarmTimestamp(dhikr.id)
                        if (nextAlarm in 1..now) {
                            Log.d(TAG, "Watchdog triggered: Periodic timer due for Dhikr ${dhikr.id} (${dhikr.arabicText.take(12)})")
                            // Advance timestamp so it doesn't re-trigger in loop
                            val intervalMillis = (dhikr.timerValue.coerceAtLeast(1) * dhikr.timerUnit.multiplierMinutes * 60 * 1000L).coerceAtLeast(10000L)
                            prefs.setNextAlarmTimestamp(dhikr.id, now + intervalMillis)
                            com.example.util.DhikrTriggerManager.handlePeriodicTimerAlarm(this@DhikrForegroundService, dhikr.id)
                            com.example.util.DhikrAlarmScheduler.scheduleDhikrAlarmInternal(this@DhikrForegroundService, dhikr, forceReset = false)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Watchdog check error: ${e.message}")
                }
            }
        }
    }

    private fun startForegroundWithNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DhikrApplication.CHANNEL_FOREGROUND_SERVICE)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun registerScreenStateReceiver() {
        if (screenReceiver != null) return

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            // ACTION_USER_PRESENT is handled exclusively by BootAndScreenReceiver
            // to avoid duplicate simultaneous triggers and prevent overlapping
        }

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action ?: return
                when (action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        Log.d(TAG, "Screen turned off")
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        Log.d(TAG, "Screen turned on")
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
    }

    private fun unregisterScreenStateReceiver() {
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering receiver: ${e.message}")
            }
            screenReceiver = null
        }
    }

    private fun launchDhikrOverlay(context: Context, source: String) {
        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences

        if (!com.example.util.ExclusionHelper.canTriggerOverlay(context, prefs)) {
            Log.d(TAG, "Overlay skipped due to ExclusionHelper (call, recording, or pause active)")
            return
        }

        val overlayIntent = Intent(context, DhikrLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(DhikrLockActivity.EXTRA_TRIGGER_SOURCE, source)
        }
        try {
            context.startActivity(overlayIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not start DhikrLockActivity: ${e.message}", e)
        }
    }
}
