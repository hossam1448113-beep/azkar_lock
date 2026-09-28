package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DhikrHistory
import com.example.data.model.DhikrItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {

    @Query("SELECT * FROM dhikr_items ORDER BY id ASC")
    fun getAllDhikrs(): Flow<List<DhikrItem>>

    @Query("SELECT * FROM dhikr_items WHERE isEnabled = 1 ORDER BY id ASC")
    fun getActiveDhikrs(): Flow<List<DhikrItem>>

    @Query("SELECT * FROM dhikr_items WHERE isEnabled = 1 ORDER BY id ASC")
    fun getActiveDhikrsSync(): List<DhikrItem>

    @Query("SELECT * FROM dhikr_items WHERE isEnabled = 1 AND (triggerMode = 'ON_UNLOCK' OR triggerMode = 'BOTH') ORDER BY id ASC")
    fun getUnlockDhikrs(): Flow<List<DhikrItem>>

    @Query("SELECT * FROM dhikr_items WHERE isEnabled = 1 AND (triggerMode = 'ON_UNLOCK' OR triggerMode = 'BOTH') ORDER BY id ASC")
    fun getUnlockDhikrsSync(): List<DhikrItem>

    @Query("SELECT * FROM dhikr_items WHERE isEnabled = 1 AND (triggerMode = 'PERIODIC_TIMER' OR triggerMode = 'BOTH' OR triggerMode = 'SCHEDULED_TIME') ORDER BY id ASC")
    fun getPeriodicDhikrsSync(): List<DhikrItem>

    @Query("SELECT * FROM dhikr_items WHERE id = :id LIMIT 1")
    suspend fun getDhikrById(id: Long): DhikrItem?

    @Query("SELECT COUNT(*) FROM dhikr_items")
    suspend fun getDhikrCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikr(item: DhikrItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDhikrs(items: List<DhikrItem>)

    @Update
    suspend fun updateDhikr(item: DhikrItem)

    @Delete
    suspend fun deleteDhikr(item: DhikrItem)

    @Query("DELETE FROM dhikr_items WHERE id = :id")
    suspend fun deleteDhikrById(id: Long)

    @Query("UPDATE dhikr_items SET totalLifetimeCount = totalLifetimeCount + :count WHERE id = :id")
    suspend fun incrementLifetimeCount(id: Long, count: Int)

    // History & Statistics
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DhikrHistory): Long

    @Query("SELECT * FROM dhikr_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<DhikrHistory>>

    @Query("SELECT * FROM dhikr_history ORDER BY timestamp DESC")
    fun getAllHistorySync(): List<DhikrHistory>

    @Query("SELECT * FROM dhikr_history WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getHistorySince(sinceTimestamp: Long): Flow<List<DhikrHistory>>

    @Query("DELETE FROM dhikr_history")
    suspend fun clearAllHistory()
}
