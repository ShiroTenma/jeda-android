package com.jeda.android

import android.app.Application
import com.jeda.android.monitoring.MonitoringManager

class JedaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Guarantees backend settings, the rolling intervention log, and the
        // coordinator exist before any component can reach for them.
        MonitoringManager.init(this)
    }
}
