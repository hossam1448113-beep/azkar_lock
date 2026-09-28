package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DhikrApplication
import com.example.ui.MainViewModel
import com.example.ui.overlay.DhikrLockActivity
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.PureWhite

@Composable
fun MandatoryAzkarSettingsCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as DhikrApplication
    val prefs = app.preferences

    val isMorningEnabled by viewModel.isMandatoryMorningAzkarEnabled.collectAsState()
    val morningStartHour by viewModel.morningStartHour.collectAsState()
    val morningEndHour by viewModel.morningEndHour.collectAsState()

    val isEveningEnabled by viewModel.isMandatoryEveningAzkarEnabled.collectAsState()
    val eveningStartHour by viewModel.eveningStartHour.collectAsState()
    val eveningEndHour by viewModel.eveningEndHour.collectAsState()

    val isMorningDoneToday = remember(prefs.lastCompletedMorningDate) {
        prefs.lastCompletedMorningDate == prefs.getTodayDateString()
    }
    val isEveningDoneToday = remember(prefs.lastCompletedEveningDate) {
        prefs.lastCompletedEveningDate == prefs.getTodayDateString()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldMetallic.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldMetallic.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = GoldMetallic,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "أذكار الصباح والمساء الإلزامية",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ظهور إجباري كامل عند فتح الهاتف في الوقت المحدد",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Morning Azkar Section
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, if (isMorningEnabled) GoldMetallic.copy(alpha = 0.3f) else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = GoldLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أذكار الصباح الإلزامية",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isMorningEnabled,
                            onCheckedChange = { viewModel.setMandatoryMorningEnabled(it) }
                        )
                    }

                    if (isMorningEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "نافذة وقت الظهور: من الساعة ${formatHour(morningStartHour)} حتى الساعة ${formatHour(morningEndHour)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "بداية الوقت: ${formatHour(morningStartHour)}", fontSize = 11.sp)
                        Slider(
                            value = morningStartHour.toFloat(),
                            onValueChange = { viewModel.setMorningHours(it.toInt(), morningEndHour) },
                            valueRange = 0f..12f,
                            steps = 11,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(text = "نهاية الوقت: ${formatHour(morningEndHour)}", fontSize = 11.sp)
                        Slider(
                            value = morningEndHour.toFloat(),
                            onValueChange = { viewModel.setMorningHours(morningStartHour, it.toInt()) },
                            valueRange = 6f..15f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Status & Manual test button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = if (isMorningDoneToday) "✓ أتممت أذكار الصباح اليوم" else "في انتظار فتح الهاتف خلال الوقت",
                                fontSize = 11.sp,
                                color = if (isMorningDoneToday) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(context, DhikrLockActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        putExtra(DhikrLockActivity.EXTRA_TRIGGER_SOURCE, "morning_azkar")
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("قراءة الآن", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Evening Azkar Section
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, if (isEveningEnabled) Emerald500.copy(alpha = 0.3f) else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Nightlight, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أذكار المساء الإلزامية",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isEveningEnabled,
                            onCheckedChange = { viewModel.setMandatoryEveningEnabled(it) }
                        )
                    }

                    if (isEveningEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "نافذة وقت الظهور: من الساعة ${formatHour(eveningStartHour)} حتى الساعة ${formatHour(eveningEndHour)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald700
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "بداية الوقت: ${formatHour(eveningStartHour)}", fontSize = 11.sp)
                        Slider(
                            value = eveningStartHour.toFloat(),
                            onValueChange = { viewModel.setEveningHours(it.toInt(), eveningEndHour) },
                            valueRange = 12f..20f,
                            steps = 7,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(text = "نهاية الوقت: ${formatHour(eveningEndHour)}", fontSize = 11.sp)
                        Slider(
                            value = eveningEndHour.toFloat(),
                            onValueChange = { viewModel.setEveningHours(eveningStartHour, it.toInt()) },
                            valueRange = 17f..23f,
                            steps = 5,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Status & Manual test button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = if (isEveningDoneToday) "✓ أتممت أذكار المساء اليوم" else "في انتظار فتح الهاتف خلال الوقت",
                                fontSize = 11.sp,
                                color = if (isEveningDoneToday) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(context, DhikrLockActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        putExtra(DhikrLockActivity.EXTRA_TRIGGER_SOURCE, "evening_azkar")
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("قراءة الآن", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val period = if (hour < 12) "صباحاً" else "مساءً"
    return "$h:00 $period"
}
