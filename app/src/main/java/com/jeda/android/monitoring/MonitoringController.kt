package com.jeda.android.monitoring

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Keeps the monitoring switch separate from the current foreground-app state. */
class MonitoringController {
    private val _state = MutableStateFlow(MonitoringState.IDLE)
    val stateFlow: StateFlow<MonitoringState> = _state.asStateFlow()

    private val _enabled = MutableStateFlow(false)
    val enabledFlow: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _foregroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackageFlow: StateFlow<String?> = _foregroundPackage.asStateFlow()

    val state: MonitoringState
        get() = _state.value

    val enabled: Boolean
        get() = _enabled.value

    val foregroundPackage: String?
        get() = _foregroundPackage.value

    fun start() {
        _enabled.value = true
        _state.value = MonitoringState.IDLE
    }

    fun stop() {
        _enabled.value = false
        _state.value = MonitoringState.IDLE
        _foregroundPackage.value = null
    }

    fun updateMonitoringState(
        isInterventionActive: Boolean,
        isCooldown: Boolean
    ): MonitoringState {
        val currentPackage = _foregroundPackage.value
        _state.value = when {
            !_enabled.value -> MonitoringState.IDLE
            isInterventionActive -> MonitoringState.INTERVENTION
            isCooldown -> MonitoringState.COOLDOWN
            currentPackage != null && TargetAppPolicy.isTarget(currentPackage) -> MonitoringState.MONITORING
            else -> MonitoringState.IDLE
        }
        return _state.value
    }

    fun onForegroundPackage(packageName: String): MonitoringState {
        _foregroundPackage.value = packageName
        return updateMonitoringState(isInterventionActive = false, isCooldown = false)
    }
}
