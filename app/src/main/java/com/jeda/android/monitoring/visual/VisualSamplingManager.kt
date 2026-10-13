package com.jeda.android.monitoring.visual

import android.content.Context
import android.content.Intent
import com.jeda.android.monitoring.MonitoringManager
import com.jeda.android.monitoring.TargetAppPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

object VisualSamplingManager {

    val isCapturing = AtomicBoolean(false)
    val isEnabled = AtomicBoolean(false)

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _lastFrameTimestamp = MutableStateFlow(0L)
    val lastFrameTimestamp: StateFlow<Long> = _lastFrameTimestamp.asStateFlow()

    private val _frameCount = MutableStateFlow(0)
    val frameCount: StateFlow<Int> = _frameCount.asStateFlow()

    var currentIntervalMillis: Long = 3_000L

    fun setElevatedSampling(elevated: Boolean) {
        currentIntervalMillis = if (elevated) 2_000L else 3_000L
    }

    fun shouldSample(): Boolean {
        if (!isEnabled.get()) return false
        val currentPackage = MonitoringManager.controller.foregroundPackageFlow.value ?: return false
        return TargetAppPolicy.isRrsScope(currentPackage)
    }

    fun onFrameCaptured(jpegBytes: ByteArray) {
        _lastFrameTimestamp.value = System.currentTimeMillis()
        _frameCount.value += 1
        // Forwarding to M5/M6 visual inference pipeline goes here (in-memory only).
    }

    fun startService(context: Context, resultCode: Int, data: Intent) {
        isEnabled.set(true)
        val intent = Intent(context, MediaProjectionService::class.java).apply {
            action = MediaProjectionService.ACTION_START
            putExtra(MediaProjectionService.EXTRA_RESULT_CODE, resultCode)
            putExtra(MediaProjectionService.EXTRA_RESULT_DATA, data)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _isServiceRunning.value = true
    }

    fun stopService(context: Context) {
        isEnabled.set(false)
        val intent = Intent(context, MediaProjectionService::class.java).apply {
            action = MediaProjectionService.ACTION_STOP
        }
        context.startService(intent)
        _isServiceRunning.value = false
        _frameCount.value = 0
    }
}
