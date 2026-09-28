package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.DhikrForegroundService
import com.example.ui.MainViewModel
import com.example.ui.screens.DhikrManagerScreen
import com.example.ui.screens.DhikrSessionScreen
import com.example.ui.screens.PermissionsSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.Emerald500
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMetallic
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.util.PermissionHelper
import androidx.compose.material.icons.filled.HourglassTop

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DhikrApplication
        if (app.preferences.isServiceEnabled) {
            DhikrForegroundService.start(this)
        }

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    DhikrLockMainApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DHIKR_MANAGER("إدارة الأذكار", Icons.Default.ListAlt, "tab_dhikr_manager"),
    DHIKR_SESSION("جلسة الذكر", Icons.Default.HourglassTop, "tab_dhikr_session"),
    SETTINGS("الجلسات والصلاحيات", Icons.Default.Settings, "tab_settings"),
    STATISTICS("الإحصائيات", Icons.Default.Analytics, "tab_statistics")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrLockMainApp(viewModel: MainViewModel) {
    val app = DhikrApplication.instance
    var isOnboardingDone by remember { mutableStateOf(app.preferences.isOnboardingCompleted) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(NavigationTab.DHIKR_MANAGER) }

    val hasOverlay by viewModel.hasOverlayPermission.collectAsState()
    val hasBattery by viewModel.hasBatteryOptIgnored.collectAsState()
    val isCoreReady = hasOverlay && hasBattery

    if (!isOnboardingDone && !isCoreReady) {
        PermissionsSetupScreen(
            viewModel = viewModel,
            onContinue = {
                app.preferences.isOnboardingCompleted = true
                isOnboardingDone = true
            }
        )
    } else if (showPermissionsDialog) {
        PermissionsSetupScreen(
            viewModel = viewModel,
            onContinue = {
                showPermissionsDialog = false
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "أذكار القفل",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• رطب لسانك",
                                fontSize = 12.sp,
                                color = GoldLight
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showPermissionsDialog = true },
                            modifier = Modifier.testTag("permissions_button")
                        ) {
                            Icon(
                                imageVector = if (isCoreReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "الصلاحيات",
                                tint = if (isCoreReady) GoldLight else MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
                    when (tab) {
                        NavigationTab.DHIKR_MANAGER -> DhikrManagerScreen(viewModel = viewModel)
                        NavigationTab.DHIKR_SESSION -> DhikrSessionScreen(viewModel = viewModel)
                        NavigationTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        NavigationTab.STATISTICS -> StatisticsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
