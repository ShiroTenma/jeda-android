package com.jeda.android.monitoring

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class JedaAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                MonitoringManager.controller.onForegroundPackage(packageName)
                MonitoringManager.aggregator.onForegroundPackage(packageName)
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                MonitoringManager.aggregator.onScrollEvent(packageName)
            }
        }
    }

    /**
     * Deliberately empty. UC-08 is the only sanctioned stop path, and the system
     * calls this whenever another accessibility service takes priority — wiring it
     * to stop would let an unrelated service silently end a monitoring session.
     */
    override fun onInterrupt() = Unit
}
