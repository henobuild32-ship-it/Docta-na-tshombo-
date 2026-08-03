package com.example.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminders"
        manager.createNotificationChannel(NotificationChannel(channelId, "Rappels médicaments", NotificationManager.IMPORTANCE_HIGH))
        val name = intent.getStringExtra("name") ?: "Médicament"
        val dosage = intent.getStringExtra("dosage").orEmpty()
        manager.notify((name + dosage).hashCode(), NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Rappel de traitement")
            .setContentText("$name — $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build())
    }
}
