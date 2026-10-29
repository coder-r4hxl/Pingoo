package com.pingoo.app.data.repository

import com.pingoo.app.data.datastore.UserPreferencesRepository
import com.pingoo.app.data.local.dao.DestinationDao
import com.pingoo.app.data.local.entity.DestinationEntity
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.UserSettings
import com.pingoo.app.domain.repository.DestinationRepository
import com.pingoo.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DestinationRepositoryImpl(
    private val destinationDao: DestinationDao
) : DestinationRepository {

    override fun getAllDestinations(): Flow<List<Destination>> {
        return destinationDao.getAllDestinations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveDestinations(): Flow<List<Destination>> {
        return destinationDao.getActiveDestinations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDestinationById(id: Long): Destination? {
        return destinationDao.getDestinationById(id)?.toDomain()
    }

    override suspend fun insertDestination(destination: Destination): Long {
        return destinationDao.insertDestination(DestinationEntity.fromDomain(destination))
    }

    override suspend fun updateDestination(destination: Destination) {
        destinationDao.updateDestination(DestinationEntity.fromDomain(destination))
    }

    override suspend fun deleteDestination(destination: Destination) {
        destinationDao.deleteDestination(DestinationEntity.fromDomain(destination))
    }

    override suspend fun toggleDestinationActive(id: Long, isActive: Boolean) {
        destinationDao.updateActiveState(id, isActive)
    }
}

class SettingsRepositoryImpl(
    private val userPreferencesRepository: UserPreferencesRepository
) : SettingsRepository {

    override fun getUserSettings(): Flow<UserSettings> {
        return userPreferencesRepository.userSettings
    }

    override suspend fun updateLanguage(languageCode: String) {
        userPreferencesRepository.setLanguage(languageCode)
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        userPreferencesRepository.setVibrationEnabled(enabled)
    }

    override suspend fun updateDefaultRadius(radiusMeters: Int) {
        userPreferencesRepository.setDefaultRadius(radiusMeters)
    }
}
