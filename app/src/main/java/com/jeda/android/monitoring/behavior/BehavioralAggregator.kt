package com.jeda.android.monitoring.behavior

import com.jeda.android.monitoring.TargetAppPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * M2 behavioral aggregator. Produces the four raw counters FDS needs, over a
 * rolling 30-second observation window, and nothing else.
 *
 * Only packages in the FDS scope contribute. Browsers are RRS-scope: their scroll
 * events are discarded here, so browser metadata can never reach the behavioral
 * score (SRS Appendix D.2, FR-10).
 *
 * Session semantics, decided here because FR-06 is ambiguous: an app change starts
 * a *new* session run rather than continuing the old one, and leaving the FDS scope
 * ends the run entirely. Recorded here for the calibration log required by SRS
 * section 6.2.
 *
 * Not thread-safe. All calls are expected on the main thread or the heartbeat
 * dispatcher.
 */
class BehavioralAggregator(
    private val clock: BehaviorClock = SystemBehaviorClock,
    private val config: BehaviorConfig = BehaviorConfig()
) {
    private val _metrics = MutableStateFlow(BehavioralMetrics.EMPTY)
    val metricsFlow: StateFlow<BehavioralMetrics> = _metrics.asStateFlow()

    private var sessionId: String = newSessionId()
    private var started = false

    private var windowStartMillis = 0L
    private var lastRelevantEventMillis = 0L

    private var currentPackage: String? = null
    private var sessionStartMillis: Long? = null

    private var scrollEvents = 0
    private var socialAppSwitches = 0
    private var windowsRolled = 0L
    private var completedWindow: BehavioralSnapshot? = null

    fun start() {
        if (started) return
        started = true
        sessionId = newSessionId()
        windowStartMillis = clock.nowMillis()
        lastRelevantEventMillis = windowStartMillis
        currentPackage = null
        sessionStartMillis = null
        scrollEvents = 0
        socialAppSwitches = 0
        windowsRolled = 0L
        completedWindow = null
        publish()
    }

    /** Full reset, not a decay. SRS D.3 requires a clean session on stop. */
    fun stop() {
        started = false
        sessionId = newSessionId()
        windowStartMillis = 0L
        lastRelevantEventMillis = 0L
        currentPackage = null
        sessionStartMillis = null
        scrollEvents = 0
        socialAppSwitches = 0
        windowsRolled = 0L
        completedWindow = null
        publish()
    }

    fun onScrollEvent(packageName: String) {
        if (!started) return
        if (!TargetAppPolicy.isFdsScope(packageName)) return
        rollWindows(clock.nowMillis())
        scrollEvents++
        lastRelevantEventMillis = clock.nowMillis()
        publish()
    }

    fun onForegroundPackage(packageName: String) {
        if (!started) return
        rollWindows(clock.nowMillis())

        if (!TargetAppPolicy.isFdsScope(packageName)) {
            if (currentPackage != null) {
                currentPackage = null
                sessionStartMillis = null
            }
            publish()
            return
        }

        val now = clock.nowMillis()
        if (currentPackage == packageName) {
            // Same app, new window event (in-app navigation). Session continues.
            publish()
            return
        }

        if (currentPackage != null) {
            socialAppSwitches++
        }
        currentPackage = packageName
        sessionStartMillis = now
        lastRelevantEventMillis = now
        publish()
    }

    /**
     * Wall-clock heartbeat. No accessibility event fires while the user is idle, so
     * without this the idle and session timers would never advance and window
     * decay would never fire.
     */
    fun tick() {
        if (!started) return
        rollWindows(clock.nowMillis())
        publish()
    }

    /**
     * Returns the observation window that has just closed, exactly once, then
     * discards it.
     *
     * The snapshot is captured *before* decay is applied, so the window that is
     * scored is the one the user actually produced. Rolling the window first and
     * reading the counters afterwards would halve every score, and the FDS
     * thresholds were calibrated against the undiminished values.
     */
    fun consumeCompletedWindow(): BehavioralSnapshot? {
        tick()
        val completed = completedWindow
        completedWindow = null
        return completed
    }

    fun snapshot(): BehavioralSnapshot {
        tick()
        val metrics = _metrics.value
        return BehavioralSnapshot(
            sessionId = sessionId,
            requestId = UUID.randomUUID().toString(),
            observationWindowSeconds = config.observationWindowSeconds,
            scrollEvents = metrics.scrollEvents,
            socialAppSwitches = metrics.socialAppSwitches,
            activeSocialSessionSeconds = metrics.activeSocialSessionSeconds,
            idleSeconds = metrics.idleSeconds,
            foregroundPackage = metrics.foregroundPackage,
            softInterventionCount24h = 0
        )
    }

    /**
     * Applies gradual decay once per elapsed whole window. Computed as a single
     * exponent so a large clock jump (app backgrounded) costs nothing and still
     * decays correctly instead of spinning.
     */
    private fun rollWindows(now: Long) {
        val elapsed = now - windowStartMillis
        if (elapsed < config.observationWindowMillis) return
        val windows = elapsed / config.observationWindowMillis
        val factor = Math.pow(config.decayFactor, windows.toDouble())

        completedWindow = buildSnapshot(
            scroll = scrollEvents,
            switches = socialAppSwitches,
            now = now
        )

        scrollEvents = (scrollEvents * factor).toInt()
        socialAppSwitches = (socialAppSwitches * factor).toInt()
        windowStartMillis += windows * config.observationWindowMillis
        windowsRolled += windows
    }

    private fun buildSnapshot(scroll: Int, switches: Int, now: Long) = BehavioralSnapshot(
        sessionId = sessionId,
        requestId = UUID.randomUUID().toString(),
        observationWindowSeconds = config.observationWindowSeconds,
        scrollEvents = scroll,
        socialAppSwitches = switches,
        activeSocialSessionSeconds = sessionSecondsAt(now),
        idleSeconds = idleSecondsAt(now),
        foregroundPackage = currentPackage,
        softInterventionCount24h = 0
    )

    private fun publish() {
        _metrics.value = computeMetrics(clock.nowMillis())
    }

    private fun computeMetrics(now: Long): BehavioralMetrics {
        val sessionSeconds = sessionSecondsAt(now)
        val idleSeconds = idleSecondsAt(now)

        return BehavioralMetrics(
            scrollEvents = scrollEvents,
            socialAppSwitches = socialAppSwitches,
            activeSocialSessionSeconds = sessionSeconds,
            idleSeconds = idleSeconds,
            normalizedScroll = config.normalizeScroll(scrollEvents),
            normalizedSwitches = config.normalizeSwitches(socialAppSwitches),
            normalizedSession = config.normalizeSession(sessionSeconds),
            normalizedIdle = config.normalizeIdle(idleSeconds),
            foregroundPackage = currentPackage,
            windowElapsedSeconds = ((now - windowStartMillis) / 1000L).coerceAtLeast(0L),
            windowsRolled = windowsRolled
        )
    }

    private fun sessionSecondsAt(now: Long): Long = sessionStartMillis
        ?.let { ((now - it) / 1000L).coerceAtLeast(0L) }
        ?: 0L

    private fun idleSecondsAt(now: Long): Long {
        val sinceLastEvent = ((now - lastRelevantEventMillis) / 1000L).coerceAtLeast(0L)
        return (sinceLastEvent - config.idleStartSeconds).coerceAtLeast(0L)
    }

    private fun newSessionId(): String = UUID.randomUUID().toString()
}
