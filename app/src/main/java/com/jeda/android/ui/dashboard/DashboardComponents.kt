package com.jeda.android.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeda.android.data.remote.BackendStatus
import com.jeda.android.monitoring.MonitoringState

@Composable
fun HeroFocusCard(
    isProtected: Boolean,
    isVisualActive: Boolean,
    onToggleShield: (Boolean) -> Unit,
    onToggleVisual: () -> Unit,
    state: MonitoringState,
    foregroundApp: String?,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isProtected) Color(0xFF10B981) else Color(0xFF64748B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isProtected) Color(0x3310B981) else Color(0x22FFFFFF), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isProtected) Color(0xFF0F172A) else Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProtected) "SHIELD ACTIVE" else "SHIELD PAUSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = accentColor
                    )
                }

                Switch(
                    checked = isProtected,
                    onCheckedChange = onToggleShield,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155)
                    )
                )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                text = if (isProtected) "Active Habit Shield" else "Protection Inactive",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                val subtitle = when {
                !isProtected -> "One tap to guard against doomscrolling and adult relapse triggers."
                state == MonitoringState.MONITORING && foregroundApp != null -> "Shielding habits inside $foregroundApp"
                state == MonitoringState.INTERVENTION -> "Intervention active. Breathe and take a pause."
                state == MonitoringState.COOLDOWN -> "In cooldown recovery period."
                else -> "Silently active in the background."
                }

                Text(
                text = subtitle,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .clickable { onToggleVisual() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
                ) {
                Column {
                    Text(
                        text = "Visual AI Guard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = if (isVisualActive) "Live on-device sampling" else "Tap to enable screen sampling",
                        fontSize = 12.sp,
                        color = if (isVisualActive) Color(0xFF10B981) else Color(0xFF64748B)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isVisualActive) Color(0x3310B981) else Color(0x22FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isVisualActive) "ENABLED" else "ENABLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isVisualActive) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }
                }
                }
                }
                }

@Composable
fun WellbeingMetersCard(
    scrollCpm: Int,
    sessionSec: Long,
    softCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Today's Focus Habits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scroll Velocity
            val scrollRatio = (scrollCpm / 60f).coerceIn(0f, 1f)
            val scrollColor = when {
                scrollCpm > 45 -> Color(0xFFEF4444)
                scrollCpm > 25 -> Color(0xFFF59E0B)
                else -> Color(0xFF10B981)
            }
            MetricBar(
                title = "Scroll Tempo",
                value = "$scrollCpm events/min",
                progress = scrollRatio,
                color = scrollColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Continuous session
            val sessionMin = sessionSec / 60
            val sessionRatio = (sessionSec / 600f).coerceIn(0f, 1f)
            MetricBar(
                title = "Active Session",
                value = "${sessionMin}m ${sessionSec % 60}s",
                progress = sessionRatio,
                color = if (sessionSec > 300) Color(0xFFF59E0B) else Color(0xFF3B82F6)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mindful Pauses Taken
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mindful Pauses (24h)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$softCount taken",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MetricBar(
    title: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun StatusPill(
    backendStatus: BackendStatus,
    accessibilityOk: Boolean,
    overlayOk: Boolean,
    modifier: Modifier = Modifier
) {
    val isReady = accessibilityOk && overlayOk && backendStatus is BackendStatus.Online
    val statusText = when {
        !accessibilityOk -> "Accessibility Required"
        !overlayOk -> "Overlay Permission Required"
        backendStatus !is BackendStatus.Online -> "Connecting to Core..."
        else -> "All Systems Connected"
    }
    val statusColor = if (isReady) Color(0xFF10B981) else Color(0xFFF59E0B)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = statusColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
    }
}
