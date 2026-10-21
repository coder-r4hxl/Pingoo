package com.pingoo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pingoo.app.data.local.entity.JourneyHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyHistoryDao {

    @Query("SELECT * FROM journey_history ORDER BY timestampMillis DESC")
    fun getAllHistory(): Flow<List<JourneyHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: JourneyHistoryEntity): Long

    @Delete
    suspend fun deleteHistory(history: JourneyHistoryEntity)

    @Query("DELETE FROM journey_history")
    suspend fun deleteAllHistory()
}
