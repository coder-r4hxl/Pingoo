package com.pingoo.app.domain.updater

import java.io.File
import java.security.MessageDigest
import java.util.Locale

object AppUpdateEngine {

    fun isUpdateAvailable(
        currentVersionCode: Int,
        candidateVersionCode: Int
    ): Boolean {
        return candidateVersionCode > currentVersionCode
    }

    fun validateApkUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase(Locale.US)
        return lower.startsWith("https://") && lower.endsWith(".apk")
    }

    fun verifySha256Checksum(file: File, expectedSha256: String): Boolean {
        if (!file.exists() || !file.isFile || expectedSha256.isBlank()) {
            return false
        }
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            file.inputStream().use { inputStream ->
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val calculatedHash = digest.digest().joinToString("") { "%02x".format(it) }
            return calculatedHash.equals(expectedSha256.trim(), ignoreCase = true)
        } catch (e: Exception) {
            return false
        }
    }
}
