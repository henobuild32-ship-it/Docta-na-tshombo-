package com.example

import android.app.Application
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class DoctaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.WARN
        val appId = BuildConfig.ONESIGNAL_APP_ID
        if (appId.isNotBlank() && !appId.startsWith("replace_")) {
            OneSignal.initWithContext(this, appId)

            // À la réception d'une notification OneSignal, on loggue proprement
            // l'événement. L'affichage natif est géré par le SDK OneSignal.
            OneSignal.Notifications.addClickListener(object : com.onesignal.notifications.INotificationClickListener {
                override fun onClick(event: com.onesignal.notifications.INotificationClickEvent) {
                    try {
                        val n = event.notification
                        Log.d("OneSignal", "Notification reçue : ${n.title} — ${n.body}")
                    } catch (e: Exception) {
                        Log.w("OneSignal", "Handler click : ${e.message}")
                    }
                }
            })
        }
    }
}