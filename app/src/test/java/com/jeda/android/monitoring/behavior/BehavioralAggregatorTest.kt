package com.jeda.android.monitoring.behavior

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BehavioralAggregatorTest {
    private val clock = FakeBehaviorClock()
    private val aggregator = BehavioralAggregator(clock, BehaviorConfig())

    private val instagram = "com.instagram.android"
    private val tiktok = "com.zhiliaoapp.musically"
    private val chrome = "com.android.chrome"

    @Before
    fun setUp() {
        aggregator.start()
        aggregator.onForegroundPackage(instagram)
    }

    // --- scope -----------------------------------------------------------------

    @Test
    fun `scroll events from browsers are discarded`() {
        repeat(20) { aggregator.onScrollEvent(chrome) }
        assertEquals(0, aggregator.metricsFlow.value.scrollEvents)
    }

    @Test
    fun `scroll events from excluded apps are discarded`() {
        repeat(20) { aggregator.onScrollEvent("com.whatsapp") }
        assertEquals(0, aggregator.metricsFlow.value.scrollEvents)
    }

    @Test
    fun `events before start are ignored`() {
        val fresh = BehavioralAggregator(clock, BehaviorConfig())
        repeat(20) { fresh.onScrollEvent(instagram) }
        fresh.onForegroundPackage(instagram)

        assertEquals(0, fresh.metricsFlow.value.scrollEvents)
        assertNull(fresh.metricsFlow.value.foregroundPackage)
    }

    // --- switching -------------------------------------------------------------

    @Test
    fun `entering the first app is not a switch`() {
        val metrics = aggregator.metricsFlow.value
        assertEquals(0, metrics.socialAppSwitches)
    }

    @Test
    fun `moving between two social apps counts one switch`() {
        aggregator.onForegroundPackage(tiktok)
        assertEquals(1, aggregator.metricsFlow.value.socialAppSwitches)
    }

    @Test
    fun `repeat window events for the same app continue the session`() {
        clock.advanceSeconds(20)
        aggregator.onForegroundPackage(instagram)
        assertEquals(20L, aggregator.metricsFlow.value.activeSocialSessionSeconds)
    }

    @Test
    fun `entering a browser is neither a switch nor a session`() {
        clock.advanceSeconds(30)
        aggregator.onForegroundPackage(chrome)

        val metrics = aggregator.metricsFlow.value
        assertEquals(0, metrics.socialAppSwitches)
        assertEquals(0L, metrics.activeSocialSessionSeconds)
        assertNull(metrics.foregroundPackage)
    }

    // --- idle ------------------------------------------------------------------

    @Test
    fun `idle only accrues past the ten second threshold`() {
        clock.advanceSeconds(9)
        aggregator.tick()
        assertEquals(0L, aggregator.metricsFlow.value.idleSeconds)

        clock.advanceSeconds(2)
        aggregator.tick()
        assertEquals(1L, aggregator.metricsFlow.value.idleSeconds)
    }

    @Test
    fun `idle saturates at the recovery reference`() {
        clock.advanceSeconds(70)
        aggregator.tick()

        val metrics = aggregator.metricsFlow.value
        assertEquals(60L, metrics.idleSeconds)
        assertEquals(1.0, metrics.normalizedIdle, 1e-6)
    }

    @Test
    fun `activity resets the idle clock`() {
        clock.advanceSeconds(30)
        aggregator.onScrollEvent(instagram)
        assertEquals(0L, aggregator.metricsFlow.value.idleSeconds)
    }

    // --- normalization boundaries ----------------------------------------------

    @Test
    fun `scroll normalization saturates at sixty`() {
        repeat(60) { aggregator.onScrollEvent(instagram) }
        assertEquals(1.0, aggregator.metricsFlow.value.normalizedScroll, 1e-6)

        repeat(60) { aggregator.onScrollEvent(instagram) }
        assertEquals(120, aggregator.metricsFlow.value.scrollEvents)
        assertEquals(1.0, aggregator.metricsFlow.value.normalizedScroll, 1e-6)
    }

    @Test
    fun `switch normalization saturates at six`() {
        repeat(6) {
            aggregator.onForegroundPackage(tiktok)
            aggregator.onForegroundPackage(instagram)
        }
        assertEquals(1.0, aggregator.metricsFlow.value.normalizedSwitches, 1e-6)
    }

    @Test
    fun `session normalization saturates at the reference`() {
        clock.advanceSeconds(300)
        aggregator.tick()
        assertEquals(1.0, aggregator.metricsFlow.value.normalizedSession, 1e-6)

        clock.advanceSeconds(300)
        aggregator.tick()
        assertEquals(1.0, aggregator.metricsFlow.value.normalizedSession, 1e-6)
    }

    // --- window decay ----------------------------------------------------------

    @Test
    fun `counters decay by one window step`() {
        repeat(60) { aggregator.onScrollEvent(instagram) }
        assertEquals(60, aggregator.metricsFlow.value.scrollEvents)

        clock.advanceSeconds(30)
        aggregator.tick()
        assertEquals(30, aggregator.metricsFlow.value.scrollEvents)
    }

    @Test
    fun `switch counters decay as well`() {
        repeat(6) {
            aggregator.onForegroundPackage(tiktok)
            aggregator.onForegroundPackage(instagram)
        }
        assertEquals(12, aggregator.metricsFlow.value.socialAppSwitches)

        clock.advanceSeconds(30)
        aggregator.tick()
        assertEquals(6, aggregator.metricsFlow.value.socialAppSwitches)
    }

    @Test
    fun `a long gap collapses counters through repeated decay`() {
        repeat(60) { aggregator.onScrollEvent(instagram) }

        clock.advanceSeconds(600)
        aggregator.tick()
        assertEquals(0, aggregator.metricsFlow.value.scrollEvents)
    }

    // --- lifecycle -------------------------------------------------------------

    @Test
    fun `stop performs a full reset rather than a decay`() {
        repeat(60) { aggregator.onScrollEvent(instagram) }
        clock.advanceSeconds(5)
        aggregator.onForegroundPackage(tiktok)

        aggregator.stop()
        val after = aggregator.metricsFlow.value

        assertEquals(0, after.scrollEvents)
        assertEquals(0, after.socialAppSwitches)
        assertEquals(0L, after.activeSocialSessionSeconds)
        assertEquals(0L, after.idleSeconds)
        assertNull(after.foregroundPackage)
    }

    @Test
    fun `restart issues a new session id`() {
        val first = aggregator.snapshot().sessionId

        aggregator.stop()
        aggregator.start()

        assertNotEquals(first, aggregator.snapshot().sessionId)
    }

    @Test
    fun `tick is inert while monitoring is stopped`() {
        aggregator.stop()
        clock.advanceSeconds(120)
        aggregator.tick()

        val metrics = aggregator.metricsFlow.value
        assertEquals(0L, metrics.activeSocialSessionSeconds)
        assertEquals(0L, metrics.idleSeconds)
        assertEquals(0L, metrics.windowElapsedSeconds)
    }

    // --- snapshot contract -----------------------------------------------------

    @Test
    fun `a completed window is reported before decay is applied`() {
        repeat(60) { aggregator.onScrollEvent(instagram) }

        clock.advanceSeconds(30)
        val completed = aggregator.consumeCompletedWindow()

        assertEquals(60, completed!!.scrollEvents)
        assertNull(aggregator.consumeCompletedWindow())
        assertEquals(30, aggregator.metricsFlow.value.scrollEvents)
    }

    @Test
    fun `no completed window is available before the boundary`() {
        repeat(20) { aggregator.onScrollEvent(instagram) }
        clock.advanceSeconds(10)
        assertNull(aggregator.consumeCompletedWindow())
    }

    @Test
    fun `stop discards a pending completed window`() {
        repeat(20) { aggregator.onScrollEvent(instagram) }
        clock.advanceSeconds(30)
        aggregator.stop()
        aggregator.start()

        assertNull(aggregator.consumeCompletedWindow())
    }

    @Test
    fun `snapshot carries raw counters with a stable session and fresh request id`() {
        repeat(10) { aggregator.onScrollEvent(instagram) }
        clock.advanceSeconds(4)

        val first = aggregator.snapshot()
        val second = aggregator.snapshot()

        assertEquals(10, first.scrollEvents)
        assertEquals(0, first.socialAppSwitches)
        assertEquals(4L, first.activeSocialSessionSeconds)
        assertEquals(30L, first.observationWindowSeconds)
        assertEquals(instagram, first.foregroundPackage)
        assertEquals(0, first.softInterventionCount24h)

        assertEquals(first.sessionId, second.sessionId)
        assertNotEquals(first.requestId, second.requestId)
    }
}
