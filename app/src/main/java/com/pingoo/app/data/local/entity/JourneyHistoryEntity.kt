package com.pingoo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journey_history")
data class JourneyHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalStops: Int,
    val reachedStops: Int,
    val isCompleted: Boolean,
    val durationSeconds: Long,
    val timestampMillis: Long = System.currentTimeMillis()
)
