package com.pingoo.app.domain.updater

data class ReleaseManifest(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256Checksum: String,
    val releaseNotes: String = ""
)

sealed class UpdateStatus {
    data object Idle : UpdateStatus()
    data object Checking : UpdateStatus()
    data class UpdateAvailable(val manifest: ReleaseManifest) : UpdateStatus()
    data object UpToDate : UpdateStatus()
    data class Downloading(val progressPercent: Int) : UpdateStatus()
    data class ReadyToInstall(val apkPath: String, val manifest: ReleaseManifest) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}
