package com.jeda.android.monitoring

/**
 * Explicit monitoring scopes. The two sets are deliberately different: behavioural
 * collection (FDS) is social media only, while visual sampling (RRS) also covers
 * browsers. SRS Appendix D.2 forbids browser interaction metadata from reaching the
 * behavioural score, so a single flat allowlist is not safe here.
 *
 * Private messaging, email, banking, gallery and file managers appear in neither set.
 */
object TargetAppPolicy {
    val socialPackages: Set<String> = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.twitter.android",
        "com.facebook.katana"
    )

    val browserPackages: Set<String> = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.brave.browser",
        "com.opera.browser",
        "com.UCMobile.intl"
    )

    val fdsPackages: Set<String> = socialPackages
    val rrsPackages: Set<String> = socialPackages + browserPackages

    /** Behavioural sensing scope. Social media only. */
    fun isFdsScope(packageName: String): Boolean = packageName in fdsPackages

    /** Visual sampling scope. Social media and browsers. */
    fun isRrsScope(packageName: String): Boolean = packageName in rrsPackages

    /** True if the app is monitored by any path. Not a substitute for [isFdsScope]. */
    fun isTarget(packageName: String): Boolean = isRrsScope(packageName)
}

enum class MonitoringState {
    IDLE,
    MONITORING,
    INTERVENTION,
    COOLDOWN
}
