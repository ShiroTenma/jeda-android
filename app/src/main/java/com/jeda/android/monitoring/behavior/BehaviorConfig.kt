package com.jeda.android.monitoring.behavior

/**
 * M2 baseline parameters, mirroring SRS Appendix D.3.
 *
 * Every value here is a calibration candidate, not a validated measurement. Under
 * Evolutionary Prototyping these get tuned during stage 5 and each change has to
 * be recorded in the test documentation. Nothing outside this file should hold a
 * behavioural magic number.
 */
data class BehaviorConfig(
    val observationWindowMillis: Long = 30_000L,
    val idleStartMillis: Long = 10_000L,
    val scrollNormalizationMax: Int = 60,
    val switchNormalizationMax: Int = 6,
    val sessionNormalizationSeconds: Long = 300L,
    val idleRecoveryMaxSeconds: Long = 60L,
    val decayFactor: Double = 0.5
) {
    val observationWindowSeconds: Long get() = observationWindowMillis / 1000L
    val idleStartSeconds: Long get() = idleStartMillis / 1000L

    fun normalizeScroll(events: Int): Double =
        (events.toDouble() / scrollNormalizationMax).coerceIn(0.0, 1.0)

    fun normalizeSwitches(count: Int): Double =
        (count.toDouble() / switchNormalizationMax).coerceIn(0.0, 1.0)

    fun normalizeSession(seconds: Long): Double =
        (seconds.toDouble() / sessionNormalizationSeconds).coerceIn(0.0, 1.0)

    fun normalizeIdle(seconds: Long): Double =
        (seconds.toDouble() / idleRecoveryMaxSeconds).coerceIn(0.0, 1.0)
}
