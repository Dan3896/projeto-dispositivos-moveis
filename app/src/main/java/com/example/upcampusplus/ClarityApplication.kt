package com.example.upcampusplus

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode

class ClarityApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // RNF-08: em debug, acesso a disco na thread principal aparece no log.
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build())
        }
        container = AppContainer.create(this)
    }
}
