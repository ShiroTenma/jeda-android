package com.jeda.android.monitoring.behavior

/**
 * Injectable time source. M2 aggregates on wall-clock time, so the clock has to be
 * swappable or idle accrual and window decay cannot be unit-tested without real
 * delays. Production uses [SystemBehaviorClock]; tests supply a fake.
 */
interface BehaviorClock {
    fun nowMillis(): Long
}

object SystemBehaviorClock : BehaviorClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
