package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DhikrHistory
import com.example.data.model.DhikrItem
import com.example.data.model.PeriodicTimeUnit
import com.example.data.model.TargetMode
import com.example.data.model.TriggerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromTargetMode(mode: TargetMode): String = mode.name

    @TypeConverter
    fun toTargetMode(value: String): TargetMode = runCatching { TargetMode.valueOf(value) }.getOrDefault(TargetMode.COUNT_ONLY)

    @TypeConverter
    fun fromTriggerMode(mode: TriggerMode): String = mode.name

    @TypeConverter
    fun toTriggerMode(value: String): TriggerMode = runCatching { TriggerMode.valueOf(value) }.getOrDefault(TriggerMode.ON_UNLOCK)

    @TypeConverter
    fun fromPeriodicTimeUnit(unit: PeriodicTimeUnit): String = unit.name

    @TypeConverter
    fun toPeriodicTimeUnit(value: String): PeriodicTimeUnit = runCatching { PeriodicTimeUnit.valueOf(value) }.getOrDefault(PeriodicTimeUnit.MINUTES)
}

@Database(
    entities = [DhikrItem::class, DhikrHistory::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dhikrDao(): DhikrDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dhikr_lock_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_PRESETS = listOf(
            DhikrItem(
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
                virtue = "كلمتان خفيفتان على اللسان، ثقيلتان في الميزان، حبيبتان إلى الرحمن",
                targetMode = TargetMode.COUNT_ONLY,
                targetCount = 33,
                targetTimeSeconds = 30,
                triggerMode = TriggerMode.ON_UNLOCK,
                timerIntervalMinutes = 20,
                isEnabled = true
            ),
            DhikrItem(
                arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ وَأَتُوبُ إِلَيْهِ",
                virtue = "من لزم الاستغفار جعل الله له من كل هم فرجاً ومن كل ضيق مخرجاً",
                targetMode = TargetMode.COUNT_ONLY,
                targetCount = 20,
                targetTimeSeconds = 25,
                triggerMode = TriggerMode.ON_UNLOCK,
                timerIntervalMinutes = 20,
                isEnabled = true
            ),
            DhikrItem(
                arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                virtue = "من قالها كانت له عدل عشر رقاب وكُتبت له مائة حسنة ومُحيت عنه مائة سيئة",
                targetMode = TargetMode.COUNT_ONLY,
                targetCount = 10,
                targetTimeSeconds = 30,
                triggerMode = TriggerMode.ON_UNLOCK,
                timerIntervalMinutes = 30,
                isEnabled = true
            ),
            DhikrItem(
                arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
                virtue = "من صلى عليَّ صلاة واحدة صلى الله عليه بها عشراً وحُطت عنه عشر خطيئات",
                targetMode = TargetMode.COUNT_AND_TIME,
                targetCount = 10,
                targetTimeSeconds = 20,
                triggerMode = TriggerMode.BOTH,
                timerIntervalMinutes = 25,
                isEnabled = true
            ),
            DhikrItem(
                arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
                virtue = "كنز من كنوز الجنة ودواء من تسعة وتسعين داء أيسرها الهم",
                targetMode = TargetMode.COUNT_ONLY,
                targetCount = 15,
                targetTimeSeconds = 20,
                triggerMode = TriggerMode.ON_UNLOCK,
                timerIntervalMinutes = 20,
                isEnabled = true
            ),
            DhikrItem(
                arabicText = "سُبْحَانَ اللَّهِ ، وَالْحَمْدُ لِلَّهِ ، وَلَا إِلَهَ إِلَّا اللَّهُ ، وَاللَّهُ أَكْبَرُ",
                virtue = "أحب الكلام إلى الله تعالى، وغراس الجنة المبارك",
                targetMode = TargetMode.COUNT_ONLY,
                targetCount = 25,
                targetTimeSeconds = 35,
                triggerMode = TriggerMode.ON_UNLOCK,
                timerIntervalMinutes = 30,
                isEnabled = true
            ),
            DhikrItem(
                arabicText = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                virtue = "أمان الخائفين وكلمة إبراهيم عليه السلام حين أُلقي في النار",
                targetMode = TargetMode.COUNT_ONLY,
                targetCount = 15,
                targetTimeSeconds = 15,
                triggerMode = TriggerMode.ON_UNLOCK,
                timerIntervalMinutes = 20,
                isEnabled = true
            ),
            DhikrItem(
                arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ ، أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                virtue = "دعاء الكرب وتفويض الأمر لله تعالى صباحاً ومساءً",
                targetMode = TargetMode.TIME_ONLY,
                targetCount = 3,
                targetTimeSeconds = 40,
                triggerMode = TriggerMode.PERIODIC_TIMER,
                timerIntervalMinutes = 45,
                isEnabled = true
            )
        )

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).dhikrDao()
                    if (dao.getDhikrCount() == 0) {
                        dao.insertAllDhikrs(DEFAULT_PRESETS)
                    }
                }
            }
        }
    }
}
