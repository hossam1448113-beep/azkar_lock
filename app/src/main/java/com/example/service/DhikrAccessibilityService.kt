package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.ui.overlay.DhikrLockActivity
import com.example.util.AppInfoHelper

class DhikrAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "DhikrAccessibility"
        @Volatile
        var isDhikrLockActive: Boolean = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName.isEmpty() || packageName == this.packageName) return

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isScreenOn = powerManager?.isInteractive ?: true
        if (!isScreenOn) {
            // CRITICAL FIX: The user turned off the phone screen (pressed power button).
            // Do NOT reassert or wake up the screen! Keep screen completely black as requested!
            return
        }

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isKeyguardActive = keyguardManager?.isKeyguardLocked ?: false

        if (isDhikrLockActive) {
            // If device is in keyguard lock screen state, do not fight system keyguard
            if (isKeyguardActive && packageName == "com.android.systemui") {
                return
            }

            // Only reassert if user is actively trying to bypass the active Dhikr session (e.g. recents or home)
            Log.d(TAG, "Active Dhikr session in progress. User in package: $packageName")
            if (packageName == "com.android.systemui") {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            reassertLockScreen()
            return
        }

        // Identify system feature triggers (e.g., Wi-Fi, Bluetooth, Camera, Settings)
        val className = event.className?.toString() ?: ""
        val targetFeatureOrPackage = when {
            packageName == "com.android.settings" && className.contains("Wifi", ignoreCase = true) ->
                AppInfoHelper.FEATURE_WIFI
            packageName == "com.android.settings" && className.contains("Bluetooth", ignoreCase = true) ->
                AppInfoHelper.FEATURE_BLUETOOTH
            packageName == "com.android.settings" && (className.contains("Network", ignoreCase = true) || className.contains("DataUsage", ignoreCase = true)) ->
                AppInfoHelper.FEATURE_DATA
            packageName == "com.android.settings" && className.contains("Sound", ignoreCase = true) ->
                AppInfoHelper.FEATURE_SOUND
            packageName == "com.android.settings" && className.contains("Display", ignoreCase = true) ->
                AppInfoHelper.FEATURE_DISPLAY
            packageName.contains("camera", ignoreCase = true) ->
                AppInfoHelper.FEATURE_CAMERA
            else -> packageName
        }

        // App-Open / Feature-Open Triggered Azkar check:
        // Intercepts monitored apps or system features before opening, showing a mindful Azkar pause
        com.example.util.DhikrTriggerManager.triggerOnAppOpen(this, targetFeatureOrPackage)
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (isDhikrLockActive && event != null) {
            // Intercept Back, App Switch, and Home keys while lock is active to prevent app bypassing
            if (event.keyCode == KeyEvent.KEYCODE_BACK ||
                event.keyCode == KeyEvent.KEYCODE_APP_SWITCH ||
                event.keyCode == KeyEvent.KEYCODE_HOME) {
                return true // Consume the event
            }
        }
        return super.onKeyEvent(event)
    }

    private fun reassertLockScreen() {
        val intent = Intent(this, DhikrLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }
}
