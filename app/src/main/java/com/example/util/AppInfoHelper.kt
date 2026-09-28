package com.example.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppItemInfo(
    val packageName: String,
    val appName: String,
    val isMonitored: Boolean = false,
    val isSystemFeature: Boolean = false,
    val isCustomized: Boolean = false,
    val category: String = "apps", // "features", "popular", "installed"
    val icon: Drawable? = null
)

object AppInfoHelper {

    const val FEATURE_WIFI = "sys.feature.wifi"
    const val FEATURE_BLUETOOTH = "sys.feature.bluetooth"
    const val FEATURE_DATA = "sys.feature.data"
    const val FEATURE_SOUND = "sys.feature.sound"
    const val FEATURE_DISPLAY = "sys.feature.display"
    const val FEATURE_CAMERA = "sys.feature.camera"
    const val FEATURE_SETTINGS = "com.android.settings"
    const val FEATURE_PLAY_STORE = "com.android.vending"

    private val SYSTEM_FEATURES = listOf(
        FEATURE_WIFI to "إعدادات الواي فاي (Wi-Fi)",
        FEATURE_BLUETOOTH to "إعدادات البلوتوث (Bluetooth)",
        FEATURE_DATA to "بيانات الهاتف والشبكة (Mobile Data)",
        FEATURE_SOUND to "إعدادات الصوت والنغمات (Sound)",
        FEATURE_DISPLAY to "إعدادات الشاشة والسطوع (Display)",
        FEATURE_CAMERA to "الكاميرا والتصوير (Camera)",
        FEATURE_SETTINGS to "إعدادات الهاتف العامة (Settings)",
        FEATURE_PLAY_STORE to "متجر التطبيقات (Google Play)"
    )

    private val POPULAR_APPS = listOf(
        "com.whatsapp" to "واتساب (WhatsApp)",
        "com.google.android.youtube" to "يوتيوب (YouTube)",
        "com.instagram.android" to "إنستغرام (Instagram)",
        "com.facebook.katana" to "فيسبوك (Facebook)",
        "com.twitter.android" to "تويتر / X",
        "com.zhiliaoapp.musically" to "تيك توك (TikTok)",
        "com.snapchat.android" to "سناب شات (Snapchat)",
        "org.telegram.messenger" to "تيليجرام (Telegram)",
        "com.facebook.orca" to "ماسينجر (Messenger)"
    )

    fun getAppName(context: Context, packageName: String): String {
        SYSTEM_FEATURES.find { it.first == packageName }?.let { return it.second }
        POPULAR_APPS.find { it.first == packageName }?.let { return it.second }

        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    fun getSystemFeaturesList(context: Context, monitoredPackages: Set<String>): List<AppItemInfo> {
        val app = context.applicationContext as? com.example.DhikrApplication
        val prefs = app?.preferences
        return SYSTEM_FEATURES.map { (pkg, name) ->
            val rule = prefs?.getAppOpenRule(pkg)
            AppItemInfo(
                packageName = pkg,
                appName = name,
                isMonitored = monitoredPackages.contains(pkg),
                isSystemFeature = true,
                isCustomized = rule?.isCustomized == true,
                category = "features",
                icon = getAppIcon(context, pkg)
            )
        }
    }

    fun getPopularAppList(context: Context, monitoredPackages: Set<String>): List<AppItemInfo> {
        val app = context.applicationContext as? com.example.DhikrApplication
        val prefs = app?.preferences
        return POPULAR_APPS.map { (pkg, name) ->
            val rule = prefs?.getAppOpenRule(pkg)
            AppItemInfo(
                packageName = pkg,
                appName = name,
                isMonitored = monitoredPackages.contains(pkg),
                isSystemFeature = false,
                isCustomized = rule?.isCustomized == true,
                category = "popular",
                icon = getAppIcon(context, pkg)
            )
        }
    }

    fun getAllInstalledAppsAndFeatures(context: Context, monitoredPackages: Set<String>): List<AppItemInfo> {
        val list = mutableListOf<AppItemInfo>()
        val app = context.applicationContext as? com.example.DhikrApplication
        val prefs = app?.preferences

        // 1. Add System Features section first
        list.addAll(getSystemFeaturesList(context, monitoredPackages))

        // 2. Add all installed applications on the phone
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (info in installed) {
            if (info.packageName == context.packageName) continue
            // Skip packages that are already in system features
            if (SYSTEM_FEATURES.any { it.first == info.packageName }) continue

            val isLaunchable = pm.getLaunchIntentForPackage(info.packageName) != null
            val isUserApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) == 0

            // Include all launchable apps or user-installed apps
            if (isLaunchable || isUserApp) {
                val label = try { pm.getApplicationLabel(info).toString() } catch (e: Exception) { info.packageName }
                val rule = prefs?.getAppOpenRule(info.packageName)
                list.add(
                    AppItemInfo(
                        packageName = info.packageName,
                        appName = label,
                        isMonitored = monitoredPackages.contains(info.packageName),
                        isSystemFeature = false,
                        isCustomized = rule?.isCustomized == true,
                        category = "installed",
                        icon = try { pm.getApplicationIcon(info) } catch (e: Exception) { null }
                    )
                )
            }
        }
        return list
    }

    fun getInstalledLaunchableApps(context: Context, monitoredPackages: Set<String>): List<AppItemInfo> {
        return getAllInstalledAppsAndFeatures(context, monitoredPackages)
    }
}
