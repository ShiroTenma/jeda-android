package com.jeda.android.monitoring

import org.junit.Assert.assertEquals
import org.junit.Test

class MonitoringPolicyTest {
    @Test
    fun `target packages are monitored and non targets are ignored`() {
        assertEquals(true, TargetAppPolicy.isTarget("com.instagram.android"))
        assertEquals(true, TargetAppPolicy.isTarget("com.android.chrome"))
        assertEquals(false, TargetAppPolicy.isTarget("com.whatsapp"))
        assertEquals(false, TargetAppPolicy.isTarget("com.android.settings"))
    }

    @Test
    fun `foreground package maps to monitoring state`() {
        val controller = MonitoringController()
        controller.start()
        assertEquals(MonitoringState.MONITORING, controller.onForegroundPackage("com.instagram.android"))
        assertEquals(MonitoringState.IDLE, controller.onForegroundPackage("com.whatsapp"))
        controller.stop()
        assertEquals(MonitoringState.IDLE, controller.state)
    }
}
