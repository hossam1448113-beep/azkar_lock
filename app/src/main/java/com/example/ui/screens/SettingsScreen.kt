package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.service.DhikrForegroundService
import com.example.ui.MainViewModel
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.PureWhite
import com.example.util.PermissionHelper

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh permissions and pause state when returning to screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
                viewModel.refreshPauseState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cooldownValue by viewModel.cooldownValue.collectAsState()
    val cooldownUnit by viewModel.cooldownUnit.collectAsState()
    val isAppPaused by viewModel.isAppPaused.collectAsState()
    val remainingPauseMillis by viewModel.remainingPauseMillis.collectAsState()

    val triggerOnUnlock by viewModel.triggerOnUnlock.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    val hasOverlay by viewModel.hasOverlayPermission.collectAsState()
    val hasAccessibility by viewModel.hasAccessibilityPermission.collectAsState()
    val hasBattery by viewModel.hasBatteryOptIgnored.collectAsState()
    val hasNotification by viewModel.hasNotificationPermission.collectAsState()
    val hasExactAlarm by viewModel.hasExactAlarmPermission.collectAsState()
    val hasPhoneState by viewModel.hasPhoneStatePermission.collectAsState()

    // Temporary input states for manual inputs
    var inputCooldownValue by remember(cooldownValue) { mutableIntStateOf(cooldownValue) }
    var selectedCooldownUnit by remember(cooldownUnit) { mutableStateOf(cooldownUnit) }

    var pauseValueInput by remember { mutableIntStateOf(1) }
    var pauseUnitSelected by remember { mutableStateOf("HOURS") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Pause Banner
        if (isAppPaused) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldMetallic.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldMetallic)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PauseCircle,
                                contentDescription = null,
                                tint = GoldMetallic,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "التطبيق متوقف مؤقتاً حالياً",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val remainingMinutes = (remainingPauseMillis / (60 * 1000L)).coerceAtLeast(1)
                        Text(
                            text = "لن تظهر شاشة الذكر ولن تعمل المؤقتات حتى انتهاء مهلة الإيقاف (متبقي حوالي $remainingMinutes دقيقة).",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                viewModel.resumeAppNow()
                                Toast.makeText(context, "تم استئناف التطبيق بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استئناف التطبيق الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Manual Cooldown Interval
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "مهلة عدم الإزعاج اليدوية (Cooldown)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "أدخل القيمة الرقمية واختر الوحدة بحرية لمنع تكرار ظهور الشاشة عند القفل وفتح القفل المتتالي:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = if (inputCooldownValue > 0) inputCooldownValue.toString() else "",
                            onValueChange = {
                                val v = it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0
                                inputCooldownValue = v
                                viewModel.setManualCooldown(v, selectedCooldownUnit)
                            },
                            label = { Text("المدة") },
                            placeholder = { Text("5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            modifier = Modifier.weight(1.2f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CooldownUnitChip(
                                label = "دقائق",
                                selected = selectedCooldownUnit == "MINUTES",
                                onClick = {
                                    selectedCooldownUnit = "MINUTES"
                                    viewModel.setManualCooldown(inputCooldownValue, "MINUTES")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            CooldownUnitChip(
                                label = "ساعات",
                                selected = selectedCooldownUnit == "HOURS",
                                onClick = {
                                    selectedCooldownUnit = "HOURS"
                                    viewModel.setManualCooldown(inputCooldownValue, "HOURS")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val unitLabel = if (selectedCooldownUnit == "HOURS") "ساعة" else "دقيقة"
                    Text(
                        text = "✓ الإعداد الحالي: انتظار $inputCooldownValue $unitLabel بعد كل ذكر مكتمل قبل الظهور مجدداً.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Section: Temporary App Pause
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "إيقاف مؤقت للتطبيق (Pause App)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "أوقف التطبيق مؤقتاً أثناء الاجتماعات أو الفعاليات الخاصة. يستأنف التطبيق نشاطه تلقائياً فور انتهاء المدة:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = if (pauseValueInput > 0) pauseValueInput.toString() else "",
                            onValueChange = {
                                pauseValueInput = it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0
                            },
                            label = { Text("قيمة المدة") },
                            placeholder = { Text("2") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            modifier = Modifier.weight(1.8f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("MINUTES" to "دقائق", "HOURS" to "ساعات", "DAYS" to "أيام").forEach { (unitKey, unitName) ->
                                CooldownUnitChip(
                                    label = unitName,
                                    selected = pauseUnitSelected == unitKey,
                                    onClick = { pauseUnitSelected = unitKey },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val v = pauseValueInput.coerceAtLeast(1)
                            viewModel.pauseApp(v, pauseUnitSelected)
                            val unitArabic = when (pauseUnitSelected) {
                                "MINUTES" -> "دقيقة"
                                "HOURS" -> "ساعة"
                                "DAYS" -> "يوم"
                                else -> "ساعة"
                            }
                            Toast.makeText(context, "تم إيقاف التطبيق مؤقتاً لمدة $v $unitArabic", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PauseCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تفعيل الإيقاف المؤقت الآن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Smart Exclusions Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "الاستثناءات الذكية (Smart Exclusions)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• المكالمات الهاتفية ومكالمات التطبيقات: يتم حجب ظهور الشاشة تلقائياً أثناء أي مكالمة نشطة.\n• تسجيل ومشاركة الشاشة: يتم حجب ظهور الشاشة تلقائياً أثناء تسجيل الشاشة أو البث لمنع الإحراج أو مقاطعة العمل.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Section: Mandatory Morning & Evening Azkar (أذكار الصباح والمساء الإلزامية)
        item {
            MandatoryAzkarSettingsCard(viewModel = viewModel)
        }

        // Section: App-Open Triggered Azkar (وقفة إيمانية قبل فتح التطبيقات)
        item {
            AppOpenSettingsCard(viewModel = viewModel)
        }

        // Section: Trigger Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "خيارات توقيت تشغيل الأذكار العامة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Unlock Trigger Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "التفعيل عند فتح الهاتف",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "عرض قفل الذكر فور بصمة أو رمز القفل",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = triggerOnUnlock,
                            onCheckedChange = { viewModel.setTriggerOnUnlock(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Per-Dhikr Timers Information Card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المؤقتات الدورية الفردية (Per-Dhikr Timers)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "كل ذكر يُدار بمؤقته الزمني المستقل وقواعد تشغيله الفردية داخل شاشة إدارة الأذكار. تعمل بدقة متناهية عبر منبهات النظام الدقيقة (AlarmManager) والخدمة الدائمة حتى أثناء وضع السكون العميق (Deep Doze).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Sound & Haptics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "التغذية الراجعة والتنبيهات",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "الاهتزاز عند الضغط للتسبيح", fontSize = 14.sp)
                        }
                        Switch(
                            checked = isVibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "التنبيه الصوتي عند الإتمام", fontSize = 14.sp)
                        }
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) }
                        )
                    }
                }
            }
        }

        // Section: Permissions Checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "حالة صلاحيات النظام",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تأكد من تفعيل كافة الصلاحيات لضمان حجب التخطي وعمل التطبيق بدقة",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PermissionRow(
                        title = "الظهور فوق التطبيقات (Overlay)",
                        isGranted = hasOverlay,
                        onClick = { context.startActivity(PermissionHelper.getOverlayPermissionIntent(context)) }
                    )
                    PermissionRow(
                        title = "خدمة إمكانية الوصول (Accessibility)",
                        isGranted = hasAccessibility,
                        onClick = { context.startActivity(PermissionHelper.getAccessibilitySettingsIntent()) }
                    )
                    PermissionRow(
                        title = "استثناء موفر البطارية (Battery)",
                        isGranted = hasBattery,
                        onClick = { context.startActivity(PermissionHelper.getBatteryOptimizationIntent(context)) }
                    )
                    PermissionRow(
                        title = "إذن الإشعارات الدائمة",
                        isGranted = hasNotification,
                        onClick = { context.startActivity(PermissionHelper.getAppNotificationSettingsIntent(context)) }
                    )
                    PermissionRow(
                        title = "المنبه الدقيق (Exact Alarms)",
                        isGranted = hasExactAlarm,
                        onClick = { context.startActivity(PermissionHelper.getExactAlarmSettingsIntent(context)) }
                    )
                    PermissionRow(
                        title = "استشعار حالة المكالمات (Phone State)",
                        isGranted = hasPhoneState,
                        onClick = { context.startActivity(PermissionHelper.getAppDetailsSettingsIntent(context)) }
                    )
                }
            }
        }

        // Restart Service Button
        item {
            Button(
                onClick = {
                    DhikrForegroundService.stop(context)
                    DhikrForegroundService.start(context)
                    Toast.makeText(context, "تمت إعادة تشغيل الخدمة وتحديث الجدولة بنجاح", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعادة تشغيل خدمة الخلفية وتحديث الجدولة", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CooldownUnitChip(
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

@Composable
private fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isGranted) Emerald500 else ErrorRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isGranted) {
            Text(
                text = "مفعّلة",
                color = Emerald500,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("تفعيل", fontSize = 11.sp)
            }
        }
    }
}
