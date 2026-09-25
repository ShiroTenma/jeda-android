package com.jeda.android.monitoring

data class NotificationContent(val title: String, val text: String)

object NotificationContentFormatter {
    private val appNames = mapOf(
        "com.instagram.android" to "Instagram",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.twitter.android" to "X",
        "com.facebook.katana" to "Facebook",
        "com.android.chrome" to "Chrome",
        "org.mozilla.firefox" to "Firefox",
        "com.brave.browser" to "Brave",
        "com.opera.browser" to "Opera",
        "com.UCMobile.intl" to "UC Browser"
    )

    fun format(
        enabled: Boolean,
        state: MonitoringState,
        packageName: String?
    ): NotificationContent {
        if (!enabled) {
            return NotificationContent("JEDA tidak aktif", "Monitoring dihentikan")
        }
        val appName = packageName?.let { appNames[it] }
        return if (state == MonitoringState.MONITORING && appName != null) {
            NotificationContent("JEDA aktif", "Monitoring $appName")
        } else {
            NotificationContent("JEDA aktif", "Tidak memonitor aplikasi ini")
        }
    }
}
