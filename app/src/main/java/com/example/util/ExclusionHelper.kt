package com.example.util

import android.app.KeyguardManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Display
import com.example.data.prefs.AppPreferences

object ExclusionHelper {

    private const val TAG = "ExclusionHelper"

    /**
     * Detects if an active cellular or VOIP (WhatsApp, Telegram, etc.) call is in progress.
     */
    fun isCallActive(context: Context): Boolean {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val mode = audioManager.mode
                if (mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION) {
                    Log.d(TAG, "Exclusion: Active audio call detected (mode=$mode)")
                    return true
                }
            }

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (telephonyManager != null) {
                @Suppress("DEPRECATION")
                val callState = telephonyManager.callState
                if (callState != TelephonyManager.CALL_STATE_IDLE) {
                    Log.d(TAG, "Exclusion: Active phone call detected (callState=$callState)")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking call state: ${e.message}")
        }
        return false
    }

    /**
     * Detects if screen recording, casting, or an external presentation display is active.
     */
    fun isScreenRecordingOrCasting(context: Context): Boolean {
        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            if (displayManager != null) {
                val displays = displayManager.displays
                for (display in displays) {
                    // Non-default display or presentation display indicates screen cast / recording virtual display
                    if (display.displayId != Display.DEFAULT_DISPLAY) {
                        val flags = display.flags
                        val isPresentation = (flags and Display.FLAG_PRESENTATION) != 0
                        val isPrivate = (flags and Display.FLAG_PRIVATE) != 0
                        Log.d(TAG, "Exclusion: Secondary display active id=${display.displayId}, flags=$flags")
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking display state: ${e.message}")
        }
        return false
    }

    /**
     * Verifies that the screen is ON and the device is completely UNLOCKED (no keyguard).
     * Strictly enforces ACTIVE UNLOCK DISPLAY ONLY (NO SCREEN-OFF TRIGGERS) to prevent
     * battery drain or screen-off execution.
     */
    fun isScreenInteractiveAndUnlocked(context: Context): Boolean {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val isInteractive = powerManager?.isInteractive ?: false
            if (!isInteractive) {
                Log.d(TAG, "Screen is OFF or non-interactive.")
                return false
            }

            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isLocked = keyguardManager?.isKeyguardLocked ?: false
            if (isLocked) {
                Log.d(TAG, "Keyguard is locked. Phone is not yet unlocked.")
                return false
            }
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Error checking screen interactive state: ${e.message}")
            return true
        }
    }

    /**
     * Full check before triggering Dhikr overlay:
     * - App must not be paused
     * - No active calls
     * - No active screen recording
     */
    fun canTriggerOverlay(context: Context, prefs: AppPreferences): Boolean {
        if (prefs.isAppPaused()) {
            Log.d(TAG, "Overlay blocked: App is temporarily paused until ${prefs.pauseUntilTimestamp}")
            return false
        }
        if (isCallActive(context)) {
            Log.d(TAG, "Overlay blocked: Active phone or VoIP call is in progress")
            return false
        }
        if (isScreenRecordingOrCasting(context)) {
            Log.d(TAG, "Overlay blocked: Screen recording or projection is active")
            return false
        }
        return true
    }
}
