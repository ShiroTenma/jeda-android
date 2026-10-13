package com.jeda.android.monitoring.behavior

/** Live aggregate for the debug surface. Raw plus normalized, never persisted. */
data class BehavioralMetrics(
    val scrollEvents: Int,
    val socialAppSwitches: Int,
    val activeSocialSessionSeconds: Long,
    val idleSeconds: Long,
    val normalizedScroll: Double,
    val normalizedSwitches: Double,
    val normalizedSession: Double,
    val normalizedIdle: Double,
    val foregroundPackage: String?,
    val windowElapsedSeconds: Long,
    val windowsRolled: Long
) {
    companion object {
        val EMPTY = BehavioralMetrics(
            scrollEvents = 0,
            socialAppSwitches = 0,
            activeSocialSessionSeconds = 0L,
            idleSeconds = 0L,
            normalizedScroll = 0.0,
            normalizedSwitches = 0.0,
            normalizedSession = 0.0,
            normalizedIdle = 0.0,
            foregroundPackage = null,
            windowElapsedSeconds = 0L,
            windowsRolled = 0L
        )
    }
}
