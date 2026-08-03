package com.example.data.firebase

import com.onesignal.OneSignal

/**
 * Helper de notifications 100% OneSignal.
 * Remplace Firebase Cloud Messaging (FirebaseMessagingService supprimé).
 */
class FcmHelper {

    companion object {
        /**
         * Identifiant OneSignal de l'utilisateur, stocké dans
         * `profiles.onesignal_subscription_id` pour cibler les notifications.
         */
        suspend fun getToken(): String? {
            return try {
                val userId = OneSignal.User.onesignalId
                if (!userId.isNullOrBlank()) userId
                else OneSignal.User.pushSubscription.token
            } catch (e: Exception) {
                null
            }
        }

        suspend fun subscribeToTopic(topic: String) {
            try {
                OneSignal.User.addTag(topic, "true")
            } catch (_: Exception) {}
        }

        suspend fun unsubscribeFromTopic(topic: String) {
            try {
                OneSignal.User.removeTag(topic)
            } catch (_: Exception) {}
        }
    }
}
