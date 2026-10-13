package com.jeda.android.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendConfigTest {
    @Test
    fun `defaults match the SRS D1 baseline`() {
        val config = BackendConfig()
        assertEquals("jeda-backend", config.host)
        assertEquals(8000, config.port)
        assertEquals(3, config.timeoutSeconds)
        assertEquals("http://jeda-backend:8000/", config.baseUrl)
    }

    @Test
    fun `a complete config is usable`() {
        assertTrue(BackendConfig(host = "desktop-abc123", port = 9000, token = "t").isUsable)
    }

    @Test
    fun `a blank host is unusable`() {
        assertFalse(BackendConfig(host = "   ").isUsable)
    }

    @Test
    fun `ports outside the valid range are unusable`() {
        assertFalse(BackendConfig(port = 0).isUsable)
        assertFalse(BackendConfig(port = -1).isUsable)
        assertFalse(BackendConfig(port = 70_000).isUsable)
    }

    @Test
    fun `a non positive timeout is unusable`() {
        assertFalse(BackendConfig(timeoutSeconds = 0).isUsable)
    }
}
