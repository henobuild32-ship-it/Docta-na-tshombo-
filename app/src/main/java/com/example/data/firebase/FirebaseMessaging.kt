package com.example.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DoctaMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val auth = AuthRepository()
        auth.currentUserId?.let { uid ->
            CoroutineScope(Dispatchers.IO).launch {
                auth.updateFcmToken(uid, token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "Docta na Tshombo 🩺",
                body = notification.body ?: "Nouveau rappel ou message médical.",
                data = message.data
            )
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val channelId = "docta_notifications_sound"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Docta na Tshombo Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications avec son de rendez-vous, messages et rappels"
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 100, 200)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let { intent ->
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> intent.putExtra(key, value) }

            val pendingIntent = PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(longArrayOf(100, 200, 100, 200))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}

class FcmHelper {

    companion object {
        suspend fun getToken(): String? {
            return try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                null
            }
        }

        suspend fun subscribeToTopic(topic: String) {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            } catch (_: Exception) {}
        }

        suspend fun unsubscribeFromTopic(topic: String) {
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            } catch (_: Exception) {}
        }
    }
}
