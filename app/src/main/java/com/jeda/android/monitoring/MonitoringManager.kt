package com.jeda.android.monitoring

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

object MonitoringManager {
    val controller = MonitoringController()

    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var notificationSyncStarted = false

    fun startNotificationSync(context: Context) {
        if (notificationSyncStarted) return
        notificationSyncStarted = true
        val notifications = MonitoringNotificationManager(context.applicationContext)
        notificationScope.launch {
            combine(
                controller.enabledFlow,
                controller.stateFlow,
                controller.foregroundPackageFlow
            ) { enabled, state, packageName -> Triple(enabled, state, packageName) }
                .collect { (enabled, state, packageName) ->
                    notifications.show(enabled, state, packageName)
                }
        }
    }

    fun stopMonitoring(context: Context) {
        controller.stop()
        MonitoringNotificationManager(context.applicationContext).cancel()
    }
}
