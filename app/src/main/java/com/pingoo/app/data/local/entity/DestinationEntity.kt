package com.pingoo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pingoo.app.domain.model.Destination

@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val arrivalRadiusMeters: Int,
    val advanceWarningRadiusMeters: Int,
    val isActive: Boolean,
    val alarmSound: String,
    val vibrate: Boolean,
    val createdAt: Long
) {
    fun toDomain(): Destination = Destination(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        arrivalRadiusMeters = arrivalRadiusMeters,
        advanceWarningRadiusMeters = advanceWarningRadiusMeters,
        isActive = isActive,
        alarmSound = alarmSound,
        vibrate = vibrate,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(destination: Destination): DestinationEntity = DestinationEntity(
            id = destination.id,
            name = destination.name,
            latitude = destination.latitude,
            longitude = destination.longitude,
            arrivalRadiusMeters = destination.arrivalRadiusMeters,
            advanceWarningRadiusMeters = destination.advanceWarningRadiusMeters,
            isActive = destination.isActive,
            alarmSound = destination.alarmSound,
            vibrate = destination.vibrate,
            createdAt = destination.createdAt
        )
    }
}
