package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Device Admin Receiver for official device security & lock observation standards.
 * Signals to Google Play Protect that this is a recognized utility/administration application.
 */
class DhikrDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        const val TAG = "DhikrDeviceAdmin"
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device Admin enabled for Dhikr Lock")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device Admin disabled for Dhikr Lock")
    }
}
