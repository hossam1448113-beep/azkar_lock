package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrHistory
import com.example.data.model.DhikrItem
import com.example.ui.MainViewModel
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allHistoryList by viewModel.allHistory.collectAsState()
    val allDhikrs by viewModel.allDhikrs.collectAsState()
    var selectedDhikrId by remember { mutableStateOf<Long?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Filter history by selected Dhikr if any
    val historyList = remember(allHistoryList, selectedDhikrId) {
        if (selectedDhikrId == null) {
            allHistoryList
        } else {
            allHistoryList.filter { it.dhikrId == selectedDhikrId }
        }
    }

    // Calculate time boundaries
    val calendar = currentCalendar()
    val startOfToday = calendar.startOfDay().timeInMillis
    val startOfYesterday = calendar.subtractDays(1).startOfDay().timeInMillis
    val startOfDayBeforeYesterday = calendar.subtractDays(2).startOfDay().timeInMillis
    val startOfWeek = calendar.startOfWeek().timeInMillis
    val startOfMonth = calendar.startOfMonth().timeInMillis

    // Group stats
    val todayRecords = historyList.filter { it.timestamp >= startOfToday }
    val yesterdayRecords = historyList.filter { it.timestamp in startOfYesterday until startOfToday }
    val dayBeforeRecords = historyList.filter { it.timestamp in startOfDayBeforeYesterday until startOfYesterday }
    val thisWeekRecords = historyList.filter { it.timestamp >= startOfWeek }
    val thisMonthRecords = historyList.filter { it.timestamp >= startOfMonth }

    val totalLifetimeRepetitions = historyList.sumOf { it.countDone }
    val totalLifetimeSeconds = historyList.sumOf { it.secondsDone }
    val formattedLifetimeTime = formatDurationArabic(totalLifetimeSeconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Per-Dhikr Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تصفية الإحصائيات حسب الذكر:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DhikrFilterChip(
                        label = "جميع الأذكار",
                        selected = selectedDhikrId == null,
                        onClick = { selectedDhikrId = null }
                    )

                    allDhikrs.forEach { dhikr ->
                        val shortTitle = if (dhikr.arabicText.length > 22) {
                            dhikr.arabicText.take(20) + "..."
                        } else {
                            dhikr.arabicText
                        }
                        DhikrFilterChip(
                            label = shortTitle,
                            selected = selectedDhikrId == dhikr.id,
                            onClick = { selectedDhikrId = dhikr.id }
                        )
                    }
                }
            }
        }

        // Hero Card: Total Stats for the selected scope
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldMetallic.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedDhikrId == null) "حصيلة الذكر الإجمالية (الكل)" else "حصيلة الذكر المحدد",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMetricItem(
                            value = totalLifetimeRepetitions.toString(),
                            label = "إجمالي التسبيحات",
                            unit = "تسبيحة"
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        )

                        StatMetricItem(
                            value = formattedLifetimeTime,
                            label = "الوقت المستغرق",
                            unit = "إجمالي التركيز"
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        )

                        StatMetricItem(
                            value = historyList.size.toString(),
                            label = "الجلسات المكتملة",
                            unit = "جلسة"
                        )
                    }
                }
            }
        }

        // Section Title: Periodic Breakdown
        item {
            Text(
                text = "التقارير حسب الفترات الزمنية",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 1. Today (اليوم)
        item {
            PeriodStatsCard(
                title = "اليوم (Today)",
                records = todayRecords,
                icon = Icons.Default.CalendarToday,
                isHighlighted = true
            )
        }

        // 2. Yesterday (أمس)
        item {
            PeriodStatsCard(
                title = "أمس (Yesterday)",
                records = yesterdayRecords,
                icon = Icons.Default.History
            )
        }

        // 3. Day Before Yesterday (أول أمس)
        item {
            PeriodStatsCard(
                title = "أول أمس (Day Before Yesterday)",
                records = dayBeforeRecords,
                icon = Icons.Default.History
            )
        }

        // 4. This Week (هذا الأسبوع)
        item {
            PeriodStatsCard(
                title = "هذا الأسبوع (This Week)",
                records = thisWeekRecords,
                icon = Icons.Default.DateRange
            )
        }

        // 5. This Month (هذا الشهر)
        item {
            PeriodStatsCard(
                title = "هذا الشهر (This Month)",
                records = thisMonthRecords,
                icon = Icons.Default.DateRange
            )
        }

        // Per-Dhikr Breakdown List (if in all dhikrs mode)
        if (selectedDhikrId == null && allDhikrs.isNotEmpty() && allHistoryList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تفصيل الحصيلة حسب كل ذكر",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(allDhikrs, key = { "item_${it.id}" }) { item ->
                val itemHistory = allHistoryList.filter { it.dhikrId == item.id }
                val count = itemHistory.sumOf { it.countDone }
                val mins = itemHistory.sumOf { it.secondsDone } / 60
                val sessions = itemHistory.size

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.arabicText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$sessions جلسة • $mins دقيقة ذكر",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$count",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald500
                            )
                            Text(
                                text = "تسبيحة",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Clear All Stats Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showClearDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("clear_statistics_button")
            ) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعادة تعيين ومسح كافة الإحصائيات", fontWeight = FontWeight.Bold)
            }
        }

        // Recent Activity Log
        if (historyList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "سجل الجلسات الأخيرة (${historyList.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(historyList.take(20), key = { it.id }) { record ->
                HistoryItemCard(record = record)
            }
        }
    }

    // Safety Confirmation Modal
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تأكيد مسح الإحصائيات",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف كافة سجلات وإحصائيات الأذكار السابقة؟ لا يمكن التراجع عن هذا الإجراء.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllStatistics()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_clear_stats_button")
                ) {
                    Text("مسح الآن", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun DhikrFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PeriodStatsCard(
    title: String,
    records: List<DhikrHistory>,
    icon: ImageVector,
    isHighlighted: Boolean = false
) {
    val totalCount = records.sumOf { it.countDone }
    val totalSeconds = records.sumOf { it.secondsDone }
    val formattedDuration = formatDurationArabic(totalSeconds)
    val sessionCount = records.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isHighlighted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$sessionCount جلسة مكتملة • $formattedDuration ذكر",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$totalCount",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isHighlighted) Emerald500 else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تسبيحة",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatMetricItem(
    value: String,
    label: String,
    unit: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = unit,
            fontSize = 10.sp,
            color = GoldDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HistoryItemCard(record: DhikrHistory) {
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }
    val formattedDate = remember(record.timestamp) { formatter.format(Date(record.timestamp)) }
    val formattedTime = formatDurationArabic(record.secondsDone.toLong())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Emerald500,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.dhikrText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.countDone} تسبيحة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatDurationArabic(totalSeconds: Int): String = formatDurationArabic(totalSeconds.toLong())

fun formatDurationArabic(totalSeconds: Long): String {
    if (totalSeconds < 60) {
        return "$totalSeconds ثانية"
    }
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    if (minutes < 60) {
        return if (seconds > 0) "$minutes د و $seconds ث" else "$minutes دقيقة"
    }
    val hours = minutes / 60
    val remMinutes = minutes % 60
    return if (remMinutes > 0) "$hours س و $remMinutes د" else "$hours ساعة"
}

// Calendar Extension Helpers
private fun currentCalendar(): Calendar = Calendar.getInstance(Locale.getDefault())

private fun Calendar.startOfDay(): Calendar {
    val c = clone() as Calendar
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c
}

private fun Calendar.subtractDays(days: Int): Calendar {
    val c = clone() as Calendar
    c.add(Calendar.DAY_OF_YEAR, -days)
    return c
}

private fun Calendar.startOfWeek(): Calendar {
    val c = clone() as Calendar
    c.firstDayOfWeek = Calendar.SATURDAY
    c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c
}

private fun Calendar.startOfMonth(): Calendar {
    val c = clone() as Calendar
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c
}
