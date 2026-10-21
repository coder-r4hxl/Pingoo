package com.pingoo.app.domain.usecase

import com.pingoo.app.domain.model.Destination

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

class ValidateDestinationUseCase {
    operator fun invoke(
        name: String,
        latitude: Double?,
        longitude: Double?,
        arrivalRadiusMeters: Int,
        advanceWarningMeters: Int
    ): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("Destination name cannot be empty")
        }
        if (latitude == null || latitude < -90.0 || latitude > 90.0) {
            return ValidationResult.Error("Latitude must be between -90 and 90 degrees")
        }
        if (longitude == null || longitude < -180.0 || longitude > 180.0) {
            return ValidationResult.Error("Longitude must be between -180 and 180 degrees")
        }
        if (arrivalRadiusMeters < 50 || arrivalRadiusMeters > 50000) {
            return ValidationResult.Error("Arrival radius must be between 50m and 50km")
        }
        if (advanceWarningMeters < 0 || advanceWarningMeters < arrivalRadiusMeters) {
            return ValidationResult.Error("Advance warning radius must be greater than or equal to arrival radius")
        }
        return ValidationResult.Success
    }
}
