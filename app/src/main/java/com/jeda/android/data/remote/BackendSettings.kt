package com.jeda.android.data.remote

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persisted backend settings. Replaces the hardcoded URL that M1 shipped with.
 *
 * The token is stored in plain preferences. For a prototype on a private tailnet
 * that is acceptable, but it is not encrypted at rest and should not be treated as
 * protecting against local access to the device.
 */
object BackendSettings {
    private const val PREFS_NAME = "jeda_backend_settings"
    private const val KEY_HOST = "host"
    private const val KEY_PORT = "port"
    private const val KEY_TOKEN = "token"
    private const val KEY_TIMEOUT = "timeout_seconds"

    private val _config = MutableStateFlow(BackendConfig())
    val configFlow: StateFlow<BackendConfig> = _config.asStateFlow()
    val config: BackendConfig get() = _config.value

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _config.value = BackendConfig(
            host = prefs.getString(KEY_HOST, null) ?: BackendConfig.DEFAULT_HOST,
            port = prefs.getInt(KEY_PORT, BackendConfig.DEFAULT_PORT),
            token = prefs.getString(KEY_TOKEN, null).orEmpty(),
            timeoutSeconds = prefs.getInt(KEY_TIMEOUT, BackendConfig.DEFAULT_TIMEOUT_SECONDS)
        )
    }

    fun update(context: Context, config: BackendConfig) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HOST, config.host)
            .putInt(KEY_PORT, config.port)
            .putString(KEY_TOKEN, config.token)
            .putInt(KEY_TIMEOUT, config.timeoutSeconds)
            .apply()
        _config.value = config
    }
}
