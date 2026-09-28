package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.db.AppDatabase
import com.example.data.prefs.AppPreferences
import com.example.data.repository.DhikrRepository

class DhikrApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: DhikrRepository
        private set

    lateinit var preferences: AppPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        repository = DhikrRepository(database.dhikrDao())
        preferences = AppPreferences(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return

            val foregroundChannel = NotificationChannel(
                CHANNEL_FOREGROUND_SERVICE,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            manager.createNotificationChannel(foregroundChannel)

            val reminderChannel = NotificationChannel(
                CHANNEL_DHIKR_REMINDER,
                "تنبيهات أذكار الدورة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات هادئة عند حلول وقت الذكر أثناء تصفح الهاتف بدون إيقاف التصفح"
                enableVibration(true)
                setShowBadge(true)
            }
            manager.createNotificationChannel(reminderChannel)
        }
    }

    companion object {
        const val CHANNEL_FOREGROUND_SERVICE = "dhikr_lock_foreground_channel"
        const val CHANNEL_DHIKR_REMINDER = "dhikr_lock_reminder_channel"
        lateinit var instance: DhikrApplication
            private set
    }
}
