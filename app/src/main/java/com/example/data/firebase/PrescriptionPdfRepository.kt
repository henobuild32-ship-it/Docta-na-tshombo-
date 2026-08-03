package com.example.data.firebase

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore

class PrescriptionPdfRepository(private val context: Context) {
    private val storage = StorageRepository(context)

    suspend fun download(prescription: FirestorePrescription): Result<String> = runCatching {
        require(prescription.pdfPath.isNotBlank()) { "Le PDF est encore en cours de génération" }
        val bytes = storage.downloadFile(prescription.pdfPath).getOrThrow()
        val fileName = "${prescription.reference.ifBlank { "ordonnance-${prescription.id}" }}.pdf"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Docta")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Impossible de créer le fichier dans Téléchargements")
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: error("Impossible d’écrire le PDF")
        fileName
    }
}
