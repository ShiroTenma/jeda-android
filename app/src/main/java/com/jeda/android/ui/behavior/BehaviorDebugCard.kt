package com.jeda.android.ui.behavior

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeda.android.monitoring.TargetAppPolicy
import com.jeda.android.monitoring.behavior.BehavioralMetrics
import java.util.Locale

/**
 * M2 acceptance surface: live aggregated behavioural metrics. Reads from the
 * aggregator's in-memory state only, so it cannot leak anything to storage.
 */
@Composable
fun BehaviorDebugCard(
    metrics: BehavioralMetrics,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Behavioral Metrics (M2)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                scopeLabel(metrics.foregroundPackage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(12.dp))

            MetricRow("Scroll events", "${metrics.scrollEvents}", "Vn ${fmt(metrics.normalizedScroll)}")
            MetricRow("App switches", "${metrics.socialAppSwitches}", "Sn ${fmt(metrics.normalizedSwitches)}")
            MetricRow("Session", "${metrics.activeSocialSessionSeconds}s", "Tn ${fmt(metrics.normalizedSession)}")
            MetricRow("Idle", "${metrics.idleSeconds}s", "In ${fmt(metrics.normalizedIdle)}")
            MetricRow("Window elapsed", "${metrics.windowElapsedSeconds}s", null)
        }
    }
}

@Composable
private fun MetricRow(label: String, raw: String, normalized: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            if (normalized == null) raw else "$raw   $normalized",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun scopeLabel(packageName: String?): String = when {
    packageName == null -> "No FDS-scope app in foreground"
    TargetAppPolicy.isFdsScope(packageName) -> "Behavioral collection active: $packageName"
    TargetAppPolicy.isRrsScope(packageName) -> "Visual scope only, no behavioral data: $packageName"
    else -> "Not monitored: $packageName"
}

private fun fmt(value: Double): String = String.format(Locale.US, "%.2f", value)
