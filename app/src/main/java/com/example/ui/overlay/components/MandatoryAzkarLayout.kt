package com.example.ui.overlay.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MandatoryDhikr
import com.example.data.model.MorningEveningAzkarData
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay

/**
 * MANDATORY MORNING & EVENING AZKAR OVERLAY LAYOUT.
 * Strictly presents complete Morning / Evening Azkar in an elegant, legible interface.
 * Shows the full vowelled Arabic text, hadith virtue, interactive tasbeeh counter,
 * and tracks progress sequentially until all azkar in the session are finished.
 */
@Composable
fun MandatoryAzkarLayout(
    isMorning: Boolean,
    onAllCompleted: () -> Unit,
    onVibrate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val azkarList = remember(isMorning) {
        if (isMorning) MorningEveningAzkarData.getMorningAzkar() else MorningEveningAzkarData.getEveningAzkar()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var currentItemCount by remember { mutableIntStateOf(0) }
    var isSessionFinished by remember { mutableStateOf(false) }

    val currentDhikr = azkarList.getOrNull(currentIndex)
    val totalAzkar = azkarList.size
    val progress = if (totalAzkar > 0) (currentIndex + (if (currentDhikr != null && currentDhikr.targetCount > 0) currentItemCount.toFloat() / currentDhikr.targetCount else 0f)) / totalAzkar else 1f

    val title = if (isMorning) "أذكار الصباح المباركة (إلزامية)" else "أذكار المساء المباركة (إلزامية)"
    val icon = if (isMorning) Icons.Default.WbSunny else Icons.Default.Nightlight
    val headerAccent = if (isMorning) GoldLight else Emerald300

    LaunchedEffect(currentIndex) {
        currentItemCount = 0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Islamic Ornament & Session Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = headerAccent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = headerAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Subtitle & Progress
            Text(
                text = if (!isSessionFinished) "الذكر ${currentIndex + 1} من إجمالي $totalAzkar أذكار" else "تم إتمام جميع الأذكار بنجاح!",
                color = PureWhite.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = headerAccent,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isSessionFinished && currentDhikr != null) {
                // Dhikr Content Area in Scrollable Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(Emerald500.copy(alpha = 0.5f), headerAccent.copy(alpha = 0.4f)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Dhikr Title Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Emerald900.copy(alpha = 0.6f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(GoldMetallic, Emerald400))
                                )
                            ) {
                                Text(
                                    text = currentDhikr.title,
                                    color = GoldLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Full Arabic Text (with Tashkeel)
                            Text(
                                text = currentDhikr.arabicText,
                                color = PureWhite,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 36.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp)
                            )

                            if (currentDhikr.virtue.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Emerald800.copy(alpha = 0.25f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "📖 الفضل: ${currentDhikr.virtue}",
                                        color = Emerald300,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Tasbeeh Circular Counter Button
                        val itemRemaining = (currentDhikr.targetCount - currentItemCount).coerceAtLeast(0)
                        val itemProgress = if (currentDhikr.targetCount > 0) currentItemCount.toFloat() / currentDhikr.targetCount else 0f

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onVibrate(true)
                                    val nextCount = currentItemCount + 1
                                    if (nextCount >= currentDhikr.targetCount) {
                                        currentItemCount = currentDhikr.targetCount
                                        onVibrate(false)
                                        if (currentIndex + 1 < totalAzkar) {
                                            currentIndex++
                                        } else {
                                            isSessionFinished = true
                                        }
                                    } else {
                                        currentItemCount = nextCount
                                    }
                                }
                                .testTag("mandatory_dhikr_tasbeeh_btn")
                        ) {
                            // Circular Progress Ring
                            Canvas(modifier = Modifier.size(126.dp)) {
                                drawCircle(
                                    color = DarkCanvas.copy(alpha = 0.8f),
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(listOf(Emerald400, headerAccent, Emerald500)),
                                    startAngle = -90f,
                                    sweepAngle = 360f * itemProgress,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentItemCount / ${currentDhikr.targetCount}",
                                    color = headerAccent,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (itemRemaining > 0) "اضغط للتسبيح" else "تم الذكر ✓",
                                    color = PureWhite.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Previous / Next Navigation Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                    }
                                },
                                enabled = currentIndex > 0,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("السابق", fontSize = 13.sp)
                            }

                            Text(
                                text = "المتبقي: $itemRemaining",
                                color = PureWhite.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Button(
                                onClick = {
                                    // Skip to next if count satisfied or advance
                                    if (currentItemCount >= currentDhikr.targetCount) {
                                        if (currentIndex + 1 < totalAzkar) {
                                            currentIndex++
                                        } else {
                                            isSessionFinished = true
                                        }
                                    } else {
                                        // Count one
                                        onVibrate(true)
                                        val nextCount = currentItemCount + 1
                                        if (nextCount >= currentDhikr.targetCount) {
                                            currentItemCount = currentDhikr.targetCount
                                            onVibrate(false)
                                            if (currentIndex + 1 < totalAzkar) {
                                                currentIndex++
                                            } else {
                                                isSessionFinished = true
                                            }
                                        } else {
                                            currentItemCount = nextCount
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                            ) {
                                Text(if (currentItemCount >= currentDhikr.targetCount) "التالي" else "سبّح", fontSize = 13.sp, color = PureWhite)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            } else {
                // Completed Celebration View
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(GoldMetallic, Emerald400))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(76.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "هنيئاً لك ذكر الله!",
                            color = GoldLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isMorning)
                                "أتممت أذكار الصباح كاملة بحمد الله وفضله.\nحفظك الله ورعاك وبارك لك في يومك."
                            else
                                "أتممت أذكار المساء كاملة بحمد الله وفضله.\nجعلك الله في حفظه وأمانه حتى تصبح.",
                            color = PureWhite,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = onAllCompleted,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldMetallic),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = DarkCanvas)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إتمام وفتح قفل الهاتف",
                                color = DarkCanvas,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

val Emerald600 = Color(0xFF059669)
val Emerald900 = Color(0xFF064E3B)
