package com.jeda.android.monitoring.intervention

data class HardInterventionState(
    val active: Boolean = false,
    val remainingSeconds: Int = 0,
    val rrs: Double = 0.0,
    val reason: String = "Sustained high visual risk detected.",
    val endsAtMillis: Long = 0L
)
