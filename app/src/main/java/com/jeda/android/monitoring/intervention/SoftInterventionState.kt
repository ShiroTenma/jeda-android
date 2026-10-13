package com.jeda.android.monitoring.intervention

data class SoftInterventionState(
    val active: Boolean = false,
    val fds: Double = 0.0,
    val level: String = "",
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0
)
