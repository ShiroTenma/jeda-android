package com.jeda.android.monitoring

/** Explicit allowlist; private messaging and unrelated apps remain excluded. */
object TargetAppPolicy {
    val packages: Set<String> = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.twitter.android",
        "com.facebook.katana",
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.brave.browser",
        "com.opera.browser",
        "com.UCMobile.intl"
    )

    fun isTarget(packageName: String): Boolean = packageName in packages
}

enum class MonitoringState {
    IDLE,
    MONITORING,
    INTERVENTION,
    COOLDOWN
}
