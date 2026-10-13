package com.jeda.android.monitoring.visual

import android.content.Context
import android.util.Log
import android.content.Intent
import com.jeda.android.monitoring.MonitoringManager
import com.jeda.android.monitoring.TargetAppPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val _hardInterventionState = MutableStateFlow(com.jeda.android.monitoring.intervention.HardInterventionState())
    val hardInterventionState: StateFlow<com.jeda.android.monitoring.intervention.HardInterventionState> = _hardInterventionState.asStateFlow()

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.SupervisorJob())

    private var rrsExposureStartMillis: Long? = null

    fun onFrameCaptured(jpegBytes: ByteArray) {
        _lastFrameTimestamp.value = System.currentTimeMillis()
        _frameCount.value += 1

        val now = System.currentTimeMillis()
        if (rrsExposureStartMillis == null) {
            rrsExposureStartMillis = now
        }
        val exposureSec = ((now - rrsExposureStartMillis!!) / 1000L).coerceAtLeast(10L).toFloat()

        val currentSessionId = MonitoringManager.aggregator.snapshot().sessionId
        val currentPackage = MonitoringManager.controller.foregroundPackageFlow.value ?: ""
        val softCount = MonitoringManager.interventions.softCount()
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        scope.launch {
            com.jeda.android.data.remote.BackendRepository.analyzeFrame(
                jpegBytes = jpegBytes,
                sessionId = currentSessionId,
                exposureSeconds = exposureSec,
                softInterventionCount = softCount,
                hourOfDay = hour,
                config = com.jeda.android.data.remote.BackendSettings.config
            ).onSuccess { rrsResponse ->
                Log.d("JEDA_VISUAL", "RRS response: level=${rrsResponse.level}, rrs=${rrsResponse.rrs}, action=${rrsResponse.action}")
                setElevatedSampling(rrsResponse.level != "NORMAL")
                if (rrsResponse.requestsHardIntervention) {
                    val cognitiveMsg = com.jeda.android.data.remote.BackendRepository.getCognitiveMessage(
                        riskLevel = rrsResponse.level,
                        config = com.jeda.android.data.remote.BackendSettings.config
                    )
                    val durationSec = rrsResponse.durationSeconds
                    val nowMs = System.currentTimeMillis()
                    _hardInterventionState.value = com.jeda.android.monitoring.intervention.HardInterventionState(
                        active = true,
                        remainingSeconds = durationSec,
                        rrs = rrsResponse.rrs,
                        reason = cognitiveMsg,
                        endsAtMillis = nowMs + durationSec * 1000L
                    )
                    com.jeda.android.monitoring.JedaAccessibilityService.sendHome()
                    MonitoringManager.triggerOverlaySync()
                }
            }.onFailure { err ->
                Log.e("JEDA_VISUAL", "analyzeFrame network failed", err)
            }
        }
    }

    fun onSecondElapsed() {
        val current = _hardInterventionState.value
        if (!current.active) return
        val remaining = ((current.endsAtMillis - System.currentTimeMillis()) / 1000L).toInt()
        if (remaining <= 0) {
            _hardInterventionState.value = com.jeda.android.monitoring.intervention.HardInterventionState()
        } else {
            _hardInterventionState.value = current.copy(remainingSeconds = remaining)
        }
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
        rrsExposureStartMillis = null
        val intent = Intent(context, MediaProjectionService::class.java).apply {
            action = MediaProjectionService.ACTION_STOP
        }
        context.startService(intent)
        _isServiceRunning.value = false
        _frameCount.value = 0
    }
}
