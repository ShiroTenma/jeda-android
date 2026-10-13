package com.jeda.android.monitoring.intervention

import com.jeda.android.data.remote.BehaviorAnalysisResponse
import com.jeda.android.monitoring.behavior.BehaviorConfig
import com.jeda.android.monitoring.behavior.BehavioralAggregator
import com.jeda.android.monitoring.behavior.FakeBehaviorClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InterventionCoordinatorTest {
    private val clock = FakeBehaviorClock()
    private val aggregator = BehavioralAggregator(clock, BehaviorConfig())
    private val interventions = InterventionController(clock, InMemoryTimestampStore())
    private val coordinator = InterventionCoordinator(
        aggregator = aggregator,
        controller = interventions,
        clock = clock,
        cooldownSeconds = 60
    )

    private val instagram = "com.instagram.android"

    @Before
    fun setUp() {
        aggregator.start()
        aggregator.onForegroundPackage(instagram)
        coordinator.reset()
    }

    // --- evaluation cadence ----------------------------------------------------

    @Test
    fun `no snapshot before an observation window rolls`() {
        repeat(5) {
            clock.advanceSeconds(1)
            assertNull(coordinator.onHeartbeat())
        }
    }

    @Test
    fun `a snapshot is produced once per window`() {
        repeat(60) { aggregator.onScrollEvent(instagram) }
        clock.advanceSeconds(30)

        val snapshot = coordinator.onHeartbeat()
        assertNotNull(snapshot)
        assertEquals(60, snapshot!!.scrollEvents)
        assertEquals(0, interventions.softCount())

        assertNull(coordinator.onHeartbeat())
    }

    // --- responses -------------------------------------------------------------

    @Test
    fun `a normal response changes nothing`() {
        coordinator.onResponse(response(action = "NONE", cooldown = 0))
        assertFalse(coordinator.stateFlow.value.active)
        assertEquals(0, interventions.softCount())
    }

    @Test
    fun `a soft response activates the intervention`() {
        coordinator.onResponse(soft(cooldown = 8))

        val state = coordinator.stateFlow.value
        assertTrue(state.active)
        assertEquals(8, state.remainingSeconds)
        assertEquals(8, state.totalSeconds)
        assertEquals(58.2, state.fds, 1e-6)
        assertEquals("ELEVATED", state.level)
        assertEquals(1, interventions.softCount())
    }

    @Test
    fun `only the soft action is honoured`() {
        coordinator.onResponse(response(fds = 0.99, level = "HIGH", action = "HARD_INTERVENTION", cooldown = 60))
        assertFalse(coordinator.stateFlow.value.active)
    }

    @Test
    fun `a zero cooldown response is ignored`() {
        coordinator.onResponse(response(action = "SOFT_INTERVENTION", cooldown = 0))
        assertFalse(coordinator.stateFlow.value.active)
    }

    // --- countdown -------------------------------------------------------------

    @Test
    fun `the countdown decrements and then clears`() {
        coordinator.onResponse(soft(cooldown = 3))

        coordinator.onSecondElapsed()
        coordinator.onSecondElapsed()
        assertEquals(1, coordinator.stateFlow.value.remainingSeconds)

        coordinator.onSecondElapsed()
        assertFalse(coordinator.stateFlow.value.active)
    }

    @Test
    fun `ticking with no active intervention is a no-op`() {
        repeat(3) { coordinator.onSecondElapsed() }
        assertEquals(0, coordinator.stateFlow.value.remainingSeconds)
    }

    // --- cooldown --------------------------------------------------------------

    @Test
    fun `cooldown suppresses evaluation for the next window`() {
        coordinator.onResponse(soft(cooldown = 8))

        clock.advanceSeconds(30)
        assertNull(coordinator.onHeartbeat())

        clock.advanceSeconds(40)
        assertNotNull(coordinator.onHeartbeat())
    }

    // --- anti-spam -------------------------------------------------------------

    @Test
    fun `a second response cannot stack another intervention`() {
        coordinator.onResponse(soft(cooldown = 30))
        coordinator.onResponseWhileActive(soft(cooldown = 30, fds = 91.0, level = "HIGH"))

        val state = coordinator.stateFlow.value
        assertEquals(30, state.remainingSeconds)
        assertEquals(1, interventions.softCount())
    }

    @Test
    fun `reset clears intervention state`() {
        coordinator.onResponse(soft(cooldown = 8))
        coordinator.reset()
        assertFalse(coordinator.stateFlow.value.active)
    }

    private fun soft(cooldown: Int, fds: Double = 58.2, level: String = "ELEVATED") =
        response(fds = fds, level = level, action = "SOFT_INTERVENTION", cooldown = cooldown)

    private fun response(
        fds: Double = 0.0,
        level: String = "NORMAL",
        action: String = "NONE",
        cooldown: Int = 0
    ) = BehaviorAnalysisResponse(
        fds = fds,
        level = level,
        action = action,
        cooldownSeconds = cooldown
    )
}
