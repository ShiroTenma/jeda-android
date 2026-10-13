package com.jeda.android.ui.dashboard

import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeda.android.data.remote.BackendStatus
import com.jeda.android.monitoring.MonitoringController
import com.jeda.android.monitoring.MonitoringManager
import com.jeda.android.monitoring.MonitoringState
import com.jeda.android.monitoring.behavior.BehavioralMetrics
import com.jeda.android.monitoring.visual.VisualSamplingManager

@Composable
fun ShieldScreen(
    context: Context,
    controller: MonitoringController,
    monitoringEnabled: Boolean,
    monitoringState: MonitoringState,
    foregroundApp: String?,
    isVisualServiceRunning: Boolean,
    behaviorMetrics: BehavioralMetrics,
    backendStatus: BackendStatus,
    accessibilityEnabled: Boolean,
    canDrawOverlays: Boolean,
    mediaProjectionLauncher: ManagedActivityResultLauncher<android.content.Intent, ActivityResult>,
    onOpenAccessibility: () -> Unit,
    onOpenOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("JEDA", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Mindful Focus & Protection", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            }
            StatusPill(
                backendStatus = backendStatus,
                accessibilityOk = accessibilityEnabled,
                overlayOk = canDrawOverlays
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        HeroFocusCard(
            isProtected = monitoringEnabled,
            isVisualActive = isVisualServiceRunning,
            state = monitoringState,
            foregroundApp = foregroundApp,
            onToggleShield = { enabled ->
                if (enabled) {
                    if (!accessibilityEnabled) {
                        onOpenAccessibility()
                    } else if (!canDrawOverlays) {
                        onOpenOverlay()
                    } else {
                        MonitoringManager.startMonitoring(context)
                    }
                } else {
                    MonitoringManager.stopMonitoring(context)
                }
            },
            onToggleVisual = {
                if (!isVisualServiceRunning) {
                    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjectionLauncher.launch(projectionManager.createScreenCaptureIntent())
                } else {
                    VisualSamplingManager.stopService(context)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        WellbeingMetersCard(
            scrollCpm = behaviorMetrics.scrollEvents * 2,
            sessionSec = behaviorMetrics.activeSocialSessionSeconds,
            softCount = MonitoringManager.interventions.softCount()
        )

        Spacer(modifier = Modifier.height(16.dp))

        MindfulAiCard(backendConfig = com.jeda.android.data.remote.BackendSettings.config)
    }
}
