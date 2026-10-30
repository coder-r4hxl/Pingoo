package com.pingoo.app.ui.map

import com.pingoo.app.domain.usecase.ValidateDestinationUseCase
import com.pingoo.app.domain.usecase.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapIntegrationTest {

    private val validator = ValidateDestinationUseCase()

    @Test
    fun testMapSelectedCoordinatesValidationSuccess() {
        // Map tap returns coordinates
        val selectedLat = 28.6139
        val selectedLng = 77.2090

        val result = validator(
            name = "Map Selected Location",
            latitude = selectedLat,
            longitude = selectedLng,
            arrivalRadiusMeters = 500,
            advanceWarningMeters = 2000
        )

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun testMapSelectedCoordinatesValidationFailure() {
        // Out of bound map coordinates
        val selectedLat = 95.0000
        val selectedLng = 77.2090

        val result = validator(
            name = "Invalid Map Pin",
            latitude = selectedLat,
            longitude = selectedLng,
            arrivalRadiusMeters = 500,
            advanceWarningMeters = 2000
        )

        assertTrue(result is ValidationResult.Error)
        assertEquals("Latitude must be between -90 and 90 degrees", (result as ValidationResult.Error).message)
    }

    @Test
    fun testNavigationIntentHelperValidCoordinates() {
        assertTrue(NavigationIntentHelper.isValidCoordinates(28.6139, 77.2090))
        assertFalse(NavigationIntentHelper.isValidCoordinates(91.0, 77.2090))
        assertFalse(NavigationIntentHelper.isValidCoordinates(-91.0, 77.2090))
        assertFalse(NavigationIntentHelper.isValidCoordinates(28.6139, 181.0))
        assertFalse(NavigationIntentHelper.isValidCoordinates(28.6139, -181.0))
    }

    @Test
    fun testNavigationIntentHelperBuildGeoUriString() {
        val uri = NavigationIntentHelper.buildGeoUriString(28.6139, 77.2090, "Connaught Place")
        assertTrue(uri.startsWith("geo:28.613900,77.209000?q=28.613900,77.209000("))
        assertTrue(uri.contains("Connaught"))
    }

    @Test
    fun testGeocodingHelperFallbackCoordinates() {
        val fallback = GeocodingHelper.formatFallbackCoordinates(28.6139, 77.2090)
        assertEquals("Coordinates (28.6139, 77.2090)", fallback)
    }
}
