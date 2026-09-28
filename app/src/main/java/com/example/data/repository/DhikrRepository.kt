package com.example.data.repository

import com.example.data.db.DhikrDao
import com.example.data.model.DhikrHistory
import com.example.data.model.DhikrItem
import kotlinx.coroutines.flow.Flow

class DhikrRepository(private val dhikrDao: DhikrDao) {

    val allDhikrs: Flow<List<DhikrItem>> = dhikrDao.getAllDhikrs()
    val activeDhikrs: Flow<List<DhikrItem>> = dhikrDao.getActiveDhikrs()
    val allHistory: Flow<List<DhikrHistory>> = dhikrDao.getAllHistory()

    suspend fun getDhikrById(id: Long): DhikrItem? = dhikrDao.getDhikrById(id)

    suspend fun insertDhikr(item: DhikrItem): Long = dhikrDao.insertDhikr(item)

    suspend fun updateDhikr(item: DhikrItem) = dhikrDao.updateDhikr(item)

    suspend fun deleteDhikr(item: DhikrItem) = dhikrDao.deleteDhikr(item)

    suspend fun deleteDhikrById(id: Long) = dhikrDao.deleteDhikrById(id)

    suspend fun recordCompletion(dhikrId: Long, dhikrText: String, count: Int, seconds: Int) {
        val history = DhikrHistory(
            dhikrId = dhikrId,
            dhikrText = dhikrText,
            countDone = count,
            secondsDone = seconds,
            timestamp = System.currentTimeMillis()
        )
        dhikrDao.insertHistory(history)
        if (dhikrId > 0) {
            dhikrDao.incrementLifetimeCount(dhikrId, count)
        }
    }

    suspend fun clearAllHistory() = dhikrDao.clearAllHistory()

    fun getHistorySince(since: Long): Flow<List<DhikrHistory>> = dhikrDao.getHistorySince(since)
}
