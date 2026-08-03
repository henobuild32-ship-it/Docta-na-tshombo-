package com.example.data.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone

/**
 * Modèles mappés sur les colonnes PostgREST (Supabase).
 * Les classes conservent leur nom historique (Firestore*) pour limiter les
 * changements dans l'UI ; la persistance est 100% Supabase, sans Firebase.
 */

@Serializable
data class FirestoreUser(
    @SerialName("id") val uid: String = "",
    val email: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val phone: String = "",
    @SerialName("birth_date") val birthDate: String = "",
    val gender: String = "",
    @SerialName("blood_type") val bloodType: String = "",
    val allergies: List<String> = emptyList(),
    @SerialName("medical_history") val medicalHistory: List<String> = emptyList(),
    val role: String = ROLE_PATIENT,
    @SerialName("photo_path") val photoUrl: String = "",
    val specialty: String = "",
    @SerialName("rpps_number") val rppsNumber: String = "",
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_senior_mode") val isSeniorMode: Boolean = false,
    @SerialName("onesignal_subscription_id") val onesignalSubscriptionId: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    companion object {
        const val ROLE_PATIENT = "patient"
        const val ROLE_DOCTOR = "doctor"
        const val ROLE_ADMIN = "admin"
    }

    val fullName: String get() = "$firstName $lastName".trim()
    val displayName: String get() = if (fullName.isNotBlank()) fullName else email
}

@Serializable
data class FirestoreDoctor(
    @SerialName("id") val id: String = "",
    @Transient val userId: String = "",
    val name: String = "",
    val specialty: String = "",
    val address: String = "",
    @SerialName("photo_path") val photoUrl: String = "",
    val rating: Float = 0f,
    @SerialName("review_count") val reviewCount: Int = 0,
    val price: String = "",
    val bio: String = "",
    @SerialName("professional_number") val rpps: String = "",
    @SerialName("is_online") val isOnlineForTeleconsult: Boolean = true,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("available_slots") val availableSlots: List<String> = emptyList(),
    @SerialName("consultation_types") val consultationTypes: List<String> = listOf("PRESENTIEL", "TELECONSULTATION"),
    // ===== Parcours & Formation =====
    val degree: String = "",
    @SerialName("education_level") val educationLevel: String = "",
    @SerialName("study_duration") val studyDuration: String = "",
    val universities: List<String> = emptyList(),
    val trainings: List<String> = emptyList(),
    @SerialName("graduation_year") val graduationYear: String = "",
    // ===== Documents & Vérification =====
    @SerialName("has_documents") val hasDocuments: Boolean = false,
    @SerialName("documents_verified") val documentsVerified: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    val educationSummary: String
        get() = buildString {
            if (degree.isNotBlank()) append(degree)
            if (educationLevel.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(educationLevel)
            }
        }
}

@Serializable
data class DoctorEducation(
    val degree: String = "",
    @SerialName("education_level") val educationLevel: String = "",
    @SerialName("study_duration") val studyDuration: String = "",
    val universities: List<String> = emptyList(),
    val trainings: List<String> = emptyList(),
    @SerialName("graduation_year") val graduationYear: String = ""
)

@Serializable
data class FirestoreAppointment(
    @SerialName("id") val id: String = "",
    @SerialName("patient_id") val patientId: String = "",
    @SerialName("patient_name") val patientName: String = "",
    @SerialName("doctor_id") val doctorId: String = "",
    @SerialName("doctor_name") val doctorName: String = "",
    @SerialName("doctor_specialty") val doctorSpecialty: String = "",
    @SerialName("doctor_avatar") val doctorAvatar: String = "",
    val date: String = "",
    val time: String = "",
    val type: String = "TELECONSULTATION",
    val motif: String = "",
    val status: String = STATUS_PENDING,
    @SerialName("patient_note") val userNote: String = "",
    val address: String = "",
    @Transient val consultationId: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_CANCELLED = "cancelled"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_NO_SHOW = "no_show"
    }
}

@Serializable
data class FirestoreConversation(
    @SerialName("id") val id: String = "",
    @SerialName("participant_ids") val participantIds: List<String> = emptyList(),
    @SerialName("participant_names") val participantNames: List<String> = emptyList(),
    @SerialName("last_message") val lastMessage: String = "",
    @SerialName("last_message_sender_id") val lastMessageSenderId: String = "",
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @Transient val unreadCount: Map<String, Int> = emptyMap(),
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class FirestoreMessage(
    @SerialName("id") val id: String = "",
    @SerialName("conversation_id") val conversationId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    @SerialName("sender_name") val senderName: String = "",
    @SerialName("body") val text: String = "",
    @SerialName("attachment_path") val attachmentUrl: String = "",
    @SerialName("attachment_name") val attachmentName: String = "",
    val status: String = STATUS_SENT,
    @SerialName("created_at") val createdAt: String? = null
) {
    companion object {
        const val STATUS_SENT = "sent"
        const val STATUS_DELIVERED = "delivered"
        const val STATUS_READ = "read"
    }
}

@Serializable
data class FirestorePrescription(
    @SerialName("id") val id: String = "",
    @SerialName("doctor_id") val doctorId: String = "",
    @SerialName("doctor_name") val doctorName: String = "",
    @SerialName("patient_id") val patientId: String = "",
    @SerialName("patient_name") val patientName: String = "",
    @SerialName("medicine") val medicinesSummary: String = "",
    val diagnosis: String = "",
    val dosage: String = "",
    val duration: String = "",
    val reference: String = "",
    @SerialName("signature_hash") val signatureHash: String = "",
    val notes: String = "",
    @Transient val isSigned: Boolean = false,
    @Transient val pdfUrl: String = "",
    @SerialName("pdf_path") val pdfPath: String = "",
    @SerialName("pdf_generated_at") val pdfGeneratedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class FirestoreMedicalProfile(
    @SerialName("patient_id") val patientId: String = "",
    val gender: String = "",
    @SerialName("birth_date") val birthDate: String = "",
    @SerialName("blood_type") val bloodType: String = "",
    val allergies: List<String> = emptyList(),
    @SerialName("medical_history") val medicalHistory: List<String> = emptyList(),
    @SerialName("current_treatments") val currentTreatments: List<String> = emptyList(),
    @SerialName("emergency_contact_name") val emergencyContactName: String = "",
    @SerialName("emergency_contact_phone") val emergencyContactPhone: String = "",
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class FirestoreMedicationReminder(
    @SerialName("id") val id: String = "",
    @SerialName("patient_id") val patientId: String = "",
    @SerialName("medicine_name") val medicineName: String = "",
    val dosage: String = "",
    val frequency: String = "",
    @SerialName("time_of_day") val timeOfDay: String = "",
    @SerialName("is_taken_today") val isTakenToday: Boolean = false,
    @SerialName("start_date") val startDate: String = "",
    val notes: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class FirestoreNotification(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    @SerialName("related_id") val relatedId: String = "",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class FirestoreSpecialty(
    @SerialName("id") val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerialName("icon_name") val iconName: String = "",
    @SerialName("display_order") val displayOrder: Int = 0
)

@Serializable
data class FirestoreReview(
    @SerialName("id") val id: String = "",
    @SerialName("doctor_id") val doctorId: String = "",
    @SerialName("patient_id") val patientId: String = "",
    @Transient val patientName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Aide pour l'UI : convertir les timestamps ISO-8601 renvoyés par Supabase.
 * Anciennement Firestore Timestamp.toDate().
 */
fun String?.toDisplayDate(): String {
    if (isNullOrBlank()) return ""
    return try {
        Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    } catch (e: Exception) {
        take(10)
    }
}

fun String?.toDisplayTime(): String {
    if (isNullOrBlank()) return ""
    return try {
        val dt = Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        ""
    }
}
