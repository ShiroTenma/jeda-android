package com.jeda.android.monitoring

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class JedaAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        MonitoringManager.controller.onForegroundPackage(packageName)
    }

    override fun onInterrupt() {
        MonitoringManager.controller.stop()
    }
}
