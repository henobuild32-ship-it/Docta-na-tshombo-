package com.example.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Reçoit l'événement « téléchargement terminé » de l'APK puis lance
 * l'installeur Android. Les données locales sont conservées (mise à jour
 * par-dessus l'ancienne version).
 */
class UpdateDownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (id != pendingDownloadId) return
        pendingDownloadId = -1L

        try {
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = manager.getUriForDownloadedFile(id)
            val mime = manager.getMimeTypeForDownloadedFile(id)

            val apkFile = File(context.getExternalFilesDir(null), "docta-update.apk")
            context.contentResolver.openInputStream(uri)?.use { input ->
                apkFile.outputStream().use { output -> input.copyTo(output) }
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile),
                    mime ?: "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (_: Exception) {
        }
    }

    companion object {
        @Volatile var pendingDownloadId: Long = -1L
    }
}