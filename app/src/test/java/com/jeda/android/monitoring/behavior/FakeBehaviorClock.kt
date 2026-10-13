package com.jeda.android.monitoring.behavior

/** Deterministic clock. M2 aggregates on wall-clock time, so tests advance it explicitly. */
class FakeBehaviorClock(private var now: Long = 0L) : BehaviorClock {
    override fun nowMillis(): Long = now

    fun advanceSeconds(seconds: Long) {
        now += seconds * 1000L
    }

    fun advanceMillis(millis: Long) {
        now += millis
    }
}
