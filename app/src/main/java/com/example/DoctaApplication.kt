package com.example

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class DoctaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.WARN
        val appId = BuildConfig.ONESIGNAL_APP_ID
        if (appId.isNotBlank() && !appId.startsWith("replace_")) {
            OneSignal.initWithContext(this, appId)
        }
    }
}
