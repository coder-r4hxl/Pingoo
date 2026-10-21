package com.pingoo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pingoo.app.data.local.entity.JourneyEntity
import com.pingoo.app.data.local.entity.JourneyStopEntity
import kotlinx.coroutines.flow.Flow

data class JourneyWithStops(
    val journey: JourneyEntity,
    val stops: List<JourneyStopEntity>
)

@Dao
interface JourneyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourneyStops(stops: List<JourneyStopEntity>)

    @Query("SELECT * FROM journeys WHERE id = :journeyId")
    suspend fun getJourneyById(journeyId: Long): JourneyEntity?

    @Query("SELECT * FROM journey_stops WHERE journeyId = :journeyId ORDER BY stopOrder ASC")
    suspend fun getStopsForJourney(journeyId: Long): List<JourneyStopEntity>

    @Query("UPDATE journeys SET activeStopIndex = :activeStopIndex, isCompleted = :isCompleted WHERE id = :journeyId")
    suspend fun updateJourneyProgress(journeyId: Long, activeStopIndex: Int, isCompleted: Boolean)

    @Query("DELETE FROM journeys WHERE id = :journeyId")
    suspend fun deleteJourney(journeyId: Long)
}
