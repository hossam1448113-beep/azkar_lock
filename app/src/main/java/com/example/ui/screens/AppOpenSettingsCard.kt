package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TargetMode
import com.example.data.prefs.AppOpenRule
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.PureWhite
import com.example.util.AppInfoHelper
import com.example.util.AppItemInfo
import com.example.util.PermissionHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppOpenSettingsCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEnabled by viewModel.isAppOpenTriggerEnabled.collectAsState()
    val targetMode by viewModel.appOpenTargetMode.collectAsState()
    val targetCount by viewModel.appOpenTargetCount.collectAsState()
    val targetTimeSeconds by viewModel.appOpenTargetTimeSeconds.collectAsState()
    val cooldownMinutes by viewModel.appOpenCooldownMinutes.collectAsState()
    val monitoredPackages by viewModel.appOpenMonitoredPackages.collectAsState()
    val hasAccessibility by viewModel.hasAccessibilityPermission.collectAsState()

    var showAppPickerModal by remember { mutableStateOf(false) }
    var customizingAppItem by remember { mutableStateOf<AppItemInfo?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isEnabled) Emerald500.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with Feature Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isEnabled) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = if (isEnabled) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "أذكار ما قبل فتح التطبيقات والخصائص",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "وقفة إيمانية مع جلسة استراحة مخصصة لكل تطبيق",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.setAppOpenTriggerEnabled(it) }
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // Accessibility Permission Warning if missing
                if (!hasAccessibility) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldMetallic.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GoldMetallic.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GoldMetallic,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "خدمة إمكانية الوصول مطلوبة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "يلزم تفعيل الخدمة لتتمكن المنظومة من رصد فتح التطبيقات والخصائص فورياً.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { PermissionHelper.openAccessibilitySettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldMetallic),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("تفعيل", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Default Target Mode Selector
                Text(
                    text = "الهدف الافتراضي قبل الفتح:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TargetModeChip(
                        label = "بالعدد فقط",
                        selected = targetMode == TargetMode.COUNT_ONLY,
                        onClick = { viewModel.setAppOpenTargetMode(TargetMode.COUNT_ONLY) },
                        modifier = Modifier.weight(1f)
                    )
                    TargetModeChip(
                        label = "بالوقت فقط",
                        selected = targetMode == TargetMode.TIME_ONLY,
                        onClick = { viewModel.setAppOpenTargetMode(TargetMode.TIME_ONLY) },
                        modifier = Modifier.weight(1f)
                    )
                    TargetModeChip(
                        label = "كلاهما معاً",
                        selected = targetMode == TargetMode.COUNT_AND_TIME,
                        onClick = { viewModel.setAppOpenTargetMode(TargetMode.COUNT_AND_TIME) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Count configuration
                if (targetMode == TargetMode.COUNT_ONLY || targetMode == TargetMode.COUNT_AND_TIME) {
                    Text(
                        text = "عدد التكرارات الافتراضي: $targetCount تسبيحات",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 3, 5, 7, 10, 33).forEach { countPreset ->
                            FilterChip(
                                text = countPreset.toString(),
                                selected = targetCount == countPreset,
                                onSelect = { viewModel.setAppOpenTargetCount(countPreset) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Target Duration configuration
                if (targetMode == TargetMode.TIME_ONLY || targetMode == TargetMode.COUNT_AND_TIME) {
                    Text(
                        text = "مدة التدبر الافتراضية: $targetTimeSeconds ثانية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(10, 15, 20, 30, 60).forEach { secPreset ->
                            FilterChip(
                                text = "${secPreset}ث",
                                selected = targetTimeSeconds == secPreset,
                                onSelect = { viewModel.setAppOpenTargetTimeSeconds(secPreset) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Default Rest / Cool-down Interval Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "جلسة الاستراحة الافتراضية للتطبيق (Cooldown):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (cooldownMinutes == 0) "يظهر في كل مرة تفتح فيها التطبيق" else "لن يتكرر لنفس التطبيق إلا بعد $cooldownMinutes دقيقة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0 to "في كل مرة", 5 to "5د", 15 to "15د", 30 to "30د", 60 to "ساعة").forEach { (mins, label) ->
                        FilterChip(
                            text = label,
                            selected = cooldownMinutes == mins,
                            onSelect = { viewModel.setAppOpenCooldownMinutes(mins) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monitored Apps & Features Section
                Text(
                    text = "التطبيقات والخصائص المفعلة (${monitoredPackages.size} محدد):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Popular Apps & Features Quick Toggles
                val popularApps = remember(monitoredPackages) { AppInfoHelper.getPopularAppList(context, monitoredPackages) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    popularApps.forEach { appItem ->
                        val isChecked = monitoredPackages.contains(appItem.packageName)
                        val rule = viewModel.getAppOpenRule(appItem.packageName)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isChecked) Emerald500 else Color.Transparent),
                            modifier = Modifier.clickable {
                                viewModel.toggleAppOpenMonitoredPackage(appItem.packageName)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Emerald500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = appItem.appName.substringBefore(" ("),
                                    fontSize = 11.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChecked) Emerald700 else MaterialTheme.colorScheme.onSurface
                                )
                                if (isChecked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "تخصيص",
                                        tint = if (rule.isCustomized) GoldMetallic else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { customizingAppItem = appItem }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { showAppPickerModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("عرض كل تطبيقات الهاتف وخصائص النظام (${monitoredPackages.size})", fontSize = 13.sp)
                }
            }
        }
    }

    // Modal Dialog to Select from ALL Installed Apps and System Features
    if (showAppPickerModal) {
        InstalledAppsPickerDialog(
            monitoredPackages = monitoredPackages,
            viewModel = viewModel,
            onDismiss = { showAppPickerModal = false },
            onTogglePackage = { pkg -> viewModel.toggleAppOpenMonitoredPackage(pkg) },
            onCustomize = { appItem -> customizingAppItem = appItem }
        )
    }

    // Per-App Customization Dialog
    if (customizingAppItem != null) {
        PerAppConfigDialog(
            appItem = customizingAppItem!!,
            viewModel = viewModel,
            onDismiss = { customizingAppItem = null }
        )
    }
}

@Composable
fun PerAppConfigDialog(
    appItem: AppItemInfo,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialRule = remember(appItem.packageName) {
        viewModel.getAppOpenRule(appItem.packageName)
    }

    var isCustomized by remember { mutableStateOf(initialRule.isCustomized) }
    var targetMode by remember { mutableStateOf(initialRule.targetMode) }
    var targetCount by remember { mutableIntStateOf(initialRule.targetCount) }
    var targetTimeSeconds by remember { mutableIntStateOf(initialRule.targetTimeSeconds) }
    var cooldownMinutes by remember { mutableIntStateOf(initialRule.cooldownMinutes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تخصيص: ${appItem.appName}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appItem.packageName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Rule Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "تفعيل قواعد استراحة وهدف خاصة لهذا التطبيق",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isCustomized,
                        onCheckedChange = { isCustomized = it }
                    )
                }

                if (isCustomized) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "طريقة الإتمام:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TargetModeChip(
                            label = "بالعدد",
                            selected = targetMode == TargetMode.COUNT_ONLY,
                            onClick = { targetMode = TargetMode.COUNT_ONLY },
                            modifier = Modifier.weight(1f)
                        )
                        TargetModeChip(
                            label = "بالوقت",
                            selected = targetMode == TargetMode.TIME_ONLY,
                            onClick = { targetMode = TargetMode.TIME_ONLY },
                            modifier = Modifier.weight(1f)
                        )
                        TargetModeChip(
                            label = "كلاهما",
                            selected = targetMode == TargetMode.COUNT_AND_TIME,
                            onClick = { targetMode = TargetMode.COUNT_AND_TIME },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (targetMode == TargetMode.COUNT_ONLY || targetMode == TargetMode.COUNT_AND_TIME) {
                        Text(text = "العدد المستهدف: $targetCount تسبيحات", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 3, 5, 10, 33).forEach { cnt ->
                                FilterChip(
                                    text = cnt.toString(),
                                    selected = targetCount == cnt,
                                    onSelect = { targetCount = cnt }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (targetMode == TargetMode.TIME_ONLY || targetMode == TargetMode.COUNT_AND_TIME) {
                        Text(text = "المدة المستهدفة: $targetTimeSeconds ثانية", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(10, 15, 20, 30, 60).forEach { sec ->
                                FilterChip(
                                    text = "${sec}ث",
                                    selected = targetTimeSeconds == sec,
                                    onSelect = { targetTimeSeconds = sec }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Individual Rest / Cooldown Session
                    Text(
                        text = "جلسة الاستراحة الخاصة بهذا التطبيق (Cooldown):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0 to "في كل مرة", 5 to "5د", 15 to "15د", 30 to "30د", 60 to "ساعة", 120 to "ساعتان").forEach { (m, l) ->
                            FilterChip(
                                text = l,
                                selected = cooldownMinutes == m,
                                onSelect = { cooldownMinutes = m }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val newRule = AppOpenRule(
                            packageName = appItem.packageName,
                            isCustomized = isCustomized,
                            targetMode = targetMode,
                            targetCount = targetCount,
                            targetTimeSeconds = targetTimeSeconds,
                            cooldownMinutes = cooldownMinutes,
                            specificDhikrId = -1L
                        )
                        viewModel.saveAppOpenRule(newRule)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("حفظ إعدادات التطبيق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TargetModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Emerald500 else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(38.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Emerald500 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .height(32.dp)
            .clickable(onClick = onSelect)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InstalledAppsPickerDialog(
    monitoredPackages: Set<String>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onTogglePackage: (String) -> Unit,
    onCustomize: (AppItemInfo) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf("all") } // "all", "features", "installed", "selected"

    val allAppsAndFeatures = remember(monitoredPackages) {
        AppInfoHelper.getAllInstalledAppsAndFeatures(context, monitoredPackages)
    }

    val filteredList = remember(searchQuery, selectedCategoryTab, allAppsAndFeatures, monitoredPackages) {
        allAppsAndFeatures.filter { item ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.appName.contains(searchQuery, ignoreCase = true) || item.packageName.contains(searchQuery, ignoreCase = true)
            }
            val matchesCategory = when (selectedCategoryTab) {
                "features" -> item.isSystemFeature
                "installed" -> !item.isSystemFeature
                "selected" -> monitoredPackages.contains(item.packageName)
                else -> true
            }
            matchesSearch && matchesCategory
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "التطبيقات وخصائص النظام",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Text(
                    text = "حدد التطبيقات أو الخصائص (كالواي فاي، الكاميرا، إلخ) وخصص استراحة وهدف كل منها",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن تطبيق أو خاصية...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        text = "الكل (${allAppsAndFeatures.size})",
                        selected = selectedCategoryTab == "all",
                        onSelect = { selectedCategoryTab = "all" }
                    )
                    FilterChip(
                        text = "خصائص النظام ⚙️",
                        selected = selectedCategoryTab == "features",
                        onSelect = { selectedCategoryTab = "features" }
                    )
                    FilterChip(
                        text = "المثبتة 📱",
                        selected = selectedCategoryTab == "installed",
                        onSelect = { selectedCategoryTab = "installed" }
                    )
                    FilterChip(
                        text = "المحددة (${monitoredPackages.size})",
                        selected = selectedCategoryTab == "selected",
                        onSelect = { selectedCategoryTab = "selected" }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Items List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList, key = { it.packageName }) { appItem ->
                        val isChecked = monitoredPackages.contains(appItem.packageName)
                        val rule = viewModel.getAppOpenRule(appItem.packageName)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChecked) Emerald500.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (isChecked) Emerald500.copy(alpha = 0.4f) else Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTogglePackage(appItem.packageName) }
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onTogglePackage(appItem.packageName) },
                                    colors = CheckboxDefaults.colors(checkedColor = Emerald500)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = appItem.appName,
                                            fontSize = 13.sp,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (appItem.isSystemFeature) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = GoldMetallic.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "خاصية نظام",
                                                    fontSize = 9.sp,
                                                    color = GoldLight,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (rule.isCustomized) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Emerald500.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "مخصص (${rule.cooldownMinutes}د)",
                                                    fontSize = 9.sp,
                                                    color = Emerald500,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = appItem.packageName,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                if (isChecked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onCustomize(appItem) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "تخصيص",
                                            tint = if (rule.isCustomized) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("تم وحفظ (${monitoredPackages.size} محدد)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
