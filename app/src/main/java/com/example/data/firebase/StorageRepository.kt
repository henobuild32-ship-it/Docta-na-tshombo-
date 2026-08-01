package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Repository centralisé pour la gestion des fichiers avec Supabase Storage.
 * Remplace Firebase Storage tout en conservant Firebase Auth, Firestore et FCM.
 */
class StorageRepository(private val context: Context) {

    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Storage)
            install(Auth)
        }
    }

    private val bucket
        get() = supabase.storage.from(BuildConfig.SUPABASE_BUCKET_NAME)

    /**
     * Échange le token Firebase ID contre un JWT Supabase via l'Edge Function,
     * puis importe la session. Appelé avant chaque opération Storage.
     */
    private suspend fun ensureAuthenticated() {
        val currentToken = supabase.auth.currentAccessTokenOrNull()
        if (currentToken != null) return

        val firebaseToken = AuthRepository().getIdToken().getOrElse {
            throw StorageException.UnknownError("Impossible d'obtenir le token Firebase")
        }

        val accessToken = exchangeFirebaseToken(firebaseToken)
        val session = UserSession(
            accessToken = accessToken,
            refreshToken = "",
            providerRefreshToken = null,
            providerToken = null,
            expiresIn = 3600,
            tokenType = "bearer",
            user = null,
            type = "bearer",
            expiresAt = kotlinx.datetime.Clock.System.now().plus(3600.seconds)
        )
        supabase.auth.importSession(session)
    }

    /**
     * Appelle l'Edge Function Supabase exchange-token
     */
    private suspend fun exchangeFirebaseToken(firebaseToken: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL("${BuildConfig.SUPABASE_URL}/functions/v1/exchange-token")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")

                val body = "{\"firebaseToken\":\"$firebaseToken\"}"
                conn.outputStream.use { it.write(body.toByteArray()) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val response = stream?.bufferedReader()?.use { it.readText() } ?: ""

                if (code !in 200..299) {
                    throw StorageException.UnknownError("Échange de token échoué ($code): $response")
                }

                // Extraire accessToken de la réponse JSON
                val accessToken = Regex("\"accessToken\"\\s*:\\s*\"([^\"]+)\"").find(response)
                    ?.groupValues
                    ?.get(1)
                    ?: throw StorageException.UnknownError("Réponse invalide de l'Edge Function")

                accessToken
            } finally {
                conn.disconnect()
            }
        }
    }

    /**
     * Récupère le nom du fichier depuis son URI
     */
    private suspend fun getFileName(uri: Uri): String {
        var name = "file_${System.currentTimeMillis()}"
        withContext(Dispatchers.IO) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        return name
    }

    /**
     * Lit les bytes depuis une URI
     */
    private suspend fun getBytesFromUri(uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw StorageException.UnknownError("Impossible de lire le fichier")
        }
    }

    /**
     * Upload un fichier vers Supabase Storage
     * @param uri URI du fichier à uploader
     * @param path Chemin de destination dans le bucket
     * @return Result contenant l'URL signée ou une erreur
     */
    private suspend fun uploadFile(uri: Uri, path: String): Result<String> {
        return try {
            ensureAuthenticated()
            val bytes = getBytesFromUri(uri)

            // Upload vers Supabase Storage (upsert permet d'écraser un fichier existant)
            bucket.upload(path, bytes) { upsert = true }

            // Générer une URL signée valide pendant 1 heure
            val signedUrl = bucket.createSignedUrl(path, expiresIn = 1.hours)
            Result.success(signedUrl)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Upload une photo de profil patient
     * @param uid ID du patient
     * @param imageUri URI de l'image
     * @return Result contenant l'URL signée
     */
    suspend fun uploadProfilePhoto(uid: String, imageUri: Uri): Result<String> {
        val fileName = getFileName(imageUri)
        val extension = fileName.substringAfterLast(".", "jpg")
        return uploadFile(imageUri, "patients/$uid/profile/photo.$extension")
    }

    /**
     * Upload un document pour un médecin
     * @param doctorId ID du médecin
     * @param documentUri URI du document
     * @param fileName Nom du fichier
     * @return Result contenant l'URL signée
     */
    suspend fun uploadDoctorDocument(doctorId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "doctors/$doctorId/documents/$fileName")
    }

    /**
     * Upload une ordonnance PDF pour un patient
     * @param patientId ID du patient
     * @param pdfUri URI du fichier PDF
     * @return Result contenant l'URL signée
     */
    suspend fun uploadPrescriptionPdf(patientId: String, pdfUri: Uri): Result<String> {
        val fileName = getFileName(pdfUri)
        return uploadFile(pdfUri, "patients/$patientId/prescriptions/$fileName")
    }

    /**
     * Upload un document médical pour un patient
     * @param patientId ID du patient
     * @param documentUri URI du document
     * @param fileName Nom du fichier
     * @return Result contenant l'URL signée
     */
    suspend fun uploadMedicalDocument(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/medical-documents/$fileName")
    }

    /**
     * Upload un résultat d'analyse pour un patient
     * @param patientId ID du patient
     * @param documentUri URI du document
     * @param fileName Nom du fichier
     * @return Result contenant l'URL signée
     */
    suspend fun uploadLabResult(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/analyses/$fileName")
    }

    /**
     * Upload une radio/imagerie pour un patient
     * @param patientId ID du patient
     * @param documentUri URI du document
     * @param fileName Nom du fichier
     * @return Result contenant l'URL signée
     */
    suspend fun uploadRadiology(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/radiology/$fileName")
    }

    /**
     * Upload une échographie pour un patient
     * @param patientId ID du patient
     * @param documentUri URI du document
     * @param fileName Nom du fichier
     * @return Result contenant l'URL signée
     */
    suspend fun uploadUltrasound(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/ultrasound/$fileName")
    }

    /**
     * Upload une pièce jointe de conversation
     * @param conversationId ID de la conversation
     * @param fileUri URI du fichier
     * @param fileName Nom du fichier
     * @return Result contenant l'URL signée
     */
    suspend fun uploadChatAttachment(conversationId: String, fileUri: Uri, fileName: String): Result<String> {
        return uploadFile(fileUri, "conversations/$conversationId/attachments/$fileName")
    }

    /**
     * Récupère une URL signée pour un fichier
     * @param path Chemin du fichier dans le bucket
     * @param expiresIn Durée de validité (défaut: 1 heure)
     * @return Result contenant l'URL signée
     */
    suspend fun getSignedUrl(path: String, expiresIn: Long = 3600): Result<String> {
        return try {
            ensureAuthenticated()
            val url = bucket.createSignedUrl(path, expiresIn = expiresIn.seconds)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Télécharge un fichier depuis Supabase Storage
     * @param path Chemin du fichier dans le bucket
     * @return Result contenant les bytes du fichier
     */
    suspend fun downloadFile(path: String): Result<ByteArray> {
        return try {
            ensureAuthenticated()
            val bytes = bucket.downloadAuthenticated(path)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Supprime un fichier de Supabase Storage
     * @param path Chemin du fichier dans le bucket
     * @return Result success ou failure
     */
    suspend fun deleteFile(path: String): Result<Unit> {
        return try {
            ensureAuthenticated()
            bucket.delete(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Vérifie si un fichier existe
     * @param path Chemin du fichier dans le bucket
     * @return true si le fichier existe, false sinon
     */
    suspend fun fileExists(path: String): Boolean {
        return try {
            bucket.exists(path)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Map les exceptions techniques en exceptions métier explicites
     */
    private fun mapException(e: Exception): StorageException {
        return when (e) {
            is HttpRequestException -> StorageException.NetworkError(e.message ?: "Erreur réseau")
            is RestException -> {
                when (e.statusCode) {
                    404 -> StorageException.FileNotFound(e.description ?: "Fichier non trouvé")
                    401, 403 -> StorageException.PermissionDenied(e.description ?: "Permission refusée")
                    413 -> StorageException.FileTooLarge(e.description ?: "Fichier trop volumineux")
                    else -> StorageException.UploadFailed(e.description ?: e.error ?: "Erreur d'upload")
                }
            }
            is IOException -> StorageException.NetworkError(e.message ?: "Erreur réseau")
            else -> StorageException.UnknownError(e.message ?: "Erreur inconnue", e)
        }
    }
}

/**
 * Exceptions spécifiques au stockage
 */
sealed class StorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class NetworkError(message: String) : StorageException(message)
    class FileNotFound(message: String) : StorageException(message)
    class PermissionDenied(message: String) : StorageException(message)
    class FileTooLarge(message: String) : StorageException(message)
    class InvalidFormat(message: String) : StorageException(message)
    class UploadFailed(message: String) : StorageException(message)
    class DeleteFailed(message: String) : StorageException(message)
    class UnknownError(message: String, cause: Throwable? = null) : StorageException(message, cause)
}
