package com.jeda.android.monitoring.intervention

import com.jeda.android.data.remote.BehaviorAnalysisResponse
import com.jeda.android.monitoring.behavior.BehaviorClock
import com.jeda.android.monitoring.behavior.BehavioralAggregator
import com.jeda.android.monitoring.behavior.BehavioralSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Drives the M3 loop: when to ask the backend, when to show a soft intervention,
 * and when to stay quiet.
 *
 * The behavioural path may only ever produce a soft intervention. Nothing in this
 * class can open a hard block; that decision belongs to the RRS path in M6.
 *
 * Android-free and clock-injected, so the escalation and cooldown rules are
 * testable without a device.
 */
class InterventionCoordinator(
    private val aggregator: BehavioralAggregator,
    private val controller: InterventionController,
    private val clock: BehaviorClock,
    private val cooldownSeconds: Int = DEFAULT_COOLDOWN_SECONDS
) {
    private val _state = MutableStateFlow(SoftInterventionState())
    val stateFlow: StateFlow<SoftInterventionState> = _state.asStateFlow()

    private var cooldownUntilMillis = 0L

    /**
     * Called on every heartbeat. Returns the observation window that just closed,
     * if the cooldown has expired, otherwise null.
     */
    fun onHeartbeat(): BehavioralSnapshot? {
        val completed = aggregator.consumeCompletedWindow() ?: return null
        if (clock.nowMillis() < cooldownUntilMillis) return null
        return completed
    }

    fun onResponse(response: BehaviorAnalysisResponse) {
        if (!response.requestsSoftIntervention) return
        if (response.cooldownSeconds <= 0) return

        controller.recordSoftIntervention()
        cooldownUntilMillis = clock.nowMillis() + cooldownSeconds * 1000L
        _state.value = SoftInterventionState(
            active = true,
            fds = response.fds,
            level = response.level,
            remainingSeconds = response.cooldownSeconds,
            totalSeconds = response.cooldownSeconds
        )
    }

    fun onSecondElapsed() {
        val current = _state.value
        if (!current.active) return

        val remaining = current.remainingSeconds - 1
        _state.value = if (remaining <= 0) SoftInterventionState() else current.copy(
            remainingSeconds = remaining
        )
    }

    /** Repeated events must never stack a second intervention. */
    fun onResponseWhileActive(response: BehaviorAnalysisResponse) {
        if (_state.value.active) return
        onResponse(response)
    }

    val isCooldownActive: Boolean
        get() = clock.nowMillis() < cooldownUntilMillis

    fun reset() {
        cooldownUntilMillis = 0L
        _state.value = SoftInterventionState()
    }

    companion object {
        const val DEFAULT_COOLDOWN_SECONDS = 60
    }
}
