package com.example.data.firebase

import com.example.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository de données 100% Supabase (PostgREST + Realtime).
 * Remplace Firebase Firestore en conservant la même interface publique.
 */
class FirestoreRepository(
    private val postgrest: io.github.jan.supabase.postgrest.Postgrest = SupabaseClientProvider.postgrest
) {

    // ========== HELPERS ==========

    private fun PostgrestFilterBuilder.where(column: String, value: Any) {
        filter(column, FilterOperator.EQ, value)
    }

    private fun camelToSnake(key: String): String = when (key) {
        "patientId" -> "patient_id"
        "doctorId" -> "doctor_id"
        "doctorName" -> "doctor_name"
        "doctorSpecialty" -> "doctor_specialty"
        "doctorAvatar" -> "doctor_avatar"
        "userNote" -> "patient_note"
        "photoPath", "photoUrl" -> "photo_path"
        "isOnlineForTeleconsult" -> "is_online"
        "isAvailable" -> "is_available"
        "isTakenToday" -> "is_taken_today"
        "isRead" -> "is_read"
        "hasDocuments" -> "has_documents"
        "documentsVerified" -> "documents_verified"
        "reviewCount" -> "review_count"
        "availableSlots" -> "available_slots"
        "consultationTypes" -> "consultation_types"
        "medicinesSummary" -> "medicine"
        "userId" -> "user_id"
        "conversationId" -> "conversation_id"
        "senderId" -> "sender_id"
        "senderName" -> "sender_name"
        "attachmentPath", "attachmentUrl" -> "attachment_path"
        "pdfPath" -> "pdf_path"
        "createdAt" -> "created_at"
        "updatedAt" -> "updated_at"
        "lastMessage" -> "last_message"
        "lastMessageSenderId" -> "last_message_sender_id"
        "lastMessageAt" -> "last_message_at"
        "participantIds" -> "participant_ids"
        "participantNames" -> "participant_names"
        "relatedId" -> "related_id"
        "medicineName" -> "medicine_name"
        "timeOfDay" -> "time_of_day"
        "startDate" -> "start_date"
        "displayOrder" -> "display_order"
        "iconName" -> "icon_name"
        else -> key
    }

    private fun <T : Any> watchList(
        tableName: String,
        channelName: String,
        query: suspend () -> List<T>
    ): Flow<List<T>> = callbackFlow {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val realtime = SupabaseClientProvider.realtime
        val realtimeChannel = realtime.channel(channelName) { }
        val refresh: suspend () -> Unit = {
            try {
                trySend(query())
            } catch (_: Exception) {
                trySend(emptyList())
            }
        }
        scope.launch { refresh() }
        listOf(
            realtimeChannel.postgresChangeFlow<PostgresAction.Insert>("public") {
                table = tableName
            },
            realtimeChannel.postgresChangeFlow<PostgresAction.Update>("public") {
                table = tableName
            },
            realtimeChannel.postgresChangeFlow<PostgresAction.Delete>("public") {
                table = tableName
            }
        ).forEach { changeFlow ->
            scope.launch { changeFlow.collect { refresh() } }
        }
        scope.launch {
            try { realtimeChannel.subscribe() } catch (_: Exception) {}
        }
        awaitClose {
            scope.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                try { realtimeChannel.unsubscribe() } catch (_: Exception) {}
                try { SupabaseClientProvider.realtime.removeChannel(realtimeChannel) } catch (_: Exception) {}
            }
        }
    }

    // ========== DOCTORS ==========

    fun getDoctors(): Flow<List<FirestoreDoctor>> = watchList(
        tableName = "doctors",
        channelName = "doctors_list"
    ) {
        postgrest.from("doctors")
            .select { order("rating", Order.DESCENDING) }
            .decodeList<FirestoreDoctor>()
    }

    fun getDoctorsBySpecialty(specialty: String): Flow<List<FirestoreDoctor>> = watchList(
        tableName = "doctors",
        channelName = "doctors_by_specialty"
    ) {
        postgrest.from("doctors")
            .select {
                filter { where("specialty", specialty) }
                order("rating", Order.DESCENDING)
            }
            .decodeList<FirestoreDoctor>()
    }

    suspend fun getDoctorById(doctorId: String): FirestoreDoctor? {
        return try {
            postgrest.from("doctors")
                .select { filter { where("id", doctorId) } }
                .decodeSingleOrNull<FirestoreDoctor>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createDoctor(doctor: FirestoreDoctor): Result<String> {
        return try {
            require(doctor.userId.isNotBlank()) { "Identifiant praticien manquant" }
            postgrest.from("doctors").insert(
                buildJsonObject {
                    put("id", doctor.userId)
                    put("name", doctor.name)
                    put("specialty", doctor.specialty)
                    put("address", doctor.address)
                    put("bio", doctor.bio)
                    put("professional_number", doctor.rpps)
                    put("rating", doctor.rating)
                    put("review_count", doctor.reviewCount)
                    put("is_online", doctor.isOnlineForTeleconsult)
                    put("is_available", doctor.isAvailable)
                    put("documents_verified", doctor.documentsVerified)
                    put("available_slots", kotlinx.serialization.json.JsonArray(doctor.availableSlots.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                    put("price", doctor.price)
                    put("degree", doctor.degree)
                    put("education_level", doctor.educationLevel)
                    put("study_duration", doctor.studyDuration)
                    put("universities", kotlinx.serialization.json.JsonArray(doctor.universities.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                    put("trainings", kotlinx.serialization.json.JsonArray(doctor.trainings.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                    put("graduation_year", doctor.graduationYear)
                    put("has_documents", doctor.hasDocuments)
                    put("photo_path", doctor.photoUrl)
                }
            )
            Result.success(doctor.userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDoctor(doctorId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val mapped = updates.entries.associate { (key, value) -> camelToSnake(key) to value }
            postgrest.from("doctors")
                .update({ mapped.forEach { (col, v) -> setFromAny(col, v) } }) {
                    filter { where("id", doctorId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== APPOINTMENTS ==========

    fun getPatientAppointments(patientId: String): Flow<List<FirestoreAppointment>> = watchList(
        tableName = "appointments",
        channelName = "appointments_patient_$patientId"
    ) {
        postgrest.from("appointments")
            .select {
                filter { where("patient_id", patientId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FirestoreAppointment>()
    }

    fun getDoctorAppointments(doctorId: String): Flow<List<FirestoreAppointment>> = watchList(
        tableName = "appointments",
        channelName = "appointments_doctor_$doctorId"
    ) {
        postgrest.from("appointments")
            .select {
                filter { where("doctor_id", doctorId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FirestoreAppointment>()
    }

    fun getUpcomingPatientAppointments(patientId: String): Flow<List<FirestoreAppointment>> = watchList(
        tableName = "appointments",
        channelName = "appointments_upcoming_$patientId"
    ) {
        postgrest.from("appointments")
            .select {
                filter {
                    where("patient_id", patientId)
                    isIn("status", listOf(FirestoreAppointment.STATUS_PENDING, FirestoreAppointment.STATUS_CONFIRMED))
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FirestoreAppointment>()
    }

    suspend fun createAppointment(appointment: FirestoreAppointment): Result<String> {
        return try {
            val scheduledAt = buildScheduledAt(appointment)
            val created = postgrest.rpc(
                "create_appointment",
                buildJsonObject {
                    put("p_patient_id", appointment.patientId)
                    put("p_doctor_id", appointment.doctorId)
                    put("p_scheduled_at", scheduledAt)
                    put("p_type", appointment.type)
                    put("p_motif", appointment.motif)
                    put("p_patient_note", appointment.userNote)
                    put("p_address", appointment.address)
                }
            ).decodeSingle<AppointmentIdRow>()
            postgrest.from("appointments")
                .update({
                    set("doctor_name", appointment.doctorName)
                    set("patient_name", appointment.patientName)
                    set("doctor_specialty", appointment.doctorSpecialty)
                    set("doctor_avatar", appointment.doctorAvatar)
                    set("date", appointment.date)
                    set("time", appointment.time)
                }) { filter { where("id", created.id) } }
            Result.success(created.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            postgrest.from("appointments")
                .update({ set("status", status) }) { filter { where("id", appointmentId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointment(appointmentId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            require(appointmentId.isNotBlank()) { "Identifiant de rendez-vous manquant" }
            val mapped = updates.entries.associate { (key, value) -> camelToSnake(key) to value }
            postgrest.from("appointments")
                .update({ mapped.forEach { (col, v) -> setFromAny(col, v) } }) {
                    filter { where("id", appointmentId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== CONVERSATIONS & MESSAGES ==========

    fun getConversations(userId: String): Flow<List<FirestoreConversation>> = watchList(
        tableName = "conversations",
        channelName = "conversations_$userId"
    ) {
        postgrest.from("conversations")
            .select {
                filter { contains("participant_ids", listOf(userId)) }
                order("last_message_at", Order.DESCENDING)
            }
            .decodeList<FirestoreConversation>()
    }

    fun getMessages(conversationId: String): Flow<List<FirestoreMessage>> = watchList(
        tableName = "messages",
        channelName = "messages_$conversationId"
    ) {
        postgrest.from("messages")
            .select {
                filter { where("conversation_id", conversationId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<FirestoreMessage>()
    }

    suspend fun sendMessage(message: FirestoreMessage): Result<String> {
        return try {
            val inserted = postgrest.from("messages")
                .insert(
                    buildJsonObject {
                        put("conversation_id", message.conversationId)
                        put("sender_id", message.senderId)
                        put("sender_name", message.senderName)
                        put("body", message.text)
                        put("attachment_path", message.attachmentUrl)
                        put("attachment_name", message.attachmentName)
                        put("status", message.status)
                    }
                ) { select() }
                .decodeSingle<MessageIdRow>()
            Result.success(inserted.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureDirectConversation(
        firstUserId: String,
        firstUserName: String,
        secondUserId: String,
        secondUserName: String
    ): Result<String> {
        return try {
            require(firstUserId.isNotBlank() && secondUserId.isNotBlank()) {
                "Les deux participants sont requis"
            }
            val existing = postgrest.from("conversations")
                .select { filter { contains("participant_ids", listOf(firstUserId, secondUserId)) } }
                .decodeList<FirestoreConversation>()
            if (existing.isNotEmpty()) return Result.success(existing.first().id)
            val inserted = postgrest.from("conversations")
                .insert(
                    buildJsonObject {
                        put("participant_ids", kotlinx.serialization.json.JsonArray(listOf(firstUserId, secondUserId).map { kotlinx.serialization.json.JsonPrimitive(it) }))
                        put("participant_names", kotlinx.serialization.json.JsonArray(listOf(firstUserName, secondUserName).map { kotlinx.serialization.json.JsonPrimitive(it) }))
                    }
                ) { select() }
                .decodeSingle<ConversationIdRow>()
            Result.success(inserted.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createConversation(
        participantIds: List<String>,
        participantNames: List<String>
    ): Result<String> {
        return try {
            val inserted = postgrest.from("conversations")
                .insert(
                    buildJsonObject {
                        put("participant_ids", kotlinx.serialization.json.JsonArray(participantIds.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                        put("participant_names", kotlinx.serialization.json.JsonArray(participantNames.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                    }
                ) { select() }
                .decodeSingle<ConversationIdRow>()
            Result.success(inserted.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== PRESCRIPTIONS ==========

    fun getPatientPrescriptions(patientId: String): Flow<List<FirestorePrescription>> = watchList(
        tableName = "prescriptions",
        channelName = "prescriptions_$patientId"
    ) {
        postgrest.from("prescriptions")
            .select {
                filter { where("patient_id", patientId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FirestorePrescription>()
    }

    suspend fun createPrescription(prescription: FirestorePrescription): Result<String> {
        return try {
            val inserted = postgrest.from("prescriptions")
                .insert(
                    buildJsonObject {
                        put("doctor_id", prescription.doctorId)
                        put("doctor_name", prescription.doctorName)
                        put("patient_id", prescription.patientId)
                        put("patient_name", prescription.patientName)
                        put("medicine", prescription.medicinesSummary)
                        put("dosage", prescription.dosage)
                        put("duration", prescription.duration)
                        put("notes", prescription.notes)
                        put("reference", prescription.reference)
                        put("diagnosis", prescription.diagnosis)
                        put("signature_hash", prescription.signatureHash)
                        put("pdf_path", prescription.pdfPath)
                    }
                ) { select() }
                .decodeSingle<PrescriptionIdRow>()
            Result.success(inserted.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== MEDICATION REMINDERS ==========

    fun getPatientReminders(patientId: String): Flow<List<FirestoreMedicationReminder>> = watchList(
        tableName = "medication_reminders",
        channelName = "reminders_$patientId"
    ) {
        postgrest.from("medication_reminders")
            .select {
                filter { where("patient_id", patientId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FirestoreMedicationReminder>()
    }

    suspend fun createReminder(reminder: FirestoreMedicationReminder): Result<String> {
        return try {
            val time = reminder.timeOfDay
            val timeOfDay = when {
                time.count { it == ':' } == 1 -> "$time:00"
                time.count { it == ':' } >= 2 -> time
                else -> "$time:00:00"
            }
            val startDate = reminder.startDate.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
                ?: todayIsoDate()
            val inserted = postgrest.from("medication_reminders")
                .insert(
                    buildJsonObject {
                        put("patient_id", reminder.patientId)
                        put("medicine_name", reminder.medicineName)
                        put("dosage", reminder.dosage)
                        put("frequency", reminder.frequency)
                        put("time_of_day", timeOfDay)
                        put("is_taken_today", reminder.isTakenToday)
                        put("start_date", startDate)
                        put("notes", reminder.notes)
                    }
                ) { select() }
                .decodeSingle<ReminderIdRow>()
            Result.success(inserted.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminderTaken(reminderId: String, isTaken: Boolean): Result<Unit> {
        return try {
            postgrest.from("medication_reminders")
                .update({ set("is_taken_today", isTaken) }) { filter { where("id", reminderId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== NOTIFICATIONS ==========

    fun getUserNotifications(userId: String): Flow<List<FirestoreNotification>> = watchList(
        tableName = "notifications",
        channelName = "notifications_$userId"
    ) {
        postgrest.from("notifications")
            .select {
                filter { where("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList<FirestoreNotification>()
    }

    suspend fun markNotificationRead(notificationId: String) {
        try {
            postgrest.from("notifications")
                .update({ set("is_read", true) }) { filter { where("id", notificationId) } }
        } catch (_: Exception) {}
    }

    // ========== REVIEWS ==========

    fun getDoctorReviews(doctorId: String): Flow<List<FirestoreReview>> = watchList(
        tableName = "reviews",
        channelName = "reviews_$doctorId"
    ) {
        postgrest.from("reviews")
            .select {
                filter { where("doctor_id", doctorId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FirestoreReview>()
    }

    suspend fun createReview(review: FirestoreReview): Result<String> {
        return try {
            val inserted = postgrest.from("reviews")
                .insert(
                    buildJsonObject {
                        put("doctor_id", review.doctorId)
                        put("patient_id", review.patientId)
                        put("rating", review.rating)
                        put("comment", review.comment)
                    }
                ) { select() }
                .decodeSingle<ReviewIdRow>()
            Result.success(inserted.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== SPECIALTIES ==========

    fun getSpecialties(): Flow<List<FirestoreSpecialty>> = watchList(
        tableName = "specialties",
        channelName = "specialties_list"
    ) {
        postgrest.from("specialties")
            .select { order("display_order", Order.ASCENDING) }
            .decodeList<FirestoreSpecialty>()
    }

    // ========== INTERNAL ==========

    private fun buildScheduledAt(appointment: FirestoreAppointment): String {
        val day = appointment.date.substringBefore(",").trim()
        val t = appointment.time.trim()
        val clock = when {
            t.count { it == ':' } == 1 -> "$t:00"
            t.count { it == ':' } >= 2 -> t
            else -> "09:00:00"
        }
        return if (day.isNotBlank()) "$day $clock" else todayIsoDate()
    }

    private fun todayIsoDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun io.github.jan.supabase.postgrest.query.PostgrestUpdate.setFromAny(column: String, value: Any) {
        when (value) {
            is String -> set(column, value)
            is Boolean -> set(column, value)
            is Int -> set(column, value)
            is Long -> set(column, value)
            is Float -> set(column, value)
            is Double -> set(column, value)
            else -> set(column, value.toString())
        }
    }

}

@Serializable
private data class AppointmentIdRow(@SerialName("id") val id: String = "")

@Serializable
private data class MessageIdRow(@SerialName("id") val id: String = "")

@Serializable
private data class ConversationIdRow(@SerialName("id") val id: String = "")

@Serializable
private data class PrescriptionIdRow(@SerialName("id") val id: String = "")

@Serializable
private data class ReminderIdRow(@SerialName("id") val id: String = "")

@Serializable
private data class ReviewIdRow(@SerialName("id") val id: String = "")
