package com.jeda.android.monitoring.intervention

import com.jeda.android.monitoring.behavior.FakeBehaviorClock
import org.junit.Assert.assertEquals
import org.junit.Test

class InterventionControllerTest {
    private val clock = FakeBehaviorClock()
    private val store = InMemoryTimestampStore()
    private val controller = InterventionController(clock, store)

    @Test
    fun `starts at zero`() {
        assertEquals(0, controller.softCount())
    }

    @Test
    fun `recording increments the count`() {
        controller.recordSoftIntervention()
        controller.recordSoftIntervention()
        assertEquals(2, controller.softCount())
    }

    @Test
    fun `entries inside the rolling window are retained`() {
        controller.recordSoftIntervention()
        clock.advanceSeconds(23 * 3600)
        assertEquals(1, controller.softCount())
    }

    @Test
    fun `entries older than the rolling window are pruned`() {
        controller.recordSoftIntervention()
        clock.advanceSeconds(25 * 3600)
        assertEquals(0, controller.softCount())
    }

    @Test
    fun `recording prunes stale entries`() {
        controller.recordSoftIntervention()
        clock.advanceSeconds(25 * 3600)
        controller.recordSoftIntervention()
        assertEquals(1, controller.softCount())
    }

    @Test
    fun `reset clears the log`() {
        controller.recordSoftIntervention()
        controller.reset()
        assertEquals(0, controller.softCount())
    }

    @Test
    fun `a custom rolling window is honoured`() {
        val shortWindow = InterventionController(clock, store, rollingHours = 1)
        shortWindow.recordSoftIntervention()
        clock.advanceSeconds(2 * 3600)
        assertEquals(0, shortWindow.softCount())
    }
}
