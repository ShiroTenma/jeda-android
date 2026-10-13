package com.jeda.android.monitoring.visual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VisualSamplingManagerTest {

    @Before
    fun setup() {
        VisualSamplingManager.setElevatedSampling(false)
        VisualSamplingManager.isEnabled.set(false)
    }

    @Test
    fun `default interval is 3000 ms`() {
        assertEquals(3000L, VisualSamplingManager.currentIntervalMillis)
    }

    @Test
    fun `elevated interval is 2000 ms`() {
        VisualSamplingManager.setElevatedSampling(true)
        assertEquals(2000L, VisualSamplingManager.currentIntervalMillis)

        VisualSamplingManager.setElevatedSampling(false)
        assertEquals(3000L, VisualSamplingManager.currentIntervalMillis)
    }

    @Test
    fun `shouldSample returns false when disabled`() {
        VisualSamplingManager.isEnabled.set(false)
        assertFalse(VisualSamplingManager.shouldSample())
    }
}
