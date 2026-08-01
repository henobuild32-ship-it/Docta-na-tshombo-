package com.example.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ========== DOCTORS ==========

    fun getDoctors(): Flow<List<FirestoreDoctor>> = callbackFlow {
        val listener = firestore.collection(FirestoreDoctor.COLLECTION)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val doctors = snapshot?.toObjects(FirestoreDoctor::class.java) ?: emptyList()
                trySend(doctors)
            }
        awaitClose { listener.remove() }
    }

    fun getDoctorsBySpecialty(specialty: String): Flow<List<FirestoreDoctor>> = callbackFlow {
        val listener = firestore.collection(FirestoreDoctor.COLLECTION)
            .whereEqualTo("specialty", specialty)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val doctors = snapshot?.toObjects(FirestoreDoctor::class.java) ?: emptyList()
                trySend(doctors)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getDoctorById(doctorId: String): FirestoreDoctor? {
        return try {
            firestore.collection(FirestoreDoctor.COLLECTION)
                .document(doctorId)
                .get()
                .await()
                .toObject(FirestoreDoctor::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createDoctor(doctor: FirestoreDoctor): Result<String> {
        return try {
            val docRef = firestore.collection(FirestoreDoctor.COLLECTION).add(doctor).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDoctor(doctorId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = Timestamp.now()
            firestore.collection(FirestoreDoctor.COLLECTION)
                .document(doctorId)
                .update(updateData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== APPOINTMENTS ==========

    fun getPatientAppointments(patientId: String): Flow<List<FirestoreAppointment>> = callbackFlow {
        val listener = firestore.collection(FirestoreAppointment.COLLECTION)
            .whereEqualTo("patientId", patientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.toObjects(FirestoreAppointment::class.java) ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    fun getDoctorAppointments(doctorId: String): Flow<List<FirestoreAppointment>> = callbackFlow {
        val listener = firestore.collection(FirestoreAppointment.COLLECTION)
            .whereEqualTo("doctorId", doctorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.toObjects(FirestoreAppointment::class.java) ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    fun getUpcomingPatientAppointments(patientId: String): Flow<List<FirestoreAppointment>> = callbackFlow {
        val listener = firestore.collection(FirestoreAppointment.COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereIn("status", listOf(FirestoreAppointment.STATUS_PENDING, FirestoreAppointment.STATUS_CONFIRMED))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.toObjects(FirestoreAppointment::class.java) ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createAppointment(appointment: FirestoreAppointment): Result<String> {
        return try {
            val docRef = firestore.collection(FirestoreAppointment.COLLECTION)
                .add(appointment)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            firestore.collection(FirestoreAppointment.COLLECTION)
                .document(appointmentId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== CONVERSATIONS & MESSAGES ==========

    fun getConversations(userId: String): Flow<List<FirestoreConversation>> = callbackFlow {
        val listener = firestore.collection(FirestoreConversation.COLLECTION)
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val conversations = snapshot?.toObjects(FirestoreConversation::class.java) ?: emptyList()
                trySend(conversations)
            }
        awaitClose { listener.remove() }
    }

    fun getMessages(conversationId: String): Flow<List<FirestoreMessage>> = callbackFlow {
        val listener = firestore.collection(FirestoreMessage.COLLECTION)
            .whereEqualTo("conversationId", conversationId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(FirestoreMessage::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(message: FirestoreMessage): Result<String> {
        return try {
            val docRef = firestore.collection(FirestoreMessage.COLLECTION)
                .add(message)
                .await()

            // Update conversation last message
            firestore.collection(FirestoreConversation.COLLECTION)
                .document(message.conversationId)
                .update(
                    mapOf(
                        "lastMessage" to message.text,
                        "lastMessageSenderId" to message.senderId,
                        "lastMessageAt" to Timestamp.now()
                    )
                )
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createConversation(
        participantIds: List<String>,
        participantNames: List<String>
    ): Result<String> {
        return try {
            val conversation = FirestoreConversation(
                participantIds = participantIds,
                participantNames = participantNames
            )
            val docRef = firestore.collection(FirestoreConversation.COLLECTION)
                .add(conversation)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== PRESCRIPTIONS ==========

    fun getPatientPrescriptions(patientId: String): Flow<List<FirestorePrescription>> = callbackFlow {
        val listener = firestore.collection(FirestorePrescription.COLLECTION)
            .whereEqualTo("patientId", patientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val prescriptions = snapshot?.toObjects(FirestorePrescription::class.java) ?: emptyList()
                trySend(prescriptions)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPrescription(prescription: FirestorePrescription): Result<String> {
        return try {
            val docRef = firestore.collection(FirestorePrescription.COLLECTION)
                .add(prescription)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== MEDICATION REMINDERS ==========

    fun getPatientReminders(patientId: String): Flow<List<FirestoreMedicationReminder>> = callbackFlow {
        val listener = firestore.collection(FirestoreMedicationReminder.COLLECTION)
            .whereEqualTo("patientId", patientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val reminders = snapshot?.toObjects(FirestoreMedicationReminder::class.java) ?: emptyList()
                trySend(reminders)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createReminder(reminder: FirestoreMedicationReminder): Result<String> {
        return try {
            val docRef = firestore.collection(FirestoreMedicationReminder.COLLECTION)
                .add(reminder)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminderTaken(reminderId: String, isTaken: Boolean): Result<Unit> {
        return try {
            firestore.collection(FirestoreMedicationReminder.COLLECTION)
                .document(reminderId)
                .update(
                    mapOf(
                        "isTakenToday" to isTaken,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== NOTIFICATIONS ==========

    fun getUserNotifications(userId: String): Flow<List<FirestoreNotification>> = callbackFlow {
        val listener = firestore.collection(FirestoreNotification.COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val notifications = snapshot?.toObjects(FirestoreNotification::class.java) ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markNotificationRead(notificationId: String) {
        try {
            firestore.collection(FirestoreNotification.COLLECTION)
                .document(notificationId)
                .update("isRead", true)
                .await()
        } catch (_: Exception) {}
    }

    // ========== REVIEWS ==========

    fun getDoctorReviews(doctorId: String): Flow<List<FirestoreReview>> = callbackFlow {
        val listener = firestore.collection(FirestoreReview.COLLECTION)
            .whereEqualTo("doctorId", doctorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val reviews = snapshot?.toObjects(FirestoreReview::class.java) ?: emptyList()
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createReview(review: FirestoreReview): Result<String> {
        return try {
            val docRef = firestore.collection(FirestoreReview.COLLECTION)
                .add(review)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== SPECIALTIES ==========

    fun getSpecialties(): Flow<List<FirestoreSpecialty>> = callbackFlow {
        val listener = firestore.collection(FirestoreSpecialty.COLLECTION)
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val specialties = snapshot?.toObjects(FirestoreSpecialty::class.java) ?: emptyList()
                trySend(specialties)
            }
        awaitClose { listener.remove() }
    }
}
