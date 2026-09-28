package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr_history")
data class DhikrHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dhikrId: Long,
    val dhikrText: String,
    val countDone: Int,
    val secondsDone: Int,
    val timestamp: Long = System.currentTimeMillis()
)
