package com.jeda.android.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
    onToggle: (Boolean) -> Unit,
    state: MonitoringState,
    foregroundApp: String?,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        if (isProtected) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant,
        label = "hero_bg"
    )
    val accentColor = if (isProtected) Color(0xFF10B981) else Color(0xFF64748B)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isProtected) "SHIELD ACTIVE" else "SHIELD OFF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = accentColor
                    )
                }

                Switch(
                    checked = isProtected,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFFE2E8F0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isProtected) "Mindful Focus" else "Protection Paused",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isProtected) Color.White else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            val subtitle = when {
                !isProtected -> "Tap switch to guard against doomscrolling and adult triggers."
                state == MonitoringState.MONITORING && foregroundApp != null -> "Watching over your habits in $foregroundApp"
                state == MonitoringState.INTERVENTION -> "Intervention in progress. Take a pause."
                state == MonitoringState.COOLDOWN -> "In cooldown recovery mode."
                else -> "Quietly active in the background."
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isProtected) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
            )
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
