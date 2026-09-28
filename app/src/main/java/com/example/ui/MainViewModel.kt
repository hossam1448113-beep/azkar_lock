package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DhikrApplication
import com.example.data.model.DhikrHistory
import com.example.data.model.DhikrItem
import com.example.service.DhikrForegroundService
import com.example.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhikrApplication
    private val repository = app.repository
    private val prefs = app.preferences

    val allDhikrs: StateFlow<List<DhikrItem>> = repository.allDhikrs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allHistory: StateFlow<List<DhikrHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isServiceEnabled = MutableStateFlow(prefs.isServiceEnabled)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _cooldownValue = MutableStateFlow(prefs.cooldownValue)
    val cooldownValue: StateFlow<Int> = _cooldownValue.asStateFlow()

    private val _cooldownUnit = MutableStateFlow(prefs.cooldownUnit)
    val cooldownUnit: StateFlow<String> = _cooldownUnit.asStateFlow()

    private val _cooldownMinutes = MutableStateFlow(prefs.cooldownMinutes)
    val cooldownMinutes: StateFlow<Int> = _cooldownMinutes.asStateFlow()

    private val _triggerOnUnlock = MutableStateFlow(prefs.triggerOnUnlock)
    val triggerOnUnlock: StateFlow<Boolean> = _triggerOnUnlock.asStateFlow()

    private val _triggerOnPeriodicTimer = MutableStateFlow(prefs.triggerOnPeriodicTimer)
    val triggerOnPeriodicTimer: StateFlow<Boolean> = _triggerOnPeriodicTimer.asStateFlow()

    private val _periodicIntervalMinutes = MutableStateFlow(prefs.periodicIntervalMinutes)
    val periodicIntervalMinutes: StateFlow<Int> = _periodicIntervalMinutes.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.isVibrationEnabled)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.isSoundEnabled)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    // App-Open Trigger states
    private val _isAppOpenTriggerEnabled = MutableStateFlow(prefs.isAppOpenTriggerEnabled)
    val isAppOpenTriggerEnabled: StateFlow<Boolean> = _isAppOpenTriggerEnabled.asStateFlow()

    private val _appOpenTargetMode = MutableStateFlow(prefs.appOpenTargetMode)
    val appOpenTargetMode: StateFlow<com.example.data.model.TargetMode> = _appOpenTargetMode.asStateFlow()

    private val _appOpenTargetCount = MutableStateFlow(prefs.appOpenTargetCount)
    val appOpenTargetCount: StateFlow<Int> = _appOpenTargetCount.asStateFlow()

    private val _appOpenTargetTimeSeconds = MutableStateFlow(prefs.appOpenTargetTimeSeconds)
    val appOpenTargetTimeSeconds: StateFlow<Int> = _appOpenTargetTimeSeconds.asStateFlow()

    private val _appOpenCooldownMinutes = MutableStateFlow(prefs.appOpenCooldownMinutes)
    val appOpenCooldownMinutes: StateFlow<Int> = _appOpenCooldownMinutes.asStateFlow()

    private val _appOpenMonitoredPackages = MutableStateFlow(prefs.appOpenMonitoredPackages)
    val appOpenMonitoredPackages: StateFlow<Set<String>> = _appOpenMonitoredPackages.asStateFlow()

    // Mandatory Morning & Evening Azkar states
    private val _isMandatoryMorningAzkarEnabled = MutableStateFlow(prefs.isMandatoryMorningAzkarEnabled)
    val isMandatoryMorningAzkarEnabled: StateFlow<Boolean> = _isMandatoryMorningAzkarEnabled.asStateFlow()

    private val _morningStartHour = MutableStateFlow(prefs.morningStartHour)
    val morningStartHour: StateFlow<Int> = _morningStartHour.asStateFlow()

    private val _morningEndHour = MutableStateFlow(prefs.morningEndHour)
    val morningEndHour: StateFlow<Int> = _morningEndHour.asStateFlow()

    private val _isMandatoryEveningAzkarEnabled = MutableStateFlow(prefs.isMandatoryEveningAzkarEnabled)
    val isMandatoryEveningAzkarEnabled: StateFlow<Boolean> = _isMandatoryEveningAzkarEnabled.asStateFlow()

    private val _eveningStartHour = MutableStateFlow(prefs.eveningStartHour)
    val eveningStartHour: StateFlow<Int> = _eveningStartHour.asStateFlow()

    private val _eveningEndHour = MutableStateFlow(prefs.eveningEndHour)
    val eveningEndHour: StateFlow<Int> = _eveningEndHour.asStateFlow()

    // App Pause state
    private val _isAppPaused = MutableStateFlow(prefs.isAppPaused())
    val isAppPaused: StateFlow<Boolean> = _isAppPaused.asStateFlow()

    private val _remainingPauseMillis = MutableStateFlow(prefs.getRemainingPauseMillis())
    val remainingPauseMillis: StateFlow<Long> = _remainingPauseMillis.asStateFlow()

    // Permission states
    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasAccessibilityPermission = MutableStateFlow(false)
    val hasAccessibilityPermission: StateFlow<Boolean> = _hasAccessibilityPermission.asStateFlow()

    private val _hasBatteryOptIgnored = MutableStateFlow(false)
    val hasBatteryOptIgnored: StateFlow<Boolean> = _hasBatteryOptIgnored.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _hasExactAlarmPermission = MutableStateFlow(false)
    val hasExactAlarmPermission: StateFlow<Boolean> = _hasExactAlarmPermission.asStateFlow()

    private val _hasPhoneStatePermission = MutableStateFlow(false)
    val hasPhoneStatePermission: StateFlow<Boolean> = _hasPhoneStatePermission.asStateFlow()

    init {
        refreshPermissions()
        refreshPauseState()
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        _hasOverlayPermission.value = PermissionHelper.hasOverlayPermission(context)
        _hasAccessibilityPermission.value = PermissionHelper.isAccessibilityServiceEnabled(context)
        _hasBatteryOptIgnored.value = PermissionHelper.isBatteryOptimizationIgnored(context)
        _hasNotificationPermission.value = PermissionHelper.hasNotificationPermission(context)
        _hasExactAlarmPermission.value = PermissionHelper.canScheduleExactAlarms(context)
        _hasPhoneStatePermission.value = PermissionHelper.hasPhoneStatePermission(context)
    }

    fun refreshPauseState() {
        _isAppPaused.value = prefs.isAppPaused()
        _remainingPauseMillis.value = prefs.getRemainingPauseMillis()
    }

    fun setServiceEnabled(enabled: Boolean) {
        prefs.isServiceEnabled = enabled
        _isServiceEnabled.value = enabled
        val context = getApplication<Application>()
        if (enabled) {
            DhikrForegroundService.start(context)
        } else {
            DhikrForegroundService.stop(context)
        }
    }

    fun setManualCooldown(value: Int, unit: String) {
        val validValue = value.coerceAtLeast(0)
        prefs.cooldownValue = validValue
        prefs.cooldownUnit = unit
        _cooldownValue.value = validValue
        _cooldownUnit.value = unit
        _cooldownMinutes.value = prefs.cooldownMinutes
    }

    fun pauseApp(value: Int, unit: String) {
        prefs.pauseApp(value, unit)
        refreshPauseState()
    }

    fun resumeAppNow() {
        prefs.resumeAppNow()
        refreshPauseState()
    }

    fun setCooldownMinutes(minutes: Int) {
        setManualCooldown(minutes, "MINUTES")
    }

    fun setTriggerOnUnlock(enabled: Boolean) {
        prefs.triggerOnUnlock = enabled
        _triggerOnUnlock.value = enabled
    }

    fun setTriggerOnPeriodicTimer(enabled: Boolean) {
        prefs.triggerOnPeriodicTimer = enabled
        _triggerOnPeriodicTimer.value = enabled
        val context = getApplication<Application>()
        if (enabled) {
            DhikrForegroundService.scheduleNextAlarm(context)
        } else {
            DhikrForegroundService.cancelAlarm(context)
        }
    }

    fun setPeriodicIntervalMinutes(minutes: Int) {
        prefs.periodicIntervalMinutes = minutes
        _periodicIntervalMinutes.value = minutes
        val context = getApplication<Application>()
        if (prefs.triggerOnPeriodicTimer) {
            DhikrForegroundService.scheduleNextAlarm(context)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.isVibrationEnabled = enabled
        _isVibrationEnabled.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.isSoundEnabled = enabled
        _isSoundEnabled.value = enabled
    }

    fun setAppOpenTriggerEnabled(enabled: Boolean) {
        prefs.isAppOpenTriggerEnabled = enabled
        _isAppOpenTriggerEnabled.value = enabled
    }

    fun setAppOpenTargetMode(mode: com.example.data.model.TargetMode) {
        prefs.appOpenTargetMode = mode
        _appOpenTargetMode.value = mode
    }

    fun setAppOpenTargetCount(count: Int) {
        val valid = count.coerceIn(1, 1000)
        prefs.appOpenTargetCount = valid
        _appOpenTargetCount.value = valid
    }

    fun setAppOpenTargetTimeSeconds(seconds: Int) {
        val valid = seconds.coerceIn(5, 3600)
        prefs.appOpenTargetTimeSeconds = valid
        _appOpenTargetTimeSeconds.value = valid
    }

    fun setAppOpenCooldownMinutes(minutes: Int) {
        val valid = minutes.coerceIn(0, 1440)
        prefs.appOpenCooldownMinutes = valid
        _appOpenCooldownMinutes.value = valid
    }

    fun toggleAppOpenMonitoredPackage(packageName: String) {
        val current = prefs.appOpenMonitoredPackages.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        prefs.appOpenMonitoredPackages = current
        _appOpenMonitoredPackages.value = current
    }

    fun setAppOpenMonitoredPackages(packages: Set<String>) {
        prefs.appOpenMonitoredPackages = packages
        _appOpenMonitoredPackages.value = packages
    }

    fun toggleDhikrEnabled(item: DhikrItem) {
        viewModelScope.launch {
            val updated = item.copy(isEnabled = !item.isEnabled)
            repository.updateDhikr(updated)
            val context = getApplication<Application>()
            if (updated.isEnabled) {
                com.example.util.DhikrAlarmScheduler.scheduleNextAlarmForDhikr(context, updated.id, forceReset = true)
            } else {
                com.example.util.DhikrAlarmScheduler.cancelAlarmForDhikr(context, updated.id)
            }
        }
    }

    fun saveDhikr(item: DhikrItem) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            if (item.id == 0L) {
                val newId = repository.insertDhikr(item)
                if (item.isEnabled) {
                    com.example.util.DhikrAlarmScheduler.scheduleNextAlarmForDhikr(context, newId, forceReset = true)
                }
            } else {
                repository.updateDhikr(item)
                if (item.isEnabled) {
                    com.example.util.DhikrAlarmScheduler.scheduleNextAlarmForDhikr(context, item.id, forceReset = true)
                } else {
                    com.example.util.DhikrAlarmScheduler.cancelAlarmForDhikr(context, item.id)
                }
            }
        }
    }

    fun deleteDhikr(item: DhikrItem) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            com.example.util.DhikrAlarmScheduler.cancelAlarmForDhikr(context, item.id)
            com.example.util.DhikrTriggerManager.cancelReminderNotification(context, item.id)
            prefs.removePendingTimerDhikr(item.id)
            repository.deleteDhikr(item)
        }
    }

    fun clearAllStatistics() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun setMandatoryMorningEnabled(enabled: Boolean) {
        prefs.isMandatoryMorningAzkarEnabled = enabled
        _isMandatoryMorningAzkarEnabled.value = enabled
    }

    fun setMorningHours(startHour: Int, endHour: Int) {
        prefs.morningStartHour = startHour
        prefs.morningEndHour = endHour
        _morningStartHour.value = startHour
        _morningEndHour.value = endHour
    }

    fun setMandatoryEveningEnabled(enabled: Boolean) {
        prefs.isMandatoryEveningAzkarEnabled = enabled
        _isMandatoryEveningAzkarEnabled.value = enabled
    }

    fun setEveningHours(startHour: Int, endHour: Int) {
        prefs.eveningStartHour = startHour
        prefs.eveningEndHour = endHour
        _eveningStartHour.value = startHour
        _eveningEndHour.value = endHour
    }

    fun getAppOpenRule(packageName: String): com.example.data.prefs.AppOpenRule {
        return prefs.getAppOpenRule(packageName)
    }

    fun saveAppOpenRule(rule: com.example.data.prefs.AppOpenRule) {
        prefs.saveAppOpenRule(rule)
        _appOpenMonitoredPackages.value = prefs.appOpenMonitoredPackages
    }
}
