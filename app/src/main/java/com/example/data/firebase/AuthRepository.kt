package com.example.data.firebase

import com.example.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository d'authentification 100% Supabase (Auth + table `profiles`).
 * Remplace Firebase Auth / Firestore.
 */
class AuthRepository(
    private val auth: io.github.jan.supabase.auth.Auth = SupabaseClientProvider.auth,
    private val postgrest: io.github.jan.supabase.postgrest.Postgrest = SupabaseClientProvider.postgrest
) {
    val currentUserId: String? get() = auth.currentUserOrNull()?.id
    val isAuthenticated: Boolean get() = auth.currentUserOrNull() != null

    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String> {
        return try {
            val token = auth.currentAccessTokenOrNull()
                ?: throw Exception("Aucun utilisateur connecté")
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Émet l'id de l'utilisateur connecté (ou null quand déconnecté). */
    fun authStateFlow(): Flow<String?> = auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.id
                SessionStatus.Initializing,
                is SessionStatus.NotAuthenticated,
                is SessionStatus.RefreshFailure -> null
            }
        }
        .distinctUntilChanged()

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        role: String = FirestoreUser.ROLE_PATIENT
    ): Result<UserInfo> {
        return try {
            val emailValue = email
            val passwordValue = password
            val user = auth.signUpWith(Email) {
                this.email = emailValue
                this.password = passwordValue
                data = buildJsonObject {
                    put("first_name", firstName)
                    put("last_name", lastName)
                    put("phone", phone)
                    put("role", role)
                }
            } ?: throw Exception("Échec de la création du compte")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            val emailValue = email
            val passwordValue = password
            auth.signInWith(Email) {
                this.email = emailValue
                this.password = passwordValue
            }
            val user = auth.currentUserOrNull() ?: throw Exception("Sign in failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {
            auth.signOut(io.github.jan.supabase.auth.SignOutScope.LOCAL)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): FirestoreUser? {
        return try {
            postgrest.from("profiles")
                .select { filter { eq("id", uid) } }
                .decodeSingle<FirestoreUser>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val mapped = updates.entries.associate { (key, value) -> toProfileColumn(key) to value }
            if (mapped.isEmpty()) return Result.success(Unit)
            postgrest.from("profiles")
                .update({ mapped.forEach { (col, v) -> setFromAny(col, v) } }) {
                    filter { eq("id", uid) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            postgrest.from("profiles")
                .update({ set("onesignal_subscription_id", token) }) {
                    filter { eq("id", uid) }
                }
        } catch (_: Exception) {}
    }

    suspend fun deleteUser(): Result<Unit> {
        return Result.failure(Exception("La suppression du compte n'est pas disponible côté client"))
    }

    private fun toProfileColumn(key: String): String = when (key) {
        "firstName" -> "first_name"
        "lastName" -> "last_name"
        "birthDate" -> "birth_date"
        "bloodType" -> "blood_type"
        "medicalHistory" -> "medical_history"
        "photoUrl", "photoPath" -> "photo_path"
        "isVerified" -> "is_verified"
        "isActive" -> "is_active"
        "isSeniorMode" -> "is_senior_mode"
        "onesignalSubscriptionId" -> "onesignal_subscription_id"
        "rppsNumber" -> "rpps_number"
        else -> key
    }

    private fun io.github.jan.supabase.postgrest.query.PostgrestUpdate.setFromAny(column: String, value: Any) {
        when (value) {
            is String -> set(column, value)
            is Boolean -> set(column, value)
            is Int -> set(column, value)
            is Long -> set(column, value)
            is Float -> set(column, value)
            is Double -> set(column, value)
            else -> set(column, value as String)
        }
    }
}
