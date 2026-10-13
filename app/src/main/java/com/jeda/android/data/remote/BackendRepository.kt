package com.jeda.android.data.remote

import com.jeda.android.monitoring.behavior.BehavioralSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface BackendStatus {
    data object Unknown : BackendStatus
    data object Connecting : BackendStatus
    data class Online(val service: String, val version: String) : BackendStatus
    data class Offline(val reason: String) : BackendStatus
}

/**
 * Single entry point for backend calls. Nothing above this layer knows about
 * Retrofit, OkHttp, or how the host is configured.
 *
 * Per the reliability requirements, failures are returned as values rather than
 * thrown. Monitoring never depends on the backend being reachable.
 */
object BackendRepository {
    private val _status = MutableStateFlow<BackendStatus>(BackendStatus.Unknown)
    val statusFlow: StateFlow<BackendStatus> = _status.asStateFlow()
    val status: BackendStatus get() = _status.value

    suspend fun checkHealth(config: BackendConfig): BackendStatus {
        if (!config.isUsable) {
            _status.value = BackendStatus.Offline("Backend address is incomplete")
            return _status.value
        }

        _status.value = BackendStatus.Connecting
        val result = withContext(Dispatchers.IO) {
            runCatching { BackendClient.api(config).health() }
        }

        _status.value = result.fold(
            onSuccess = { BackendStatus.Online(it.service, it.version) },
            onFailure = { BackendStatus.Offline(it.readableReason()) }
        )
        return _status.value
    }

    suspend fun analyzeBehavior(
        snapshot: BehavioralSnapshot,
        config: BackendConfig
    ): Result<BehaviorAnalysisResponse> = withContext(Dispatchers.IO) {
        if (!config.isUsable) {
            return@withContext Result.failure(
                IllegalStateException("Backend address is incomplete")
            )
        }
        runCatching { BackendClient.api(config).analyzeBehavior(snapshot) }
    }

    private fun Throwable.readableReason(): String = when (this) {
        is UnknownHostException -> "Cannot resolve the backend hostname"
        is ConnectException -> "Backend refused the connection"
        is SocketTimeoutException, is InterruptedIOException -> "Backend timed out"
        else -> message?.takeIf { it.isNotBlank() } ?: "Unknown transport error"
    }
}
