package com.pingoo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pingoo.app.data.local.entity.DestinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {
    @Query("SELECT * FROM destinations ORDER BY createdAt DESC")
    fun getAllDestinations(): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveDestinations(): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE id = :id LIMIT 1")
    suspend fun getDestinationById(id: Long): DestinationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: DestinationEntity): Long

    @Update
    suspend fun updateDestination(destination: DestinationEntity)

    @Delete
    suspend fun deleteDestination(destination: DestinationEntity)

    @Query("UPDATE destinations SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveState(id: Long, isActive: Boolean)
}
