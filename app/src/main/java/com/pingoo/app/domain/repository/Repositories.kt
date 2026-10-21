package com.pingoo.app.domain.repository

import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface DestinationRepository {
    fun getAllDestinations(): Flow<List<Destination>>
    fun getActiveDestinations(): Flow<List<Destination>>
    suspend fun getDestinationById(id: Long): Destination?
    suspend fun insertDestination(destination: Destination): Long
    suspend fun updateDestination(destination: Destination)
    suspend fun deleteDestination(destination: Destination)
    suspend fun toggleDestinationActive(id: Long, isActive: Boolean)
}

interface SettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateLanguage(languageCode: String)
    suspend fun updateVibrationEnabled(enabled: Boolean)
    suspend fun updateDefaultRadius(radiusMeters: Int)
}
