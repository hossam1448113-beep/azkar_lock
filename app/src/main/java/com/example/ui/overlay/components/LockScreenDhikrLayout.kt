package com.example.ui.overlay.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrItem
import com.example.data.model.TargetMode
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.PureWhite

/**
 * ISOLATED LAYOUT 1: Dedicated Lock Screen Azkar Display.
 * Optimized specifically for device lock and unlock enforcement.
 * Features a sacred, dignifying Islamic aesthetic with lock status badges,
 * sequential queue tracking, and prominent circular counter rings.
 */
@Composable
fun LockScreenDhikrLayout(
    activeDhikr: DhikrItem,
    queueIndex: Int,
    queueTotal: Int,
    currentCount: Int,
    targetCount: Int,
    elapsedSeconds: Int,
    targetSeconds: Int,
    goalMet: Boolean,
    onTapIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val countFraction = if (targetCount > 0) (currentCount.toFloat() / targetCount).coerceIn(0f, 1f) else 0f
    val timeFraction = if (targetSeconds > 0) (elapsedSeconds.toFloat() / targetSeconds).coerceIn(0f, 1f) else 0f

    val primaryProgress = when (activeDhikr.targetMode) {
        TargetMode.COUNT_ONLY -> countFraction
        TargetMode.TIME_ONLY -> timeFraction
        TargetMode.COUNT_AND_TIME -> (countFraction + timeFraction) / 2f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkCanvas,
                        Color(0xFF041810),
                        DarkCanvas
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Lock State & Queue Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sacred Lock Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Emerald900.copy(alpha = 0.7f))
                        .border(1.dp, GoldMetallic.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "قفل الشاشة",
                        tint = GoldMetallic,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "شاشة قفل الأذكار • أكمل الورد لفتح الهاتف",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Sequential Queue Dots / Indicator
                if (queueTotal > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "الذكر ${queueIndex + 1} من $queueTotal",
                            color = Emerald300,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until queueTotal) {
                                Box(
                                    modifier = Modifier
                                        .size(if (i == queueIndex) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (i == queueIndex) GoldMetallic
                                            else if (i < queueIndex) Emerald400
                                            else Emerald800.copy(alpha = 0.5f)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Dhikr Card (Islamic Arch Style)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.5.dp, GoldMetallic.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "﴿ بذكر الله تطمئن القلوب ﴾",
                        color = GoldMetallic,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = activeDhikr.arabicText,
                        color = PureWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    if (activeDhikr.virtue.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = activeDhikr.virtue,
                            color = Emerald300.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Circular Prayer-Bead Counter Dial
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(230.dp)
                    .scale(if (goalMet) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Emerald900.copy(alpha = 0.8f),
                                DarkSurface
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !goalMet
                    ) {
                        onTapIncrement()
                    }
                    .testTag("lock_screen_tasbih_dial")
            ) {
                // Progress Circle Canvas
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val strokeWidth = 10.dp.toPx()

                    // Background Track
                    drawCircle(
                        color = Color(0xFF132B20),
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Progress Arc
                    val sweepAngle = primaryProgress * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Emerald500, GoldMetallic, GoldLight, Emerald500)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center Content: Count or Time or Goal Met
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (goalMet) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "اكتمل الذكر",
                            tint = GoldMetallic,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تقبّل الله 🌿",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        when (activeDhikr.targetMode) {
                            TargetMode.COUNT_ONLY -> {
                                Text(
                                    text = "$currentCount",
                                    color = PureWhite,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "الهدف: $targetCount",
                                    color = GoldLight,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "اضغط للتسبيح",
                                    color = Emerald300,
                                    fontSize = 11.sp
                                )
                            }
                            TargetMode.TIME_ONLY -> {
                                Text(
                                    text = "${(targetSeconds - elapsedSeconds).coerceAtLeast(0)}s",
                                    color = PureWhite,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "الوقت: $targetSeconds ثانية",
                                    color = GoldLight,
                                    fontSize = 14.sp
                                )
                            }
                            TargetMode.COUNT_AND_TIME -> {
                                Text(
                                    text = "$currentCount / $targetCount",
                                    color = PureWhite,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${elapsedSeconds}s / ${targetSeconds}s",
                                    color = GoldMetallic,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "تكرار + مدة زمنية",
                                    color = Emerald300,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer instructions
            Text(
                text = if (goalMet) "جاري فتح القفل والمتابعة..." else "لا يمكن تجاوز شاشة الذكر حتى إتمام الورد المطلوب",
                color = if (goalMet) GoldLight else Emerald300.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
