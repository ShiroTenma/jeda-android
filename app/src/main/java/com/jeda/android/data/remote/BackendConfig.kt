package com.jeda.android.data.remote

/**
 * Backend connection settings. Defaults match the SRS D.1 baseline: MagicDNS name
 * `jeda-backend` on port 8000, with a 3-second request timeout.
 */
data class BackendConfig(
    val host: String = DEFAULT_HOST,
    val port: Int = DEFAULT_PORT,
    val token: String = "",
    val timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS
) {
    val baseUrl: String get() = "http://$host:$port/"

    val isUsable: Boolean
        get() = host.isNotBlank() && port in 1..65535 && timeoutSeconds > 0

    companion object {
        const val DEFAULT_HOST = "jeda-backend"
        const val DEFAULT_PORT = 8000
        const val DEFAULT_TIMEOUT_SECONDS = 3
    }
}
