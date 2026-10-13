package com.jeda.android.monitoring.intervention

import com.jeda.android.monitoring.behavior.BehaviorClock

/**
 * Rolling 24-hour counter of soft interventions, persisted so escalation survives
 * app restarts and does not reset every time the user switches apps.
 *
 * The count is client-asserted and forwarded to the backend, which is acceptable on
 * a private tailnet for a prototype but is not independently verified.
 */
class InterventionController(
    private val clock: BehaviorClock,
    private val store: TimestampStore,
    private val rollingHours: Int = DEFAULT_ROLLING_HOURS
) {
    fun softCount(): Int = store.read().count { it >= cutoff() }

    fun recordSoftIntervention() {
        store.write(store.read().filter { it >= cutoff() } + clock.nowMillis())
    }

    fun reset() = store.write(emptyList())

    private fun cutoff(): Long = clock.nowMillis() - rollingHours * MILLIS_PER_HOUR

    companion object {
        const val DEFAULT_ROLLING_HOURS = 24
        private const val MILLIS_PER_HOUR = 3_600_000L
    }
}
