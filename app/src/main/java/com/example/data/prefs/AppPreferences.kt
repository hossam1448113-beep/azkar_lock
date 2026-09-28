package com.example.data.prefs

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dhikr_lock_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVICE_ENABLED = "key_service_enabled"
        private const val KEY_COOLDOWN_VALUE = "key_cooldown_value"
        private const val KEY_COOLDOWN_UNIT = "key_cooldown_unit"
        private const val KEY_LAST_OVERLAY_TIME = "key_last_overlay_time"
        private const val KEY_TRIGGER_ON_UNLOCK = "key_trigger_on_unlock"
        private const val KEY_TRIGGER_ON_TIMER = "key_trigger_on_timer"
        private const val KEY_PERIODIC_INTERVAL = "key_periodic_interval"
        private const val KEY_VIBRATION_ENABLED = "key_vibration_enabled"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_STRICT_MODE = "key_strict_mode"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_PAUSE_UNTIL_TIMESTAMP = "key_pause_until_timestamp"
        private const val KEY_PAUSE_DURATION_VALUE = "key_pause_duration_value"
        private const val KEY_PAUSE_DURATION_UNIT = "key_pause_duration_unit"

        // Active Session State Persistence (reboot recovery & zero-bypass)
        private const val KEY_HAS_ACTIVE_SESSION = "key_has_active_session"
        private const val KEY_ACTIVE_SESSION_SOURCE = "key_active_session_source"
        private const val KEY_ACTIVE_SESSION_QUEUE_IDS = "key_active_session_queue_ids"
        private const val KEY_ACTIVE_SESSION_QUEUE_INDEX = "key_active_session_queue_index"
        private const val KEY_ACTIVE_SESSION_CURRENT_COUNT = "key_active_session_current_count"
        private const val KEY_ACTIVE_SESSION_ELAPSED_SECONDS = "key_active_session_elapsed_seconds"

        // Screen-off deferred timer execution
        private const val KEY_HAS_PENDING_TIMER = "key_has_pending_timer"
        private const val KEY_PENDING_TIMER_DHIKR_ID = "key_pending_timer_dhikr_id"

        // App-Open Triggered Azkar settings
        private const val KEY_APP_OPEN_ENABLED = "key_app_open_enabled"
        private const val KEY_APP_OPEN_PACKAGES = "key_app_open_packages"
        private const val KEY_APP_OPEN_TARGET_MODE = "key_app_open_target_mode"
        private const val KEY_APP_OPEN_TARGET_COUNT = "key_app_open_target_count"
        private const val KEY_APP_OPEN_TARGET_TIME = "key_app_open_target_time"
        private const val KEY_APP_OPEN_COOLDOWN_MINUTES = "key_app_open_cooldown_minutes"
        private const val KEY_APP_OPEN_DHIKR_ID = "key_app_open_dhikr_id"

        // Last dedicated session report
        private const val KEY_LAST_REPORT_DHIKR_TEXT = "key_last_report_dhikr_text"
        private const val KEY_LAST_REPORT_COUNT = "key_last_report_count"
        private const val KEY_LAST_REPORT_SECONDS = "key_last_report_seconds"
        private const val KEY_LAST_REPORT_TIMESTAMP = "key_last_report_timestamp"

        // Mandatory Morning & Evening Azkar settings
        private const val KEY_MANDATORY_MORNING_ENABLED = "key_mandatory_morning_enabled"
        private const val KEY_MORNING_START_HOUR = "key_morning_start_hour"
        private const val KEY_MORNING_END_HOUR = "key_morning_end_hour"
        private const val KEY_MANDATORY_EVENING_ENABLED = "key_mandatory_evening_enabled"
        private const val KEY_EVENING_START_HOUR = "key_evening_start_hour"
        private const val KEY_EVENING_END_HOUR = "key_evening_end_hour"
        private const val KEY_LAST_COMPLETED_MORNING_DATE = "key_last_completed_morning_date"
        private const val KEY_LAST_COMPLETED_EVENING_DATE = "key_last_completed_evening_date"
    }

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    var cooldownValue: Int
        get() = prefs.getInt(KEY_COOLDOWN_VALUE, 5)
        set(value) = prefs.edit().putInt(KEY_COOLDOWN_VALUE, value).apply()

    var cooldownUnit: String
        get() = prefs.getString(KEY_COOLDOWN_UNIT, "MINUTES") ?: "MINUTES"
        set(value) = prefs.edit().putString(KEY_COOLDOWN_UNIT, value).apply()

    // Backward compatible getter/setter in minutes
    var cooldownMinutes: Int
        get() = if (cooldownUnit == "HOURS") cooldownValue * 60 else cooldownValue
        set(value) {
            cooldownValue = value
            cooldownUnit = "MINUTES"
        }

    var lastOverlayTime: Long
        get() = prefs.getLong(KEY_LAST_OVERLAY_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_OVERLAY_TIME, value).apply()

    var triggerOnUnlock: Boolean
        get() = prefs.getBoolean(KEY_TRIGGER_ON_UNLOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_TRIGGER_ON_UNLOCK, value).apply()

    var triggerOnPeriodicTimer: Boolean
        get() = prefs.getBoolean(KEY_TRIGGER_ON_TIMER, true)
        set(value) = prefs.edit().putBoolean(KEY_TRIGGER_ON_TIMER, value).apply()

    var periodicIntervalMinutes: Int
        get() = prefs.getInt(KEY_PERIODIC_INTERVAL, 20)
        set(value) = prefs.edit().putInt(KEY_PERIODIC_INTERVAL, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isStrictMode: Boolean
        get() = prefs.getBoolean(KEY_STRICT_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_STRICT_MODE, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var pauseUntilTimestamp: Long
        get() = prefs.getLong(KEY_PAUSE_UNTIL_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_PAUSE_UNTIL_TIMESTAMP, value).apply()

    var pauseDurationValue: Int
        get() = prefs.getInt(KEY_PAUSE_DURATION_VALUE, 1)
        set(value) = prefs.edit().putInt(KEY_PAUSE_DURATION_VALUE, value).apply()

    var pauseDurationUnit: String
        get() = prefs.getString(KEY_PAUSE_DURATION_UNIT, "HOURS") ?: "HOURS"
        set(value) = prefs.edit().putString(KEY_PAUSE_DURATION_UNIT, value).apply()

    fun isAppPaused(): Boolean {
        return System.currentTimeMillis() < pauseUntilTimestamp
    }

    fun getRemainingPauseMillis(): Long {
        val remaining = pauseUntilTimestamp - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }

    fun pauseApp(value: Int, unit: String) {
        val multiplier = when (unit) {
            "MINUTES" -> 60 * 1000L
            "HOURS" -> 3600 * 1000L
            "DAYS" -> 86400 * 1000L
            else -> 60 * 1000L
        }
        val durationMillis = value.coerceAtLeast(1) * multiplier
        pauseDurationValue = value
        pauseDurationUnit = unit
        pauseUntilTimestamp = System.currentTimeMillis() + durationMillis
    }

    fun resumeAppNow() {
        pauseUntilTimestamp = 0L
    }

    fun getCooldownMillis(): Long {
        val factor = if (cooldownUnit == "HOURS") 3600 * 1000L else 60 * 1000L
        return (cooldownValue * factor).coerceAtLeast(0L)
    }

    /**
     * Checks if cooldown period has elapsed since last overlay and app is not paused
     */
    fun canTriggerOnUnlock(): Boolean {
        if (!isServiceEnabled || !triggerOnUnlock || isAppPaused()) return false
        val cooldownMillis = getCooldownMillis()
        val elapsed = System.currentTimeMillis() - lastOverlayTime
        return elapsed >= cooldownMillis
    }

    fun markOverlayShownNow() {
        lastOverlayTime = System.currentTimeMillis()
    }

    // Active Session Management
    val hasActiveSession: Boolean
        get() = prefs.getBoolean(KEY_HAS_ACTIVE_SESSION, false)

    val activeSessionSource: String
        get() = prefs.getString(KEY_ACTIVE_SESSION_SOURCE, "unlock") ?: "unlock"

    val activeSessionQueueIndex: Int
        get() = prefs.getInt(KEY_ACTIVE_SESSION_QUEUE_INDEX, 0)

    val activeSessionCurrentCount: Int
        get() = prefs.getInt(KEY_ACTIVE_SESSION_CURRENT_COUNT, 0)

    val activeSessionElapsedSeconds: Int
        get() = prefs.getInt(KEY_ACTIVE_SESSION_ELAPSED_SECONDS, 0)

    fun getActiveSessionQueueIds(): List<Long> {
        val str = prefs.getString(KEY_ACTIVE_SESSION_QUEUE_IDS, "") ?: ""
        if (str.isBlank()) return emptyList()
        return str.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    fun saveActiveSession(
        source: String,
        queueIds: List<Long>,
        queueIndex: Int,
        currentCount: Int,
        elapsedSeconds: Int
    ) {
        prefs.edit()
            .putBoolean(KEY_HAS_ACTIVE_SESSION, true)
            .putString(KEY_ACTIVE_SESSION_SOURCE, source)
            .putString(KEY_ACTIVE_SESSION_QUEUE_IDS, queueIds.joinToString(","))
            .putInt(KEY_ACTIVE_SESSION_QUEUE_INDEX, queueIndex)
            .putInt(KEY_ACTIVE_SESSION_CURRENT_COUNT, currentCount)
            .putInt(KEY_ACTIVE_SESSION_ELAPSED_SECONDS, elapsedSeconds)
            .apply()
    }

    fun updateActiveSessionProgress(count: Int, elapsedSeconds: Int, queueIndex: Int) {
        prefs.edit()
            .putInt(KEY_ACTIVE_SESSION_CURRENT_COUNT, count)
            .putInt(KEY_ACTIVE_SESSION_ELAPSED_SECONDS, elapsedSeconds)
            .putInt(KEY_ACTIVE_SESSION_QUEUE_INDEX, queueIndex)
            .apply()
    }

    fun clearActiveSession() {
        prefs.edit()
            .putBoolean(KEY_HAS_ACTIVE_SESSION, false)
            .remove(KEY_ACTIVE_SESSION_SOURCE)
            .remove(KEY_ACTIVE_SESSION_QUEUE_IDS)
            .remove(KEY_ACTIVE_SESSION_QUEUE_INDEX)
            .remove(KEY_ACTIVE_SESSION_CURRENT_COUNT)
            .remove(KEY_ACTIVE_SESSION_ELAPSED_SECONDS)
            .apply()
    }

    // Pending Timer Trigger (when cycle timer fires during browsing or screen-off)
    var hasPendingTimerTrigger: Boolean
        get() = prefs.getBoolean(KEY_HAS_PENDING_TIMER, false) || getPendingTimerDhikrIds().isNotEmpty()
        set(value) = prefs.edit().putBoolean(KEY_HAS_PENDING_TIMER, value).apply()

    var pendingTimerDhikrId: Long
        get() {
            val pendingSet = getPendingTimerDhikrIds()
            return if (pendingSet.isNotEmpty()) pendingSet.first() else prefs.getLong(KEY_PENDING_TIMER_DHIKR_ID, -1L)
        }
        set(value) = prefs.edit().putLong(KEY_PENDING_TIMER_DHIKR_ID, value).apply()

    fun getPendingTimerDhikrIds(): Set<Long> {
        val str = prefs.getString("key_pending_timer_ids", "") ?: ""
        if (str.isBlank()) {
            val single = prefs.getLong(KEY_PENDING_TIMER_DHIKR_ID, -1L)
            return if (single > 0) setOf(single) else emptySet()
        }
        return str.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { it > 0 }
            .toSet()
    }

    fun addPendingTimerDhikr(id: Long) {
        if (id <= 0) return
        val current = getPendingTimerDhikrIds().toMutableSet()
        current.add(id)
        prefs.edit()
            .putString("key_pending_timer_ids", current.joinToString(","))
            .putBoolean(KEY_HAS_PENDING_TIMER, true)
            .putLong(KEY_PENDING_TIMER_DHIKR_ID, id)
            .apply()
    }

    fun removePendingTimerDhikr(id: Long) {
        val current = getPendingTimerDhikrIds().toMutableSet()
        current.remove(id)
        val editor = prefs.edit()
        if (current.isEmpty()) {
            editor.remove("key_pending_timer_ids")
            editor.putBoolean(KEY_HAS_PENDING_TIMER, false)
            editor.putLong(KEY_PENDING_TIMER_DHIKR_ID, -1L)
        } else {
            editor.putString("key_pending_timer_ids", current.joinToString(","))
            editor.putLong(KEY_PENDING_TIMER_DHIKR_ID, current.first())
        }
        editor.apply()
    }

    fun clearPendingTimerDhikrs() {
        prefs.edit()
            .remove("key_pending_timer_ids")
            .putBoolean(KEY_HAS_PENDING_TIMER, false)
            .putLong(KEY_PENDING_TIMER_DHIKR_ID, -1L)
            .apply()
    }

    fun setPendingTimerTrigger(dhikrId: Long) {
        addPendingTimerDhikr(dhikrId)
    }

    fun clearPendingTimerTrigger() {
        clearPendingTimerDhikrs()
    }

    // Exact cycle timer target timestamps for per-dhikr precision
    fun getNextAlarmTimestamp(dhikrId: Long): Long {
        return prefs.getLong("key_next_alarm_$dhikrId", 0L)
    }

    fun setNextAlarmTimestamp(dhikrId: Long, timestamp: Long) {
        prefs.edit().putLong("key_next_alarm_$dhikrId", timestamp).apply()
    }

    fun clearNextAlarmTimestamp(dhikrId: Long) {
        prefs.edit().remove("key_next_alarm_$dhikrId").apply()
    }

    // Last Dedicated Session Report
    fun saveLastSessionReport(dhikrText: String, count: Int, seconds: Int) {
        prefs.edit()
            .putString(KEY_LAST_REPORT_DHIKR_TEXT, dhikrText)
            .putInt(KEY_LAST_REPORT_COUNT, count)
            .putInt(KEY_LAST_REPORT_SECONDS, seconds)
            .putLong(KEY_LAST_REPORT_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    val lastReportDhikrText: String?
        get() = prefs.getString(KEY_LAST_REPORT_DHIKR_TEXT, null)

    val lastReportCount: Int
        get() = prefs.getInt(KEY_LAST_REPORT_COUNT, 0)

    val lastReportSeconds: Int
        get() = prefs.getInt(KEY_LAST_REPORT_SECONDS, 0)

    val lastReportTimestamp: Long
        get() = prefs.getLong(KEY_LAST_REPORT_TIMESTAMP, 0L)

    // ==========================================
    // App-Open Triggered Azkar Settings & Logic
    // ==========================================

    var isAppOpenTriggerEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_OPEN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_OPEN_ENABLED, value).apply()

    var appOpenTargetMode: com.example.data.model.TargetMode
        get() {
            val name = prefs.getString(KEY_APP_OPEN_TARGET_MODE, com.example.data.model.TargetMode.COUNT_ONLY.name)
            return try {
                com.example.data.model.TargetMode.valueOf(name ?: com.example.data.model.TargetMode.COUNT_ONLY.name)
            } catch (e: Exception) {
                com.example.data.model.TargetMode.COUNT_ONLY
            }
        }
        set(value) = prefs.edit().putString(KEY_APP_OPEN_TARGET_MODE, value.name).apply()

    var appOpenTargetCount: Int
        get() = prefs.getInt(KEY_APP_OPEN_TARGET_COUNT, 3)
        set(value) = prefs.edit().putInt(KEY_APP_OPEN_TARGET_COUNT, value.coerceAtLeast(1)).apply()

    var appOpenTargetTimeSeconds: Int
        get() = prefs.getInt(KEY_APP_OPEN_TARGET_TIME, 15)
        set(value) = prefs.edit().putInt(KEY_APP_OPEN_TARGET_TIME, value.coerceAtLeast(5)).apply()

    var appOpenCooldownMinutes: Int
        get() = prefs.getInt(KEY_APP_OPEN_COOLDOWN_MINUTES, 15)
        set(value) = prefs.edit().putInt(KEY_APP_OPEN_COOLDOWN_MINUTES, value.coerceAtLeast(1)).apply()

    var appOpenDhikrId: Long
        get() = prefs.getLong(KEY_APP_OPEN_DHIKR_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_APP_OPEN_DHIKR_ID, value).apply()

    var appOpenMonitoredPackages: Set<String>
        get() = getMonitoredAppPackages()
        set(value) = setMonitoredAppPackages(value)

    fun getMonitoredAppPackages(): Set<String> {
        return prefs.getStringSet(KEY_APP_OPEN_PACKAGES, null) ?: setOf(
            "com.whatsapp",
            "com.facebook.katana",
            "com.instagram.android",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.zhiliaoapp.musically"
        )
    }

    fun setMonitoredAppPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_APP_OPEN_PACKAGES, packages).apply()
    }

    fun isAppMonitored(packageName: String): Boolean {
        return getMonitoredAppPackages().contains(packageName)
    }

    fun toggleAppMonitored(packageName: String) {
        val current = getMonitoredAppPackages().toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        setMonitoredAppPackages(current)
    }

    fun getLastAppOpenTriggerTime(packageName: String): Long {
        return prefs.getLong("key_app_open_last_$packageName", 0L)
    }

    fun setLastAppOpenTriggerTime(packageName: String, time: Long) {
        prefs.edit().putLong("key_app_open_last_$packageName", time).apply()
    }

    fun canTriggerForApp(packageName: String): Boolean {
        if (!isServiceEnabled || !isAppOpenTriggerEnabled || isAppPaused()) return false
        if (!isAppMonitored(packageName)) return false
        val lastTime = getLastAppOpenTriggerTime(packageName)
        val rule = getAppOpenRule(packageName)
        val cooldown = if (rule.isCustomized) rule.cooldownMinutes else appOpenCooldownMinutes
        val cooldownMillis = cooldown * 60 * 1000L
        return (System.currentTimeMillis() - lastTime) >= cooldownMillis
    }

    // Per-app customization
    fun getAppOpenRule(packageName: String): AppOpenRule {
        val customized = prefs.getBoolean("app_rule_custom_$packageName", false)
        val modeStr = prefs.getString("app_rule_mode_$packageName", appOpenTargetMode.name)
        val mode = try {
            com.example.data.model.TargetMode.valueOf(modeStr ?: appOpenTargetMode.name)
        } catch (e: Exception) {
            appOpenTargetMode
        }
        val count = prefs.getInt("app_rule_count_$packageName", appOpenTargetCount)
        val timeSec = prefs.getInt("app_rule_time_$packageName", appOpenTargetTimeSeconds)
        val cooldown = prefs.getInt("app_rule_cooldown_$packageName", appOpenCooldownMinutes)
        val dhikrId = prefs.getLong("app_rule_dhikr_$packageName", appOpenDhikrId)

        return AppOpenRule(
            packageName = packageName,
            isCustomized = customized,
            targetMode = mode,
            targetCount = count,
            targetTimeSeconds = timeSec,
            cooldownMinutes = cooldown,
            specificDhikrId = dhikrId
        )
    }

    fun saveAppOpenRule(rule: AppOpenRule) {
        prefs.edit()
            .putBoolean("app_rule_custom_${rule.packageName}", rule.isCustomized)
            .putString("app_rule_mode_${rule.packageName}", rule.targetMode.name)
            .putInt("app_rule_count_${rule.packageName}", rule.targetCount)
            .putInt("app_rule_time_${rule.packageName}", rule.targetTimeSeconds)
            .putInt("app_rule_cooldown_${rule.packageName}", rule.cooldownMinutes)
            .putLong("app_rule_dhikr_${rule.packageName}", rule.specificDhikrId)
            .apply()
    }

    // ==========================================
    // Mandatory Morning & Evening Azkar Settings
    // ==========================================

    var isMandatoryMorningAzkarEnabled: Boolean
        get() = prefs.getBoolean(KEY_MANDATORY_MORNING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MANDATORY_MORNING_ENABLED, value).apply()

    var morningStartHour: Int
        get() = prefs.getInt(KEY_MORNING_START_HOUR, 5) // 5:00 AM
        set(value) = prefs.edit().putInt(KEY_MORNING_START_HOUR, value.coerceIn(0, 23)).apply()

    var morningEndHour: Int
        get() = prefs.getInt(KEY_MORNING_END_HOUR, 11) // 11:00 AM
        set(value) = prefs.edit().putInt(KEY_MORNING_END_HOUR, value.coerceIn(0, 23)).apply()

    var isMandatoryEveningAzkarEnabled: Boolean
        get() = prefs.getBoolean(KEY_MANDATORY_EVENING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MANDATORY_EVENING_ENABLED, value).apply()

    var eveningStartHour: Int
        get() = prefs.getInt(KEY_EVENING_START_HOUR, 16) // 4:00 PM (16:00)
        set(value) = prefs.edit().putInt(KEY_EVENING_START_HOUR, value.coerceIn(0, 23)).apply()

    var eveningEndHour: Int
        get() = prefs.getInt(KEY_EVENING_END_HOUR, 19) // 7:00 PM (19:00)
        set(value) = prefs.edit().putInt(KEY_EVENING_END_HOUR, value.coerceIn(0, 23)).apply()

    var lastCompletedMorningDate: String
        get() = prefs.getString(KEY_LAST_COMPLETED_MORNING_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_COMPLETED_MORNING_DATE, value).apply()

    var lastCompletedEveningDate: String
        get() = prefs.getString(KEY_LAST_COMPLETED_EVENING_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_COMPLETED_EVENING_DATE, value).apply()

    fun getTodayDateString(): String {
        val cal = java.util.Calendar.getInstance()
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) + 1
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    fun isCurrentHourBetween(startHour: Int, endHour: Int): Boolean {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (startHour <= endHour) {
            currentHour in startHour until endHour
        } else {
            // Spans midnight (e.g. 22 to 4)
            currentHour >= startHour || currentHour < endHour
        }
    }

    fun isMorningTime(): Boolean = isCurrentHourBetween(morningStartHour, morningEndHour)

    fun isEveningTime(): Boolean = isCurrentHourBetween(eveningStartHour, eveningEndHour)

    fun isMorningAzkarNeededToday(): Boolean {
        if (!isServiceEnabled || isAppPaused() || !isMandatoryMorningAzkarEnabled) return false
        return isMorningTime() && lastCompletedMorningDate != getTodayDateString()
    }

    fun isEveningAzkarNeededToday(): Boolean {
        if (!isServiceEnabled || isAppPaused() || !isMandatoryEveningAzkarEnabled) return false
        return isEveningTime() && lastCompletedEveningDate != getTodayDateString()
    }

    fun markMorningAzkarCompletedToday() {
        lastCompletedMorningDate = getTodayDateString()
    }

    fun markEveningAzkarCompletedToday() {
        lastCompletedEveningDate = getTodayDateString()
    }
}

data class AppOpenRule(
    val packageName: String,
    val isCustomized: Boolean = false,
    val targetMode: com.example.data.model.TargetMode = com.example.data.model.TargetMode.COUNT_ONLY,
    val targetCount: Int = 3,
    val targetTimeSeconds: Int = 15,
    val cooldownMinutes: Int = 15,
    val specificDhikrId: Long = -1L
)
