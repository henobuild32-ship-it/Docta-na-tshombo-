package com.example.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.firebase.FirestoreMedicationReminder
import java.time.LocalDateTime
import java.time.ZoneId

object MedicationReminderScheduler {
    fun schedule(context: Context, reminder: FirestoreMedicationReminder) {
        val parts = reminder.timeOfDay.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        var next = LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(LocalDateTime.now())) next = next.plusDays(1)
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("name", reminder.medicineName)
            putExtra("dosage", reminder.dosage)
        }
        val requestCode = (reminder.patientId + reminder.medicineName + reminder.timeOfDay).hashCode()
        val pending = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }
}
