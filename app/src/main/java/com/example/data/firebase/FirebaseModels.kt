package com.example.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class FirestoreUser(
    @DocumentId val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val role: String = ROLE_PATIENT,
    val photoUrl: String = "",
    val specialty: String = "",
    val rppsNumber: String = "",
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val isSeniorMode: Boolean = false,
    val fcmToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val ROLE_PATIENT = "patient"
        const val ROLE_DOCTOR = "doctor"
        const val ROLE_ADMIN = "admin"
        const val COLLECTION = "users"
    }

    val fullName: String get() = "$firstName $lastName".trim()
    val displayName: String get() = if (fullName.isNotBlank()) fullName else email
}

data class FirestoreDoctor(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val specialty: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val price: String = "",
    val bio: String = "",
    val rpps: String = "",
    val isOnlineForTeleconsult: Boolean = true,
    val isAvailable: Boolean = true,
    val availableSlots: List<String> = emptyList(),
    val consultationTypes: List<String> = listOf("PRESENTIEL", "TELECONSULTATION"),
    // ===== Parcours & Formation =====
    val degree: String = "",
    val educationLevel: String = "",
    val studyDuration: String = "",
    val universities: List<String> = emptyList(),
    val trainings: List<String> = emptyList(),
    val graduationYear: String = "",
    // ===== Documents & Vérification =====
    val hasDocuments: Boolean = false,
    val documentsVerified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "doctors"
    }

    val educationSummary: String
        get() = buildString {
            if (degree.isNotBlank()) append(degree)
            if (educationLevel.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(educationLevel)
            }
        }
}

data class DoctorEducation(
    val degree: String = "",
    val educationLevel: String = "",
    val studyDuration: String = "",
    val universities: List<String> = emptyList(),
    val trainings: List<String> = emptyList(),
    val graduationYear: String = ""
)

data class FirestoreAppointment(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val doctorAvatar: String = "",
    val date: String = "",
    val time: String = "",
    val type: String = "TELECONSULTATION",
    val motif: String = "",
    val status: String = STATUS_PENDING,
    val userNote: String = "",
    val address: String = "",
    val consultationId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "appointments"
        const val STATUS_PENDING = "pending"
        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_CANCELLED = "cancelled"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_NO_SHOW = "no_show"
    }
}

data class FirestoreConversation(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    @ServerTimestamp val lastMessageAt: Timestamp? = null,
    val unreadCount: Map<String, Int> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "conversations"
    }
}

data class FirestoreMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val attachmentUrl: String = "",
    val attachmentName: String = "",
    val status: String = STATUS_SENT,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "messages"
        const val STATUS_SENT = "sent"
        const val STATUS_DELIVERED = "delivered"
        const val STATUS_READ = "read"
    }
}

data class FirestorePrescription(
    @DocumentId val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val medicinesSummary: String = "",
    val notes: String = "",
    val isSigned: Boolean = false,
    val pdfUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "prescriptions"
    }
}

data class FirestoreMedicationReminder(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val medicineName: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val timeOfDay: String = "",
    val isTakenToday: Boolean = false,
    val startDate: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "medication_reminders"
    }
}

data class FirestoreNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val relatedId: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "notifications"
    }
}

data class FirestoreSpecialty(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconName: String = "",
    val displayOrder: Int = 0
) {
    companion object {
        const val COLLECTION = "specialties"
    }
}

data class FirestoreReview(
    @DocumentId val id: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "reviews"
    }
}
