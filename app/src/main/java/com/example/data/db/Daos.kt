package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getUserProfile(id: String = "current_user"): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET isSeniorMode = :isSenior WHERE id = 'current_user'")
    suspend fun updateSeniorMode(isSenior: Boolean)
}

@Dao
interface PractitionerDao {
    @Query("SELECT * FROM practitioners")
    fun getAllPractitioners(): Flow<List<PractitionerEntity>>

    @Query("SELECT * FROM practitioners WHERE isFavorite = 1")
    fun getFavoritePractitioners(): Flow<List<PractitionerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPractitioners(practitioners: List<PractitionerEntity>)

    @Query("UPDATE practitioners SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFav: Boolean)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE status = 'CONFIRMED' ORDER BY id DESC")
    fun getUpcomingAppointments(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)
}

@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders ORDER BY id ASC")
    fun getAllReminders(): Flow<List<MedicationReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminderEntity)

    @Query("UPDATE medication_reminders SET isTakenToday = :isTaken WHERE id = :id")
    suspend fun updateTakenState(id: Long, isTaken: Boolean)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY id DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}
