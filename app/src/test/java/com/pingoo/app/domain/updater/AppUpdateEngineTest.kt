package com.pingoo.app.domain.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateEngineTest {

    @Test
    fun testUpdateAvailableForNewerVersionCode() {
        // Candidate version 2 > installed version 1
        assertTrue(AppUpdateEngine.isUpdateAvailable(currentVersionCode = 1, candidateVersionCode = 2))
    }

    @Test
    fun testUpdateNotAvailableForEqualOrOlderVersionCode() {
        // Candidate version 1 == installed version 1
        assertFalse(AppUpdateEngine.isUpdateAvailable(currentVersionCode = 1, candidateVersionCode = 1))

        // Candidate version 1 < installed version 2
        assertFalse(AppUpdateEngine.isUpdateAvailable(currentVersionCode = 2, candidateVersionCode = 1))
    }

    @Test
    fun testValidateApkUrl() {
        // Valid HTTPS URL ending in .apk
        assertTrue(AppUpdateEngine.validateApkUrl("https://github.com/coder-r4hxl/Pingoo/releases/download/v1.0.0/app-debug.apk"))

        // Insecure HTTP URL rejected
        assertFalse(AppUpdateEngine.validateApkUrl("http://github.com/coder-r4hxl/Pingoo/releases/download/v1.0.0/app-debug.apk"))

        // Non-APK URL rejected
        assertFalse(AppUpdateEngine.validateApkUrl("https://github.com/coder-r4hxl/Pingoo/releases/download/v1.0.0/app-debug.zip"))

        // Blank URL rejected
        assertFalse(AppUpdateEngine.validateApkUrl(""))
    }
}
