package com.jeda.android.monitoring

import android.content.Context
import com.jeda.android.data.remote.BackendRepository
import com.jeda.android.data.remote.BackendSettings
import com.jeda.android.monitoring.behavior.BehavioralAggregator
import com.jeda.android.monitoring.behavior.BehavioralSnapshot
import com.jeda.android.monitoring.behavior.SystemBehaviorClock
import com.jeda.android.monitoring.intervention.InterventionController
import com.jeda.android.monitoring.intervention.InterventionCoordinator
import com.jeda.android.monitoring.intervention.PrefsTimestampStore
import com.jeda.android.ui.intervention.InterventionOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object MonitoringManager {
    val controller = MonitoringController()
    val aggregator = BehavioralAggregator()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var notificationSyncStarted = false
    private var heartbeatJob: Job? = null
    private var appContext: Context? = null

    lateinit var interventions: InterventionController
        private set
    lateinit var coordinator: InterventionCoordinator
        private set

    fun init(context: Context) {
        if (appContext != null) return
        val app = context.applicationContext
        appContext = app

        BackendSettings.init(app)
        interventions = InterventionController(
            clock = SystemBehaviorClock,
            store = PrefsTimestampStore(app, "soft_interventions")
        )
        coordinator = InterventionCoordinator(aggregator, interventions, SystemBehaviorClock)
    }

    fun startNotificationSync(context: Context) {
        if (notificationSyncStarted) return
        notificationSyncStarted = true
        val notifications = MonitoringNotificationManager(context.applicationContext)
        scope.launch {
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

    fun startMonitoring(context: Context) {
        init(context)
        controller.start()
        aggregator.start()
        coordinator.reset()
        startHeartbeat()
    }

    fun stopMonitoring(context: Context) {
        controller.stop()
        aggregator.stop()
        if (appContext != null) coordinator.reset()
        stopHeartbeat()
        com.jeda.android.monitoring.visual.VisualSamplingManager.stopService(context)
        InterventionOverlay.hide()
        MonitoringNotificationManager(context.applicationContext).cancel()
    }

    /**
     * One second: advances the aggregator, runs the soft-intervention countdown,
     * syncs the overlay, and submits a snapshot when an observation window rolls.
     */
    private fun startHeartbeat() {
        if (heartbeatJob?.isActive == true) return
        heartbeatJob = scope.launch {
     while (isActive) {
         delay(HEARTBEAT_MILLIS)
         coordinator.onSecondElapsed()
         com.jeda.android.monitoring.visual.VisualSamplingManager.onSecondElapsed()
         syncOverlay()
         coordinator.onHeartbeat()?.let { submit(it) }
     }
 }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private suspend fun submit(snapshot: BehavioralSnapshot) {
        BackendRepository.analyzeBehavior(snapshot, BackendSettings.config)
            .onSuccess {
                coordinator.onResponseWhileActive(it)
                syncOverlay()
            }
        // A failed submission is intentionally silent. Per NFR-REL-03 the client
        // stays stable and simply stops scoring until connectivity returns.
    }

    fun triggerOverlaySync() {
        scope.launch(Dispatchers.Main.immediate) {
            syncOverlay()
        }
    }

    private fun syncOverlay() {
        val context = appContext ?: return
        val softState = coordinator.stateFlow.value
        val hardState = com.jeda.android.monitoring.visual.VisualSamplingManager.hardInterventionState.value
        val isIntervention = hardState.active || softState.active
        val isCooldown = coordinator.isCooldownActive

        controller.updateMonitoringState(
            isInterventionActive = isIntervention,
            isCooldown = isCooldown
        )

        android.util.Log.d("JEDA_OVERLAY", "syncOverlay: isIntervention=$isIntervention, hardActive=${hardState.active}, remaining=${hardState.remainingSeconds}")

        if (isIntervention) {
            InterventionOverlay.show(
                context = context,
                softState = coordinator.stateFlow,
                hardState = com.jeda.android.monitoring.visual.VisualSamplingManager.hardInterventionState
            )
        } else {
            InterventionOverlay.hide()
        }
    }

    private const val HEARTBEAT_MILLIS = 1_000L
}
