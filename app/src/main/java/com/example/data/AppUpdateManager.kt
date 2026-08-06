package com.example.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Vérifie périodiquement si une nouvelle version de l'application est
 * disponible sur le site (version.json) et propose la mise à jour via
 * le téléchargement de l'APK puis l'installeur Android.
 *
 * Les données locales (session, préférences, cache Room) sont préservées
 * lors d'une mise à jour d'APK par-dessus l'ancienne version.
 */
object AppUpdateManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val apkUrl: String,
        val notes: String = ""
    )

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = BuildConfig.VERSION_CHECK_URL
            if (url.isBlank()) return@withContext null
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val remoteCode = json.optInt("versionCode", 0)
                val currentCode = getCurrentVersionCode(context)
                if (remoteCode > currentCode) {
                    UpdateInfo(
                        versionCode = remoteCode,
                        versionName = json.optString("versionName", "1.2"),
                        apkUrl = json.optString("apkUrl", ""),
                        notes = json.optString("notes", "")
                    )
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    /**
     * Télécharge le nouvel APK. La réception de la fin du téléchargement
     * est gérée par [UpdateDownloadReceiver] qui lance l'installeur.
     * Retourne true si le téléchargement a été lancé.
     */
    fun downloadAndInstall(context: Context, info: UpdateInfo): Boolean {
        try {
            val downloadUrl = resolveApkUrl(info.apkUrl)
            UpdateDownloadReceiver.pendingDownloadId = -1L
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Docta na Tshombo — mise à jour ${info.versionName}")
                .setDescription("Téléchargement de la nouvelle version…")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            UpdateDownloadReceiver.pendingDownloadId = manager.enqueue(request)
            Toast.makeText(context, "Nouvelle version disponible : téléchargement en cours…", Toast.LENGTH_SHORT).show()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(context, "Ouverture de la page de téléchargement…", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resolveApkUrl(info.apkUrl))))
            } catch (_: Exception) {}
            return false
        }
    }

    private fun resolveApkUrl(apkUrl: String): String {
        if (apkUrl.startsWith("http")) return apkUrl
        val pagesRoot = BuildConfig.VERSION_CHECK_URL.substringBeforeLast("/version.json")
        val clean = apkUrl.trim().removePrefix("./").removePrefix("/")
        return "$pagesRoot/$clean"
    }
}
