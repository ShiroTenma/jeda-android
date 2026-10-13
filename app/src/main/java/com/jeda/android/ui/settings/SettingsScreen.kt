package com.jeda.android.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeda.android.data.remote.BackendConfig
import com.jeda.android.data.remote.BackendStatus

@Composable
fun SettingsScreen(
    accessibilityOk: Boolean,
    overlayOk: Boolean,
    backendConfig: BackendConfig,
    backendStatus: BackendStatus,
    onOpenAccessibility: () -> Unit,
    onOpenOverlay: () -> Unit,
    onSaveConfig: (BackendConfig) -> Unit,
    onTestBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Settings & Core",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "System permissions, neural core, and security audit",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // System Permissions Checklist
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "System Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                PermissionRow(
                    title = "Accessibility Service",
                    subtitle = "Required for scrolling tempo & app detection",
                    isGranted = accessibilityOk,
                    onGrant = onOpenAccessibility
                )

                Spacer(modifier = Modifier.height(12.dp))

                PermissionRow(
                    title = "Display Over Other Apps",
                    subtitle = "Required for blocking overlays and pauses",
                    isGranted = overlayOk,
                    onGrant = onOpenOverlay
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Backend Connection Card
        BackendConnectionCard(
            config = backendConfig,
            status = backendStatus,
            canDrawOverlays = overlayOk,
            onSave = onSaveConfig,
            onTest = onTestBackend,
            onRequestOverlayPermission = onOpenOverlay
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy Guarantee Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x1A10B981), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Privacy Guarantee",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Zero image storage: frames processed in RAM and discarded immediately.\n" +
                           "• No cloud telemetry: all scoring is performed locally or on your private Tailscale node.\n" +
                           "• Voluntary usage: you can stop protection anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x3310B981))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("READY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
        } else {
            Button(
                onClick = onGrant,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("GRANT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
