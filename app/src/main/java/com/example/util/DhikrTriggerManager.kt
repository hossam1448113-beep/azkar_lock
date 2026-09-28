package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.DhikrApplication
import com.example.R
import com.example.data.db.AppDatabase
import com.example.ui.overlay.DhikrLockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DhikrTriggerManager {

    private const val TAG = "DhikrTriggerManager"
    private const val DEBOUNCE_MS = 2500L
    private const val REMINDER_NOTIF_BASE = 20000

    @Volatile
    private var lastUnlockTriggerTimestamp: Long = 0L

    /**
     * Handles phone unlock events (ACTION_USER_PRESENT).
     * Strictly coordinates and merges any pending cycle timer Dhikrs with unlock Dhikrs
     * into a single, clean sequential queue in DhikrLockActivity.
     * Prevents overlapping activities, debounce duplication, and ensures lock screen shows
     * "فقط بعد الفتح" (only after unlocking).
     */
    fun triggerOnUnlock(context: Context) {
        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences

        synchronized(this) {
            val now = System.currentTimeMillis()
            if (now - lastUnlockTriggerTimestamp < DEBOUNCE_MS) {
                Log.d(TAG, "Debounce: Unlock trigger ignored (too close to previous trigger).")
                return
            }
            if (DhikrLockActivity.isActivityVisible) {
                Log.d(TAG, "DhikrLockActivity is already active on screen. Ignoring duplicate trigger to prevent overlap.")
                return
            }
            lastUnlockTriggerTimestamp = now
        }

        if (!prefs.isServiceEnabled || prefs.isAppPaused()) {
            Log.d(TAG, "Service disabled or app paused. Skipping unlock trigger.")
            return
        }

        if (!ExclusionHelper.canTriggerOverlay(context, prefs)) {
            Log.d(TAG, "Overlay excluded by active call or screen recording.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. If an active session was already in progress (e.g., interrupted by phone locking mid-session), restore it
                if (prefs.hasActiveSession) {
                    Log.d(TAG, "Restoring active in-progress session upon unlock.")
                    launchOverlayActivity(context, source = "boot_restore", queueIds = null)
                    return@launch
                }

                // 2. Check Mandatory Morning & Evening Azkar windows:
                if (prefs.isMorningAzkarNeededToday()) {
                    Log.d(TAG, "Mandatory Morning Azkar due on unlock.")
                    launchOverlayActivity(context, source = "morning_azkar")
                    return@launch
                }
                if (prefs.isEveningAzkarNeededToday()) {
                    Log.d(TAG, "Mandatory Evening Azkar due on unlock.")
                    launchOverlayActivity(context, source = "evening_azkar")
                    return@launch
                }

                // 3. Retrieve any pending cycle timer Dhikrs that matured during browsing or while screen was off
                val pendingIds = prefs.getPendingTimerDhikrIds().toList()

                // 3. Check if unlock triggers are active and cooldown has passed
                val unlockIds = if (prefs.triggerOnUnlock && prefs.canTriggerOnUnlock()) {
                    app.database.dhikrDao().getUnlockDhikrsSync().map { it.id }
                } else {
                    emptyList()
                }

                // 4. Merge into a clean, unified queue without duplicates:
                // Pending cycle Dhikrs take priority, followed by unlock Dhikrs
                val combinedIds = (pendingIds + unlockIds).distinct()

                if (combinedIds.isEmpty()) {
                    Log.d(TAG, "No pending timer Dhikrs and cooldown active or unlock disabled. Skipping overlay.")
                    return@launch
                }

                // Clear pending timers from prefs and dismiss corresponding notifications
                prefs.clearPendingTimerDhikrs()
                for (id in pendingIds) {
                    cancelReminderNotification(context, id)
                }

                Log.d(TAG, "Launching DhikrLockActivity after unlock with ${combinedIds.size} queued Dhikrs: $combinedIds")
                launchOverlayActivity(context, source = "unlock", queueIds = combinedIds.toLongArray())
            } catch (e: Exception) {
                Log.e(TAG, "Error in triggerOnUnlock: ${e.message}", e)
            }
        }
    }

    /**
     * Handles periodic cycle timer alarm (ACTION_TRIGGER_DHIKR).
     *
     * USER INTENT SPECIFICATION:
     * "الوقت الدورة وحدة لا يظهر شاشة القفل اثناء التصفح فقط بعد الفتح"
     * "شاشفة القفل للقفل فقط"
     *
     * The periodic timer alone MUST NOT show the lock screen during browsing!
     * It only shows the lock screen AFTER unlocking ("فقط بعد الفتح").
     */
    fun handlePeriodicTimerAlarm(context: Context, dhikrId: Long) {
        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences

        if (!prefs.isServiceEnabled || prefs.isAppPaused()) {
            Log.d(TAG, "Service disabled or paused, ignoring periodic timer alarm for Dhikr $dhikrId")
            return
        }

        Log.d(TAG, "Periodic timer alarm triggered for Dhikr ID: $dhikrId")

        // Store this Dhikr as pending for the next phone unlock ("فقط بعد الفتح")
        prefs.addPendingTimerDhikr(dhikrId)

        // Check if user is currently browsing / using phone (screen ON & interactive & device UNLOCKED)
        val isBrowsing = ExclusionHelper.isScreenInteractiveAndUnlocked(context)
        if (isBrowsing) {
            // User is actively browsing! Do NOT interrupt with fullscreen lock screen.
            // Post a clean, high-priority notification in the status bar so they are aware,
            // and the lock screen will only appear when they unlock next time!
            Log.d(TAG, "User is browsing: Deferring lock screen until next unlock. Displaying polite notification.")
            CoroutineScope(Dispatchers.IO).launch {
                val dhikr = app.database.dhikrDao().getDhikrById(dhikrId)
                val text = dhikr?.arabicText ?: "سُبْحَانَ اللَّهِ"
                postPeriodicReminderNotification(context, dhikrId, text)
            }
        } else {
            // Screen is OFF or locked in pocket: keep in pending list for next unlock
            Log.d(TAG, "Screen is off or locked. Stored Dhikr $dhikrId as pending for next unlock.")
        }
    }

    /**
     * Displays a respectful notification in the status bar when a periodic cycle alarm matures during browsing.
     * Tapping it allows voluntary recitation, or the user can ignore it and it will display automatically
     * on the next unlock.
     */
    fun postPeriodicReminderNotification(context: Context, dhikrId: Long, dhikrText: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val openIntent = Intent(context, DhikrLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(DhikrLockActivity.EXTRA_TRIGGER_SOURCE, "notification_click")
            putExtra(DhikrLockActivity.EXTRA_DHIKR_ID, dhikrId)
        }

        val reqCode = (REMINDER_NOTIF_BASE + dhikrId).toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            reqCode,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DhikrApplication.CHANNEL_DHIKR_REMINDER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("حان موعد ذكر الله 🌿")
            .setContentText(dhikrText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("﴿ $dhikrText ﴾\n\nاضغط هنا للبدء بالذكر الآن، أو ستظهر لك شاشة الذكر تلقائياً عند فتح الهاتف القادم."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reqCode, notification)
    }

    fun cancelReminderNotification(context: Context, dhikrId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val reqCode = (REMINDER_NOTIF_BASE + dhikrId).toInt()
        notificationManager.cancel(reqCode)
    }

    /**
     * Handles App-Open Triggered Azkar.
     * Displays a dedicated, distraction-free Azkar pause before the user enters a monitored app (e.g. social media).
     * Enforces the user's custom target (Count, Duration, or Both) and honors the Rest / Cool-down interval.
     */
    fun triggerOnAppOpen(context: Context, packageName: String) {
        val app = context.applicationContext as DhikrApplication
        val prefs = app.preferences

        if (!prefs.canTriggerForApp(packageName)) {
            return
        }

        if (DhikrLockActivity.isActivityVisible) {
            Log.d(TAG, "DhikrLockActivity is already visible. Skipping app-open trigger.")
            return
        }

        if (!ExclusionHelper.canTriggerOverlay(context, prefs)) {
            Log.d(TAG, "Overlay excluded by active phone call or screen recording.")
            return
        }

        // Record trigger timestamp for this specific app to enforce the rest/cooldown interval
        prefs.setLastAppOpenTriggerTime(packageName, System.currentTimeMillis())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Determine which Dhikr and targets to show (per-app customization supported):
                val rule = prefs.getAppOpenRule(packageName)
                val targetMode = if (rule.isCustomized) rule.targetMode else prefs.appOpenTargetMode
                val targetCount = if (rule.isCustomized) rule.targetCount else prefs.appOpenTargetCount
                val targetTime = if (rule.isCustomized) rule.targetTimeSeconds else prefs.appOpenTargetTimeSeconds
                val customDhikrId = if (rule.isCustomized && rule.specificDhikrId > 0) rule.specificDhikrId else prefs.appOpenDhikrId

                val targetDhikr = if (customDhikrId > 0) {
                    app.database.dhikrDao().getDhikrById(customDhikrId)
                } else {
                    val activeList = app.database.dhikrDao().getActiveDhikrsSync()
                    if (activeList.isNotEmpty()) activeList.random() else AppDatabase.DEFAULT_PRESETS.first()
                }

                val dhikrId = targetDhikr?.id ?: 1L

                val intent = Intent(context, DhikrLockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(DhikrLockActivity.EXTRA_TRIGGER_SOURCE, "app_open")
                    putExtra(DhikrLockActivity.EXTRA_APP_OPEN_PACKAGE, packageName)
                    putExtra(DhikrLockActivity.EXTRA_CUSTOM_TARGET_MODE, targetMode.name)
                    putExtra(DhikrLockActivity.EXTRA_CUSTOM_COUNT, targetCount)
                    putExtra(DhikrLockActivity.EXTRA_CUSTOM_TIME, targetTime)
                    putExtra(DhikrLockActivity.EXTRA_DHIKR_ID, dhikrId)
                }
                context.startActivity(intent)
                Log.d(TAG, "Triggered Pre-App Open Azkar for package $packageName with Dhikr ID $dhikrId (Count: $targetCount, Time: ${targetTime}s)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch Pre-App Open Dhikr: ${e.message}", e)
            }
        }
    }

    private fun launchOverlayActivity(
        context: Context,
        source: String,
        queueIds: LongArray? = null,
        specificId: Long = -1L
    ) {
        val intent = Intent(context, DhikrLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(DhikrLockActivity.EXTRA_TRIGGER_SOURCE, source)
            if (queueIds != null && queueIds.isNotEmpty()) {
                putExtra(DhikrLockActivity.EXTRA_QUEUE_IDS, queueIds)
            }
            if (specificId > 0) {
                putExtra(DhikrLockActivity.EXTRA_DHIKR_ID, specificId)
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch DhikrLockActivity: ${e.message}", e)
        }
    }
}
