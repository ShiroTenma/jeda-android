package com.jeda.android.monitoring

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationContentFormatterTest {
    @Test
    fun `target foreground status names the monitored app`() {
        val content = NotificationContentFormatter.format(
            enabled = true,
            state = MonitoringState.MONITORING,
            packageName = "com.instagram.android"
        )

        assertEquals("JEDA aktif", content.title)
        assertEquals("Monitoring Instagram", content.text)
    }

    @Test
    fun `non target foreground status is explicit`() {
        val content = NotificationContentFormatter.format(
            enabled = true,
            state = MonitoringState.IDLE,
            packageName = "com.whatsapp"
        )

        assertEquals("JEDA aktif", content.title)
        assertEquals("Tidak memonitor aplikasi ini", content.text)
    }

    @Test
    fun `disabled monitoring has stopped status`() {
        val content = NotificationContentFormatter.format(
            enabled = false,
            state = MonitoringState.IDLE,
            packageName = null
        )

        assertEquals("JEDA tidak aktif", content.title)
        assertEquals("Monitoring dihentikan", content.text)
    }
}
