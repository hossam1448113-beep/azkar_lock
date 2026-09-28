package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Completion mode:
 * - COUNT_ONLY: Complete by tapping target number of times
 * - TIME_ONLY: Complete by waiting/reciting for target seconds
 * - COUNT_AND_TIME: Complete both target repetitions AND target duration
 */
enum class TargetMode {
    COUNT_ONLY,
    TIME_ONLY,
    COUNT_AND_TIME
}

/**
 * Trigger mode:
 * - ON_UNLOCK: Trigger overlay when screen is unlocked
 * - PERIODIC_TIMER: Trigger periodically (every X minutes)
 * - BOTH: Both on unlock and periodic timer
 * - SCHEDULED_TIME: Trigger at a specific time of day on selected days of the week
 */
enum class TriggerMode {
    ON_UNLOCK,
    PERIODIC_TIMER,
    BOTH,
    SCHEDULED_TIME
}

enum class PeriodicTimeUnit(val displayNameArabic: String, val multiplierMinutes: Long) {
    MINUTES("دقائق", 1L),
    HOURS("ساعات", 60L),
    DAYS("أيام", 1440L)
}

@Entity(tableName = "dhikr_items")
data class DhikrItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val arabicText: String,
    val virtue: String = "",
    val targetMode: TargetMode = TargetMode.COUNT_ONLY,
    val targetCount: Int = 33,
    val targetTimeSeconds: Int = 30,
    val triggerMode: TriggerMode = TriggerMode.ON_UNLOCK,
    val timerIntervalMinutes: Int = 20,
    val timerValue: Int = 20,
    val timerUnit: PeriodicTimeUnit = PeriodicTimeUnit.MINUTES,
    val isEnabled: Boolean = true,
    val totalLifetimeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledHour: Int = 12,
    val scheduledMinute: Int = 0,
    val scheduledDaysOfWeek: String = "1,2,3,4,5,6,7" // 1=Sunday, 2=Monday, ..., 7=Saturday
) {
    fun getTotalMinutesInterval(): Long {
        return (timerValue * timerUnit.multiplierMinutes).coerceAtLeast(1L)
    }

    fun getDaysOfWeekSet(): Set<Int> {
        return scheduledDaysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
            .ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }
    }
}
