package com.example.ui.overlay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.example.ui.overlay.components.AppOpenDhikrLayout
import com.example.ui.overlay.components.LockScreenDhikrLayout
import com.example.ui.overlay.components.MandatoryAzkarLayout
import com.example.ui.overlay.components.ScheduledDhikrLayout
import com.example.util.AppInfoHelper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DhikrApplication
import com.example.data.db.AppDatabase
import com.example.data.model.DhikrItem
import com.example.data.model.TargetMode
import com.example.service.DhikrAccessibilityService
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.util.DhikrAlarmScheduler
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Non-bypassable Dhikr Lock Screen Activity.
 * Strictly enforces daily remembrance without skip buttons or emergency loopholes.
 * Supports sequential Multi-Dhikr queue, exact per-dhikr timers, and persistence across reboots.
 */
class DhikrLockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TRIGGER_SOURCE = "extra_trigger_source"
        const val EXTRA_DHIKR_ID = "extra_dhikr_id"
        const val EXTRA_QUEUE_IDS = "extra_queue_ids"
        const val EXTRA_APP_OPEN_PACKAGE = "extra_app_open_package"
        const val EXTRA_CUSTOM_TARGET_MODE = "extra_custom_target_mode"
        const val EXTRA_CUSTOM_COUNT = "extra_custom_count"
        const val EXTRA_CUSTOM_TIME = "extra_custom_time"

        @Volatile
        var isActivityVisible: Boolean = false
    }

    private var isAllCompleted = false
    private var appOpenPackage: String = ""

    private var screenOffReceiverRegistered = false
    private val screenOffReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                // When power button is pressed, let the screen remain off and black
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isActivityVisible = true

        // Register screen off listener
        try {
            registerReceiver(screenOffReceiver, android.content.IntentFilter(Intent.ACTION_SCREEN_OFF))
            screenOffReceiverRegistered = true
        } catch (e: Exception) {
            // Ignore
        }

        // Setup lock-screen window flags to display on top of keyguard without forcing screen on
        setupWindowFlags()

        // Inform Accessibility Service that Dhikr Lock is active to block system navigation
        DhikrAccessibilityService.isDhikrLockActive = true

        val triggerSource = intent.getStringExtra(EXTRA_TRIGGER_SOURCE) ?: "unlock"
        val specificDhikrId = intent.getLongExtra(EXTRA_DHIKR_ID, -1L)
        val queueIds = intent.getLongArrayExtra(EXTRA_QUEUE_IDS)
        appOpenPackage = intent.getStringExtra(EXTRA_APP_OPEN_PACKAGE) ?: ""
        val customTargetMode = intent.getStringExtra(EXTRA_CUSTOM_TARGET_MODE)
        val customCount = intent.getIntExtra(EXTRA_CUSTOM_COUNT, -1)
        val customTime = intent.getIntExtra(EXTRA_CUSTOM_TIME, -1)

        // Intercept Back Press: strictly prevent bypassing until all goals in the queue are reached
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAllCompleted) {
                    dismissAndFinish()
                } else {
                    vibrateFeedback(short = true)
                    Toast.makeText(
                        this@DhikrLockActivity,
                        "🔒 يرجى إتمام الأذكار المطلوبة لفتح قفل الشاشة",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    DhikrLockScreen(
                        triggerSource = triggerSource,
                        specificDhikrId = specificDhikrId,
                        queueIds = queueIds,
                        appOpenPackage = appOpenPackage,
                        customTargetMode = customTargetMode,
                        customCount = customCount,
                        customTime = customTime,
                        onAllCompleted = {
                            isAllCompleted = true
                            dismissAndFinish()
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isActivityVisible = true
        DhikrAccessibilityService.isDhikrLockActive = true
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        DhikrAccessibilityService.isDhikrLockActive = true
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            isActivityVisible = false
            DhikrAccessibilityService.isDhikrLockActive = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            isActivityVisible = false
            DhikrAccessibilityService.isDhikrLockActive = false
        }
    }

    override fun onDestroy() {
        isActivityVisible = false
        DhikrAccessibilityService.isDhikrLockActive = false
        if (screenOffReceiverRegistered) {
            try {
                unregisterReceiver(screenOffReceiver)
            } catch (e: Exception) {
                // Ignore
            }
            screenOffReceiverRegistered = false
        }
        super.onDestroy()
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun dismissAndFinish() {
        isActivityVisible = false
        DhikrAccessibilityService.isDhikrLockActive = false
        val app = applicationContext as DhikrApplication
        app.preferences.clearPendingTimerDhikrs()
        app.preferences.clearActiveSession()
        app.preferences.markOverlayShownNow()

        if (appOpenPackage.isNotEmpty()) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(appOpenPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    startActivity(launchIntent)
                }
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
        finish()
    }

    private fun vibrateFeedback(short: Boolean) {
        val app = applicationContext as DhikrApplication
        if (!app.preferences.isVibrationEnabled) return

        try {
            val duration = if (short) 40L else 120L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(duration)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }
}

@Composable
fun DhikrLockScreen(
    triggerSource: String,
    specificDhikrId: Long,
    queueIds: LongArray? = null,
    appOpenPackage: String = "",
    customTargetMode: String? = null,
    customCount: Int = -1,
    customTime: Int = -1,
    onAllCompleted: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as DhikrApplication
    val repository = app.repository
    val prefs = app.preferences

    if (triggerSource == "morning_azkar") {
        MandatoryAzkarLayout(
            isMorning = true,
            onAllCompleted = {
                prefs.markMorningAzkarCompletedToday()
                onAllCompleted()
            },
            onVibrate = { short ->
                if (prefs.isVibrationEnabled) {
                    val duration = if (short) 35L else 110L
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator?.vibrate(
                            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(duration)
                    }
                }
            }
        )
        return
    }

    if (triggerSource == "evening_azkar") {
        MandatoryAzkarLayout(
            isMorning = false,
            onAllCompleted = {
                prefs.markEveningAzkarCompletedToday()
                onAllCompleted()
            },
            onVibrate = { short ->
                if (prefs.isVibrationEnabled) {
                    val duration = if (short) 35L else 110L
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator?.vibrate(
                            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(duration)
                    }
                }
            }
        )
        return
    }

    var dhikrQueue by remember { mutableStateOf<List<DhikrItem>>(emptyList()) }
    var currentQueueIndex by remember { mutableIntStateOf(0) }
    var currentCount by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isQueueLoaded by remember { mutableStateOf(false) }
    var isCurrentDhikrTransitioning by remember { mutableStateOf(false) }

    var showSessionSummaryDialog by remember { mutableStateOf(false) }
    var summaryCount by remember { mutableIntStateOf(0) }
    var summarySeconds by remember { mutableIntStateOf(0) }
    var summaryDhikrText by remember { mutableStateOf("") }

    // Load Dhikr Queue & Restore Persistent State
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<DhikrItem>()

            if (triggerSource == "boot_restore" && prefs.hasActiveSession) {
                // Restore in-progress session across reboots
                val savedIds = prefs.getActiveSessionQueueIds()
                for (id in savedIds) {
                    val item = repository.getDhikrById(id)
                    if (item != null) list.add(item)
                }
                currentQueueIndex = prefs.activeSessionQueueIndex.coerceIn(0, (list.size - 1).coerceAtLeast(0))
                currentCount = prefs.activeSessionCurrentCount
                elapsedSeconds = prefs.activeSessionElapsedSeconds
            } else if (queueIds != null && queueIds.isNotEmpty()) {
                // Unified sequential queue from DhikrTriggerManager (e.g. pending cycle timer Dhikrs + unlock Dhikrs)
                for (id in queueIds) {
                    val item = repository.getDhikrById(id)
                    if (item != null) list.add(item)
                }
            } else if (specificDhikrId > 0) {
                // Single specific Dhikr triggered by periodic timer or dedicated session
                val item = repository.getDhikrById(specificDhikrId)
                if (item != null) {
                    list.add(item)
                }
            } else {
                // Phone Unlock Event: Load ALL active unlock Dhikrs for sequential queue
                val unlockList = app.database.dhikrDao().getUnlockDhikrsSync()
                if (unlockList.isNotEmpty()) {
                    list.addAll(unlockList)
                }
            }

            // Deduplicate items to guarantee zero overlap and no repeated entries in the same session
            val distinctList = list.distinctBy { it.id }.toMutableList()

            // Fallback if list is empty
            if (distinctList.isEmpty()) {
                val activeList = app.database.dhikrDao().getActiveDhikrsSync()
                if (activeList.isNotEmpty()) {
                    distinctList.addAll(activeList.distinctBy { it.id })
                } else {
                    distinctList.add(AppDatabase.DEFAULT_PRESETS.first())
                }
            }

            // Apply custom targets if passed from dedicated session mode
            if (customTargetMode != null && distinctList.isNotEmpty()) {
                val first = distinctList.first()
                val mode = try {
                    TargetMode.valueOf(customTargetMode)
                } catch (e: Exception) {
                    first.targetMode
                }
                val count = if (customCount > 0) customCount else first.targetCount
                val time = if (customTime > 0) customTime else first.targetTimeSeconds
                distinctList[0] = first.copy(
                    targetMode = mode,
                    targetCount = count,
                    targetTimeSeconds = time
                )
            }

            dhikrQueue = distinctList
            isQueueLoaded = true

            // Save initial active session in persistent storage
            prefs.saveActiveSession(
                source = triggerSource,
                queueIds = distinctList.map { it.id },
                queueIndex = currentQueueIndex,
                currentCount = currentCount,
                elapsedSeconds = elapsedSeconds
            )
        }
    }

    if (!isQueueLoaded || dhikrQueue.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                color = GoldMetallic,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    val activeDhikr = dhikrQueue.getOrElse(currentQueueIndex) { dhikrQueue.first() }
    val targetCount = activeDhikr.targetCount.coerceAtLeast(1)
    val targetSeconds = activeDhikr.targetTimeSeconds.coerceAtLeast(1)

    // Completion criteria
    val isCountSatisfied = when (activeDhikr.targetMode) {
        TargetMode.TIME_ONLY -> true
        else -> currentCount >= targetCount
    }

    val isTimeSatisfied = when (activeDhikr.targetMode) {
        TargetMode.COUNT_ONLY -> true
        else -> elapsedSeconds >= targetSeconds
    }

    val goalMet = isCountSatisfied && isTimeSatisfied

    // Time-based ticker
    LaunchedEffect(currentQueueIndex, isCurrentDhikrTransitioning) {
        if (!isCurrentDhikrTransitioning && activeDhikr.targetMode != TargetMode.COUNT_ONLY) {
            while (!isTimeSatisfied && !isCurrentDhikrTransitioning) {
                delay(1000L)
                elapsedSeconds++
                prefs.updateActiveSessionProgress(currentCount, elapsedSeconds, currentQueueIndex)
            }
        }
    }

    // Goal satisfaction & Queue Progression
    LaunchedEffect(goalMet) {
        if (goalMet && !isCurrentDhikrTransitioning) {
            isCurrentDhikrTransitioning = true

            val countToRecord = if (activeDhikr.targetMode == TargetMode.TIME_ONLY && currentCount == 0) 1 else currentCount
            val secsToRecord = elapsedSeconds.coerceAtLeast(1)

            // Record completion statistics in Room
            withContext(Dispatchers.IO) {
                repository.recordCompletion(
                    dhikrId = activeDhikr.id,
                    dhikrText = activeDhikr.arabicText,
                    count = countToRecord,
                    seconds = secsToRecord
                )
                // Schedule the NEXT alarm for this Dhikr strictly AFTER completion (exact cycle precision)
                DhikrAlarmScheduler.scheduleNextAlarmForDhikr(context, activeDhikr.id, forceReset = true)
                // Remove from pending timer list and cancel reminder notification if any
                prefs.removePendingTimerDhikr(activeDhikr.id)
                com.example.util.DhikrTriggerManager.cancelReminderNotification(context, activeDhikr.id)
            }

            if (triggerSource == "dedicated_session") {
                prefs.saveLastSessionReport(activeDhikr.arabicText, countToRecord, secsToRecord)
                summaryDhikrText = activeDhikr.arabicText
                summaryCount = countToRecord
                summarySeconds = secsToRecord
            }

            if (currentQueueIndex + 1 < dhikrQueue.size) {
                // Advance to next Dhikr in queue
                delay(400L)
                currentQueueIndex++
                currentCount = 0
                elapsedSeconds = 0
                isCurrentDhikrTransitioning = false
                prefs.updateActiveSessionProgress(0, 0, currentQueueIndex)
            } else {
                // Entire queue completed!
                if (triggerSource == "dedicated_session") {
                    showSessionSummaryDialog = true
                } else {
                    delay(300L)
                    prefs.clearPendingTimerDhikrs()
                    prefs.clearActiveSession()
                    prefs.markOverlayShownNow()
                    onAllCompleted()
                }
            }
        }
    }

    val onTapIncrement = {
        if (!goalMet && !isCurrentDhikrTransitioning && activeDhikr.targetMode != TargetMode.TIME_ONLY) {
            currentCount++
            prefs.updateActiveSessionProgress(currentCount, elapsedSeconds, currentQueueIndex)
            if (prefs.isVibrationEnabled) {
                vibrateSingleTap(context)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (triggerSource) {
            "app_open" -> {
                val appName = remember(appOpenPackage) {
                    if (appOpenPackage.isNotEmpty()) AppInfoHelper.getAppName(context, appOpenPackage) else "التطبيق"
                }
                AppOpenDhikrLayout(
                    appName = appName,
                    activeDhikr = activeDhikr,
                    targetMode = activeDhikr.targetMode,
                    currentCount = currentCount,
                    targetCount = targetCount,
                    elapsedSeconds = elapsedSeconds,
                    targetSeconds = targetSeconds,
                    cooldownMinutes = prefs.appOpenCooldownMinutes,
                    goalMet = goalMet,
                    onTapIncrement = onTapIncrement
                )
            }
            "timer", "notification_click" -> {
                ScheduledDhikrLayout(
                    activeDhikr = activeDhikr,
                    currentCount = currentCount,
                    targetCount = targetCount,
                    elapsedSeconds = elapsedSeconds,
                    targetSeconds = targetSeconds,
                    goalMet = goalMet,
                    onTapIncrement = onTapIncrement
                )
            }
            else -> {
                LockScreenDhikrLayout(
                    activeDhikr = activeDhikr,
                    queueIndex = currentQueueIndex,
                    queueTotal = dhikrQueue.size,
                    currentCount = currentCount,
                    targetCount = targetCount,
                    elapsedSeconds = elapsedSeconds,
                    targetSeconds = targetSeconds,
                    goalMet = goalMet,
                    onTapIncrement = onTapIncrement
                )
            }
        }

        // Dedicated Mode: Session Completion Summary Dialog
        if (showSessionSummaryDialog) {
            val speed = if (summarySeconds > 0) {
                (summaryCount.toDouble() / (summarySeconds.toDouble() / 60.0))
            } else 0.0
            val formattedSpeed = String.format(Locale.getDefault(), "%.1f", speed)
            val formattedTime = if (summarySeconds < 60) {
                "$summarySeconds ثانية"
            } else {
                val mins = summarySeconds / 60
                val secs = summarySeconds % 60
                if (secs > 0) "$mins دقيقة و $secs ثانية" else "$mins دقيقة"
            }

            AlertDialog(
                onDismissRequest = {
                    prefs.clearActiveSession()
                    onAllCompleted()
                },
                title = {
                    Text(
                        text = "تقبل الله طاعتكم 🌿",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Emerald500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "تقرير جلسة الذكر المكتملة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = summaryDhikrText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoldMetallic,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("العدد المنجز:", fontSize = 13.sp)
                                    Text("$summaryCount تسبيحة", fontWeight = FontWeight.Bold, color = Emerald500)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الوقت المستغرق:", fontSize = 13.sp)
                                    Text(formattedTime, fontWeight = FontWeight.Bold, color = Emerald500)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("متوسط السرعة:", fontSize = 13.sp)
                                    Text("$formattedSpeed تسبيحة/دقيقة", fontWeight = FontWeight.Bold, color = GoldDark)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            prefs.clearActiveSession()
                            onAllCompleted()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إتمام وإنهاء الجلسة المباركة", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

private fun vibrateSingleTap(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(35L)
        }
    } catch (e: Exception) {
        // Ignore
    }
}
