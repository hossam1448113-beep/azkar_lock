package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.MainViewModel
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.PureWhite
import com.example.util.PermissionHelper

@Composable
fun PermissionsSetupScreen(
    viewModel: MainViewModel,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions()
    }

    // Re-check permissions whenever returning to screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val hasOverlay by viewModel.hasOverlayPermission.collectAsState()
    val hasAccessibility by viewModel.hasAccessibilityPermission.collectAsState()
    val hasBattery by viewModel.hasBatteryOptIgnored.collectAsState()
    val hasNotification by viewModel.hasNotificationPermission.collectAsState()
    val hasExactAlarm by viewModel.hasExactAlarmPermission.collectAsState()
    val hasPhoneState by viewModel.hasPhoneStatePermission.collectAsState()

    val allGranted = hasOverlay && hasAccessibility && hasBattery && hasNotification && hasExactAlarm && hasPhoneState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        // Shield / Security Header Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Emerald700.copy(alpha = 0.15f))
                .border(2.dp, Emerald500.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "صلاحيات النظام",
                tint = Emerald500,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "صلاحيات التشغيل الضرورية",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ليعمل تطبيق أذكار القفل بدون تخطي ولضمان ظهور الأذكار فور فتح الهاتف وبانتظام في الخلفية، يتطلب النظام منح الصلاحيات التالية:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Overlay Permission
        PermissionItemCard(
            title = "الظهور فوق التطبيقات (Overlay)",
            description = "لعرض شاشة الأذكار التفاعلية فوق كل التطبيقات وشاشة القفل",
            icon = Icons.Default.Layers,
            isGranted = hasOverlay,
            buttonText = "منح الصلاحية",
            onRequest = {
                context.startActivity(PermissionHelper.getOverlayPermissionIntent(context))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Accessibility Service
        PermissionItemCard(
            title = "خدمة إمكانية الوصول (Accessibility)",
            description = "لتأمين شاشة الذكر ومنع تخطيها بأزرار الرجوع أو التطبيقات الحديثة",
            icon = Icons.Default.Security,
            isGranted = hasAccessibility,
            buttonText = "تفعيل الخدمة",
            onRequest = {
                context.startActivity(PermissionHelper.getAccessibilitySettingsIntent())
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Battery Optimization
        PermissionItemCard(
            title = "استثناء من توفير الطاقة (Battery)",
            description = "لمنع نظام أندرويد من إيقاف الخدمة في الخلفية عند قفل الشاشة",
            icon = Icons.Default.BatteryChargingFull,
            isGranted = hasBattery,
            buttonText = "استثناء البطارية",
            onRequest = {
                context.startActivity(PermissionHelper.getBatteryOptimizationIntent(context))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Notifications
        PermissionItemCard(
            title = "إشعارات الخدمة الدائمة",
            description = "لإبقاء خدمة مراقبة فتح الهاتف نشطة في الشريط العلوي",
            icon = Icons.Default.Notifications,
            isGranted = hasNotification,
            buttonText = "السماح بالإشعار",
            onRequest = {
                context.startActivity(PermissionHelper.getAppNotificationSettingsIntent(context))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Exact Alarm
        PermissionItemCard(
            title = "المنبه الدقيق (Exact Alarms)",
            description = "لتشغيل أذكار المؤقتات الدورية بدقة متناهية بالدقيقة المحددة",
            icon = Icons.Default.Alarm,
            isGranted = hasExactAlarm,
            buttonText = "تفعيل المنبه",
            onRequest = {
                context.startActivity(PermissionHelper.getExactAlarmSettingsIntent(context))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Phone State
        PermissionItemCard(
            title = "استشعار المكالمات (Phone State)",
            description = "لمنع ظهور شاشة الذكر أثناء المكالمات الهاتفية الجارية تجنباً للإزعاج",
            icon = Icons.Default.PhoneInTalk,
            isGranted = hasPhoneState,
            buttonText = "منح الإذن",
            onRequest = {
                phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Bottom Continue button
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allGranted) Emerald500 else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("continue_to_app_button")
        ) {
            Text(
                text = if (allGranted) "اكتملت الصلاحيات • الدخول للتطبيق" else "المتابعة للوحة التحكم",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    buttonText: String,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) Emerald500.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with status badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) Emerald500.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) Emerald500 else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action Button or Checkmark
            if (isGranted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "ممنوح",
                        tint = Emerald500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مفعّل",
                        color = Emerald500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        }
    }
}
