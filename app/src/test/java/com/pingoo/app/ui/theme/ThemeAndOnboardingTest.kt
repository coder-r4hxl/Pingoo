package com.pingoo.app.ui.theme

import com.pingoo.app.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeAndOnboardingTest {

    @Test
    fun testDefaultUserSettingsHasExpectedThemeState() {
        val settings = UserSettings()
        assertFalse(settings.darkTheme)
        assertEquals(500, settings.defaultRadiusMeters)
    }

    @Test
    fun testUserSettingsDarkThemeToggle() {
        val settings = UserSettings(darkTheme = true)
        assertTrue(settings.darkTheme)
    }
}
