package com.pingoo.app.service.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.pingoo.app.domain.updater.AppUpdateEngine
import com.pingoo.app.domain.updater.ReleaseManifest
import com.pingoo.app.domain.updater.UpdateStatus
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkDownloader(private val context: Context) {

    suspend fun downloadApk(
        manifest: ReleaseManifest,
        onProgress: (Int) -> Unit
    ): UpdateStatus = withContext(Dispatchers.IO) {
        if (!AppUpdateEngine.validateApkUrl(manifest.apkUrl)) {
            return@withContext UpdateStatus.Error("Invalid or insecure update URL")
        }

        try {
            val url = URL(manifest.apkUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext UpdateStatus.Error("Server returned HTTP ${connection.responseCode}")
            }

            val fileLength = connection.contentLength
            val updatesDir = File(context.cacheDir, "apk_updates").apply { mkdirs() }
            val outputFile = File(updatesDir, "pingoo-update-v${manifest.versionCode}.apk")

            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            val progress = ((total * 100) / fileLength).toInt()
                            onProgress(progress)
                        }
                        output.write(data, 0, count)
                    }
                }
            }

            // Verify SHA-256 checksum if provided
            if (manifest.sha256Checksum.isNotBlank()) {
                val isChecksumValid = AppUpdateEngine.verifySha256Checksum(outputFile, manifest.sha256Checksum)
                if (!isChecksumValid) {
                    outputFile.delete()
                    return@withContext UpdateStatus.Error("APK integrity check failed (SHA-256 mismatch)")
                }
            }

            UpdateStatus.ReadyToInstall(outputFile.absolutePath, manifest)
        } catch (e: Exception) {
            UpdateStatus.Error("Download failed: ${e.localizedMessage ?: "Network error"}")
        }
    }

    fun promptInstall(apkFile: File): Intent {
        val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
        } else {
            Uri.fromFile(apkFile)
        }

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }
}
