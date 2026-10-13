package com.jeda.android.ui.visual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VisualSamplingCard(
    isServiceRunning: Boolean,
    frameCount: Int,
    lastTimestamp: Long,
    onStartRequested: () -> Unit,
    onStopRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Visual Monitoring (M4)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isServiceRunning) "Status: Sampling Active" else "Status: Inactive",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Captured Frames (In-Memory): $frameCount",
                style = MaterialTheme.typography.bodySmall
            )
            if (lastTimestamp > 0L) {
                Text(
                    text = "Last Capture: ${(System.currentTimeMillis() - lastTimestamp) / 1000}s ago",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isServiceRunning) {
                    Button(
                        onClick = onStartRequested,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enable Visual Sampling")
                    }
                } else {
                    Button(
                        onClick = onStopRequested,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop Visual Sampling")
                    }
                }
            }
        }
    }
}
