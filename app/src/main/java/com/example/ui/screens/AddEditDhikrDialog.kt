package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrItem
import com.example.data.model.PeriodicTimeUnit
import com.example.data.model.TargetMode
import com.example.data.model.TriggerMode
import com.example.ui.theme.Emerald500
import com.example.ui.theme.PureWhite
import java.util.Calendar
import java.util.Locale

@Composable
fun AddEditDhikrDialog(
    dhikrItem: DhikrItem? = null,
    onDismiss: () -> Unit,
    onSave: (DhikrItem) -> Unit
) {
    var arabicText by remember { mutableStateOf(dhikrItem?.arabicText ?: "") }
    var virtue by remember { mutableStateOf(dhikrItem?.virtue ?: "") }
    var targetMode by remember { mutableStateOf(dhikrItem?.targetMode ?: TargetMode.COUNT_ONLY) }
    var targetCount by remember { mutableIntStateOf(dhikrItem?.targetCount ?: 33) }
    var targetTimeSeconds by remember { mutableIntStateOf(dhikrItem?.targetTimeSeconds ?: 30) }
    var triggerMode by remember { mutableStateOf(dhikrItem?.triggerMode ?: TriggerMode.ON_UNLOCK) }
    var timerValue by remember { mutableIntStateOf(dhikrItem?.timerValue ?: 20) }
    var timerUnit by remember { mutableStateOf(dhikrItem?.timerUnit ?: PeriodicTimeUnit.MINUTES) }

    var scheduledHour by remember { mutableIntStateOf(dhikrItem?.scheduledHour ?: 12) }
    var scheduledMinute by remember { mutableIntStateOf(dhikrItem?.scheduledMinute ?: 0) }
    var selectedDays by remember { mutableStateOf(dhikrItem?.getDaysOfWeekSet() ?: setOf(1, 2, 3, 4, 5, 6, 7)) }

    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (dhikrItem == null) "إضافة ذكر جديد" else "تعديل الذكر",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Arabic Dhikr text input
                OutlinedTextField(
                    value = arabicText,
                    onValueChange = {
                        arabicText = it
                        errorText = null
                    },
                    label = { Text("نص الذكر الشريف (بالتشكيل)") },
                    placeholder = { Text("مثال: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dhikr_text_input"),
                    minLines = 2,
                    isError = errorText != null
                )

                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                // Virtue input
                OutlinedTextField(
                    value = virtue,
                    onValueChange = { virtue = it },
                    label = { Text("فضل الذكر أو معناه (اختياري)") },
                    placeholder = { Text("مثال: كلمتان خفيفتان على اللسان...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dhikr_virtue_input"),
                    minLines = 1
                )

                // Target Mode Selection
                Text(
                    text = "طريقة إتمام الذكر المستهدفة:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    // Option 1: Count Only
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { targetMode = TargetMode.COUNT_ONLY }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = targetMode == TargetMode.COUNT_ONLY,
                            onClick = { targetMode = TargetMode.COUNT_ONLY }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "بالعدد فقط (تسبيحات محددة)", fontSize = 14.sp)
                    }

                    // Option 2: Time Only
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { targetMode = TargetMode.TIME_ONLY }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = targetMode == TargetMode.TIME_ONLY,
                            onClick = { targetMode = TargetMode.TIME_ONLY }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "بالوقت فقط (مدة زمنية بالثواني)", fontSize = 14.sp)
                    }

                    // Option 3: Count and Time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { targetMode = TargetMode.COUNT_AND_TIME }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = targetMode == TargetMode.COUNT_AND_TIME,
                            onClick = { targetMode = TargetMode.COUNT_AND_TIME }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "العدد والوقت معاً (تكرار وَ مهلة زمنية)", fontSize = 14.sp)
                    }
                }

                // Targets inputs based on mode
                if (targetMode == TargetMode.COUNT_ONLY || targetMode == TargetMode.COUNT_AND_TIME) {
                    OutlinedTextField(
                        value = if (targetCount > 0) targetCount.toString() else "",
                        onValueChange = {
                            targetCount = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
                        },
                        label = { Text("العدد المطلوب للتسبيح (مثال: 33)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (targetMode == TargetMode.TIME_ONLY || targetMode == TargetMode.COUNT_AND_TIME) {
                    OutlinedTextField(
                        value = if (targetTimeSeconds > 0) targetTimeSeconds.toString() else "",
                        onValueChange = {
                            targetTimeSeconds = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
                        },
                        label = { Text("المدة المطلوبة بالثواني (مثال: 45)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Trigger Mode Section
                Text(
                    text = "وقت ظهور هذا الذكر:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TriggerChip(
                            label = "عند فتح الهاتف",
                            selected = triggerMode == TriggerMode.ON_UNLOCK,
                            onClick = { triggerMode = TriggerMode.ON_UNLOCK },
                            modifier = Modifier.weight(1f)
                        )
                        TriggerChip(
                            label = "مؤقت دوري",
                            selected = triggerMode == TriggerMode.PERIODIC_TIMER,
                            onClick = { triggerMode = TriggerMode.PERIODIC_TIMER },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TriggerChip(
                            label = "كلاهما معاً",
                            selected = triggerMode == TriggerMode.BOTH,
                            onClick = { triggerMode = TriggerMode.BOTH },
                            modifier = Modifier.weight(1f)
                        )
                        TriggerChip(
                            label = "وقت ويوم محدد ⏰",
                            selected = triggerMode == TriggerMode.SCHEDULED_TIME,
                            onClick = { triggerMode = TriggerMode.SCHEDULED_TIME },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Periodic Timer Customization (Number + Unit: Minutes / Hours / Days)
                if (triggerMode == TriggerMode.PERIODIC_TIMER || triggerMode == TriggerMode.BOTH) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "تخصيص وقت المؤقت الدوري:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = if (timerValue > 0) timerValue.toString() else "",
                                onValueChange = {
                                    timerValue = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
                                },
                                label = { Text("القيمة الرقمية") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            // Unit selection chips
                            Row(
                                modifier = Modifier.weight(1.4f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PeriodicTimeUnit.values().forEach { unit ->
                                    TriggerChip(
                                        label = unit.displayNameArabic,
                                        selected = timerUnit == unit,
                                        onClick = { timerUnit = unit },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val previewMinutes = (timerValue.coerceAtLeast(1) * timerUnit.multiplierMinutes)
                        Text(
                            text = "💡 سيظهر هذا الذكر كل $timerValue ${timerUnit.displayNameArabic} (ما يعادل $previewMinutes دقيقة).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Specific Time & Days of Week Customization
                if (triggerMode == TriggerMode.SCHEDULED_TIME) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "تخصيص الوقت ويوم الأسبوع:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Hour and Minute Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الساعة المحددة:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", scheduledHour, scheduledMinute)
                            Text(
                                text = formattedTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Hour Slider (0..23)
                        Text(text = "الساعة: $scheduledHour", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value = scheduledHour.toFloat(),
                            onValueChange = { scheduledHour = it.toInt() },
                            valueRange = 0f..23f,
                            steps = 22,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Minute Slider (0..55 in 5m steps)
                        Text(text = "الدقيقة: $scheduledMinute", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value = scheduledMinute.toFloat(),
                            onValueChange = { scheduledMinute = it.toInt() },
                            valueRange = 0f..59f,
                            steps = 58,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Days of week selector
                        Text(
                            text = "أيام الظهور في الأسبوع:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val daysList = listOf(
                            Calendar.SATURDAY to "السبت",
                            Calendar.SUNDAY to "الأحد",
                            Calendar.MONDAY to "الإثنين",
                            Calendar.TUESDAY to "الثلاثاء",
                            Calendar.WEDNESDAY to "الأربعاء",
                            Calendar.THURSDAY to "الخميس",
                            Calendar.FRIDAY to "الجمعة"
                        )

                        // 2 rows of day chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            daysList.take(4).forEach { (dayCode, dayName) ->
                                val isSelected = selectedDays.contains(dayCode)
                                TriggerChip(
                                    label = dayName,
                                    selected = isSelected,
                                    onClick = {
                                        val updated = selectedDays.toMutableSet()
                                        if (isSelected) {
                                            if (updated.size > 1) updated.remove(dayCode)
                                        } else {
                                            updated.add(dayCode)
                                        }
                                        selectedDays = updated
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            daysList.drop(4).forEach { (dayCode, dayName) ->
                                val isSelected = selectedDays.contains(dayCode)
                                TriggerChip(
                                    label = dayName,
                                    selected = isSelected,
                                    onClick = {
                                        val updated = selectedDays.toMutableSet()
                                        if (isSelected) {
                                            if (updated.size > 1) updated.remove(dayCode)
                                        } else {
                                            updated.add(dayCode)
                                        }
                                        selectedDays = updated
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedTimeStr = String.format(Locale.getDefault(), "%02d:%02d", scheduledHour, scheduledMinute)
                        Text(
                            text = "💡 سيظهر هذا الذكر تلقائياً في تمام الساعة $formattedTimeStr في ${if (selectedDays.size == 7) "كل الأيام" else "${selectedDays.size} أيام محددة"}.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (arabicText.trim().isEmpty()) {
                        errorText = "يرجى كتابة نص الذكر"
                        return@Button
                    }
                    val validCount = if (targetCount <= 0) 33 else targetCount
                    val validSeconds = if (targetTimeSeconds <= 0) 30 else targetTimeSeconds
                    val validValue = if (timerValue <= 0) 20 else timerValue
                    val totalMinutes = (validValue * timerUnit.multiplierMinutes).toInt().coerceAtLeast(1)

                    val newItem = (dhikrItem ?: DhikrItem(arabicText = "")).copy(
                        arabicText = arabicText.trim(),
                        virtue = virtue.trim(),
                        targetMode = targetMode,
                        targetCount = validCount,
                        targetTimeSeconds = validSeconds,
                        triggerMode = triggerMode,
                        timerIntervalMinutes = totalMinutes,
                        timerValue = validValue,
                        timerUnit = timerUnit,
                        scheduledHour = scheduledHour,
                        scheduledMinute = scheduledMinute,
                        scheduledDaysOfWeek = selectedDays.joinToString(",")
                    )
                    onSave(newItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_dhikr_button")
            ) {
                Text("حفظ الذكر", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun TriggerChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
