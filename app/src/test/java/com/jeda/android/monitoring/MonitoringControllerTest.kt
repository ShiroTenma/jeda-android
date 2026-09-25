package com.jeda.android.monitoring

import org.junit.Assert.assertEquals
import org.junit.Test

class MonitoringControllerTest {
    @Test
    fun `non-target apps do not trigger monitoring`() {
        val controller = MonitoringController()
        controller.start()
        assertEquals(MonitoringState.IDLE, controller.state)

        controller.onForegroundPackage("com.whatsapp")
        assertEquals(MonitoringState.IDLE, controller.state)
    }

    @Test
    fun `target apps transition to monitoring state`() {
        val controller = MonitoringController()
        controller.start()
        assertEquals(MonitoringState.IDLE, controller.state)

        val newState = controller.onForegroundPackage("com.instagram.android")
        assertEquals(MonitoringState.MONITORING, newState)
        assertEquals(MonitoringState.MONITORING, controller.state)
    }

    @Test
    fun `stop clears enabled flag and resets state`() {
        val controller = MonitoringController()
        controller.start()
        assertEquals(true, controller.enabled)
        assertEquals(MonitoringState.IDLE, controller.state)

        controller.stop()
        assertEquals(false, controller.enabled)
        assertEquals(MonitoringState.IDLE, controller.state)
    }

    @Test
    fun `disabled monitoring stays idle regardless of package`() {
        val controller = MonitoringController()
        // disabled by default
        assertEquals(false, controller.enabled)
        assertEquals(MonitoringState.IDLE, controller.state)

        controller.onForegroundPackage("com.instagram.android")
        assertEquals(MonitoringState.IDLE, controller.state)
    }
}