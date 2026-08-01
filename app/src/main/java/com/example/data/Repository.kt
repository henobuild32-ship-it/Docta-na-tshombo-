package com.example.data

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao().getUserProfile()
    val allPractitioners: Flow<List<PractitionerEntity>> = db.practitionerDao().getAllPractitioners()
    val favoritePractitioners: Flow<List<PractitionerEntity>> = db.practitionerDao().getFavoritePractitioners()
    val upcomingAppointments: Flow<List<AppointmentEntity>> = db.appointmentDao().getUpcomingAppointments()
    val allAppointments: Flow<List<AppointmentEntity>> = db.appointmentDao().getAllAppointments()
    val medicationReminders: Flow<List<MedicationReminderEntity>> = db.medicationReminderDao().getAllReminders()
    val prescriptions: Flow<List<PrescriptionEntity>> = db.prescriptionDao().getAllPrescriptions()
    val chatMessages: Flow<List<ChatMessageEntity>> = db.chatMessageDao().getAllMessages()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        db.userProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun toggleSeniorMode(isSenior: Boolean) {
        db.userProfileDao().updateSeniorMode(isSenior)
    }

    suspend fun toggleFavoritePractitioner(id: String, isFav: Boolean) {
        db.practitionerDao().toggleFavorite(id, isFav)
    }

    suspend fun addAppointment(appointment: AppointmentEntity) {
        db.appointmentDao().insertAppointment(appointment)
    }

    suspend fun cancelAppointment(id: Long) {
        db.appointmentDao().updateAppointmentStatus(id, "CANCELLED")
    }

    suspend fun addMedicationReminder(reminder: MedicationReminderEntity) {
        db.medicationReminderDao().insertReminder(reminder)
    }

    suspend fun toggleMedicationTaken(id: Long, isTaken: Boolean) {
        db.medicationReminderDao().updateTakenState(id, isTaken)
    }

    suspend fun addPrescription(prescription: PrescriptionEntity) {
        db.prescriptionDao().insertPrescription(prescription)
    }

    suspend fun sendChatMessage(message: ChatMessageEntity) {
        db.chatMessageDao().insertMessage(message)
    }

    suspend fun registerPatient(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        photoUri: String?
    ) {
        db.userProfileDao().insertOrUpdateProfile(
            UserProfileEntity(
                id = "current_user",
                role = "PATIENT",
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                profileImageUri = photoUri,
                isVerified = true
            )
        )
    }

    suspend fun registerPractitioner(
        doctorName: String,
        specialty: String,
        rppsNumber: String,
        photoUri: String?
    ) {
        db.userProfileDao().insertOrUpdateProfile(
            UserProfileEntity(
                id = "current_user",
                role = "PRATICIEN",
                firstName = doctorName,
                lastName = "",
                email = "",
                phone = "",
                specialty = specialty,
                rppsNumber = rppsNumber,
                profileImageUri = photoUri,
                isVerified = true
            )
        )
    }
}
