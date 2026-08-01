package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val role: String = "PATIENT",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarResName: String = "img_profile_default",
    val profileImageUri: String? = null,
    val specialty: String = "",
    val rppsNumber: String = "",
    val isVerified: Boolean = false,
    val isSeniorMode: Boolean = false
)

@Entity(tableName = "practitioners")
data class PractitionerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val specialty: String,
    val address: String,
    val avatarDrawableName: String,
    val profileImageUri: String? = null,
    val rating: Float,
    val reviewCount: Int,
    val price: String,
    val bio: String,
    val rpps: String,
    val isFavorite: Boolean = false,
    val isOnlineForTeleconsult: Boolean = true
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val practitionerId: String,
    val practitionerName: String,
    val practitionerSpecialty: String,
    val practitionerAvatar: String,
    val date: String,
    val time: String,
    val type: String, // PRESENTIEL, TELECONSULTATION
    val motif: String,
    val status: String = "CONFIRMED", // CONFIRMED, CANCELLED, COMPLETED
    val userNote: String = "",
    val address: String = ""
)

@Entity(tableName = "medication_reminders")
data class MedicationReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineName: String,
    val dosage: String,
    val frequency: String, // e.g. "2 fois par jour"
    val timeOfDay: String, // e.g. "08:00, 20:00"
    val isTakenToday: Boolean = false,
    val startDate: String,
    val notes: String = ""
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val practitionerName: String,
    val date: String,
    val medicinesSummary: String,
    val isSigned: Boolean = true,
    val pdfSummary: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String = "main_chat",
    val senderName: String,
    val isFromPractitioner: Boolean,
    val message: String,
    val timestamp: String,
    val attachmentName: String? = null,
    val isVerified: Boolean = false
)
