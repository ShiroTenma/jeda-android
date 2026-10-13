package com.jeda.android.monitoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `behavioural scope is social media only`() {
        TargetAppPolicy.socialPackages.forEach {
            assertTrue("$it should be behavioural scope", TargetAppPolicy.isFdsScope(it))
        }
        TargetAppPolicy.browserPackages.forEach {
            assertFalse("$it must not feed FDS", TargetAppPolicy.isFdsScope(it))
        }
    }

    @Test
    fun `visual scope covers social media and browsers`() {
        TargetAppPolicy.rrsPackages.forEach {
            assertTrue("$it should be visual scope", TargetAppPolicy.isRrsScope(it))
        }
    }

    @Test
    fun `excluded apps sit in neither scope`() {
        val excluded = listOf(
            "com.whatsapp",
            "org.telegram.messenger",
            "com.discord",
            "org.thoughtcrime.securesms",
            "com.android.settings",
            "com.microsoft.office.outlook",
            "com.android.gallery3d",
            "com.android.documentsui"
        )
        excluded.forEach {
            assertFalse("$it must not be behavioural scope", TargetAppPolicy.isFdsScope(it))
            assertFalse("$it must not be visual scope", TargetAppPolicy.isRrsScope(it))
        }
    }

    @Test
    fun `scopes are declared separately and browsers are only additive`() {
        assertTrue(TargetAppPolicy.socialPackages.containsAll(TargetAppPolicy.fdsPackages))
        assertTrue(TargetAppPolicy.rrsPackages.containsAll(TargetAppPolicy.browserPackages))
        assertFalse(TargetAppPolicy.fdsPackages.contains("com.android.chrome"))
    }
}
