package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.DhikrApplication
import com.example.ui.overlay.DhikrLockActivity
import com.example.util.PermissionHelper

class BootAndScreenReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootAndScreenReceiver"
        const val ACTION_TRIGGER_DHIKR = "com.aistudio.dhikrlock.ACTION_TRIGGER_DHIKR"
        const val EXTRA_DHIKR_ID = "extra_dhikr_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received action: $action")

        // Acquire temporary CPU WakeLock to guarantee background execution in deep sleep
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        val wakeLock = powerManager?.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "dhikrlock:receiver_wakelock"
        )
        try {
            wakeLock?.acquire(4000L)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire WakeLock: ${e.message}")
        }

        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "Device booted / package replaced. Starting foreground service.")
                if (prefs.isServiceEnabled) {
                    startForegroundService(context)
                    com.example.util.DhikrAlarmScheduler.scheduleAllPeriodicDhikrs(context, forceReset = false)
                }
            }

            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "Phone unlocked (ACTION_USER_PRESENT)")
                com.example.util.DhikrTriggerManager.triggerOnUnlock(context)
            }

            ACTION_TRIGGER_DHIKR -> {
                val dhikrId = intent.getLongExtra(EXTRA_DHIKR_ID, -1L)
                Log.d(TAG, "ACTION_TRIGGER_DHIKR received for ID: $dhikrId")
                if (dhikrId > 0) {
                    com.example.util.DhikrTriggerManager.handlePeriodicTimerAlarm(context, dhikrId)
                }
            }
        }
    }

    private fun triggerDhikrOverlay(context: Context, source: String, dhikrId: Long = -1L) {
        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences

        if (!com.example.util.ExclusionHelper.canTriggerOverlay(context, prefs)) {
            Log.d(TAG, "Overlay cancelled by ExclusionHelper (call/recording/pause)")
            return
        }

        // Only launch if overlay permission or activity launch is available
        val overlayIntent = Intent(context, DhikrLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(DhikrLockActivity.EXTRA_TRIGGER_SOURCE, source)
            if (dhikrId > 0) {
                putExtra(DhikrLockActivity.EXTRA_DHIKR_ID, dhikrId)
            }
        }
        try {
            context.startActivity(overlayIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start DhikrLockActivity: ${e.message}", e)
        }
    }

    private fun startForegroundService(context: Context) {
        val serviceIntent = Intent(context, DhikrForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
