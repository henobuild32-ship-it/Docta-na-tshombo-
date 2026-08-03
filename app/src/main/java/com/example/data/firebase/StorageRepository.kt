package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.BuildConfig
import com.example.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.time.Duration.Companion.minutes

/**
 * Repository centralisé pour la gestion des fichiers avec Supabase Storage.
 * 100% Supabase, sans Firebase : le client partagé fournit l'authentification.
 */
class StorageRepository(private val context: Context) {

    private val supabase = SupabaseClientProvider.client

    private val bucket: BucketApi
        get() = supabase.storage.from(BuildConfig.SUPABASE_BUCKET_NAME)

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
                ?: throw FileStorageException.UnknownError("Impossible de lire le fichier")
        }
    }

    /**
     * Upload un fichier vers Supabase Storage
     * @param uri URI du fichier à uploader
     * @param path Chemin de destination dans le bucket
     * @return Result contenant le chemin stable du fichier
     */
    private suspend fun uploadFile(uri: Uri, path: String): Result<String> {
        return try {
            val bytes = getBytesFromUri(uri)
            bucket.upload(path, bytes) { upsert = true }
            // Ne pas persister une URL signée éphémère : le chemin stable
            // permet de régénérer une URL à la demande.
            Result.success(path)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Upload une photo de profil patient
     */
    suspend fun uploadProfilePhoto(uid: String, imageUri: Uri): Result<String> {
        val fileName = getFileName(imageUri)
        val extension = fileName.substringAfterLast(".", "jpg")
        return uploadFile(imageUri, "patients/$uid/profile/photo.$extension")
    }

    /**
     * Upload un document pour un médecin
     */
    suspend fun uploadDoctorDocument(doctorId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "doctors/$doctorId/documents/$fileName")
    }

    /**
     * Upload une ordonnance PDF pour un patient
     */
    suspend fun uploadPrescriptionPdf(patientId: String, pdfUri: Uri): Result<String> {
        val fileName = getFileName(pdfUri)
        return uploadFile(pdfUri, "patients/$patientId/prescriptions/$fileName")
    }

    /**
     * Upload un document médical pour un patient
     */
    suspend fun uploadMedicalDocument(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/medical-documents/$fileName")
    }

    /**
     * Upload un résultat d'analyse pour un patient
     */
    suspend fun uploadLabResult(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/analyses/$fileName")
    }

    /**
     * Upload une radio/imagerie pour un patient
     */
    suspend fun uploadRadiology(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/radiology/$fileName")
    }

    /**
     * Upload une échographie pour un patient
     */
    suspend fun uploadUltrasound(patientId: String, documentUri: Uri, fileName: String): Result<String> {
        return uploadFile(documentUri, "patients/$patientId/ultrasound/$fileName")
    }

    /**
     * Upload une pièce jointe de conversation
     */
    suspend fun uploadChatAttachment(conversationId: String, fileUri: Uri, fileName: String): Result<String> {
        return uploadFile(fileUri, "conversations/$conversationId/attachments/$fileName")
    }

    /**
     * Récupère une URL signée pour un fichier
     * @param path Chemin du fichier dans le bucket
     * @param expiresInMinutes Durée de validité en minutes (défaut: 60 minutes)
     */
    suspend fun getSignedUrl(path: String, expiresInMinutes: Long = 60): Result<String> {
        return try {
            val url = bucket.createSignedUrl(path, expiresIn = expiresInMinutes.minutes)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Télécharge un fichier depuis Supabase Storage
     */
    suspend fun downloadFile(path: String): Result<ByteArray> {
        return try {
            val bytes = bucket.downloadAuthenticated(path)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Supprime un fichier de Supabase Storage
     */
    suspend fun deleteFile(path: String): Result<Unit> {
        return try {
            bucket.delete(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Vérifie si un fichier existe
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
    private fun mapException(e: Exception): FileStorageException {
        return when (e) {
            is IOException -> FileStorageException.NetworkError(e.message ?: "Erreur réseau")
            is FileStorageException -> e // Déjà une FileStorageException
            else -> {
                val message = e.message ?: "Erreur inconnue"
                when {
                    message.contains("not found", ignoreCase = true) ->
                        FileStorageException.FileNotFound(message)
                    message.contains("permission", ignoreCase = true) ->
                        FileStorageException.PermissionDenied(message)
                    message.contains("size", ignoreCase = true) ->
                        FileStorageException.FileTooLarge(message)
                    message.contains("upload", ignoreCase = true) ->
                        FileStorageException.UploadFailed(message)
                    else -> FileStorageException.UnknownError(message, e)
                }
            }
        }
    }
}

/**
 * Exceptions spécifiques au stockage de fichiers
 * Nom différent pour éviter le conflit avec io.github.jan.supabase.storage.StorageException
 */
sealed class FileStorageException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String) : FileStorageException(message, null)
    class FileNotFound(message: String) : FileStorageException(message, null)
    class PermissionDenied(message: String) : FileStorageException(message, null)
    class FileTooLarge(message: String) : FileStorageException(message, null)
    class InvalidFormat(message: String) : FileStorageException(message, null)
    class UploadFailed(message: String) : FileStorageException(message, null)
    class DeleteFailed(message: String) : FileStorageException(message, null)
    class UnknownError(message: String, cause: Throwable? = null) : FileStorageException(message, cause)
}
