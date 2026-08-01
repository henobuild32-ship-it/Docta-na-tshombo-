package com.example.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUserId: String? get() = auth.currentUser?.uid
    val isAuthenticated: Boolean get() = auth.currentUser != null

    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String> {
        return try {
            val user = auth.currentUser ?: throw Exception("Aucun utilisateur connecté")
            val result = user.getIdToken(forceRefresh).await()
            val token = result.token ?: throw Exception("Token Firebase null")
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        role: String = FirestoreUser.ROLE_PATIENT
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")

            val firestoreUser = FirestoreUser(
                uid = user.uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                role = role,
                createdAt = null,
                updatedAt = null
            )

            firestore.collection(FirestoreUser.COLLECTION)
                .document(user.uid)
                .set(firestoreUser)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign in failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): FirestoreUser? {
        return try {
            firestore.collection(FirestoreUser.COLLECTION)
                .document(uid)
                .get()
                .await()
                .toObject(FirestoreUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = com.google.firebase.Timestamp.now()
            firestore.collection(FirestoreUser.COLLECTION)
                .document(uid)
                .update(updateData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            firestore.collection(FirestoreUser.COLLECTION)
                .document(uid)
                .update("fcmToken", token)
                .await()
        } catch (_: Exception) {}
    }

    suspend fun deleteUser(): Result<Unit> {
        return try {
            currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
