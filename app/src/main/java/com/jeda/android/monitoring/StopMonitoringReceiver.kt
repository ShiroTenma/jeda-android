package com.jeda.android.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopMonitoringReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        MonitoringManager.stopMonitoring(context)
    }
}
