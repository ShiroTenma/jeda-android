package com.jeda.android.ui.intervention

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeda.android.monitoring.intervention.SoftInterventionState
import java.util.Locale

/**
 * UI-03 soft intervention. A pause, not a punishment: muted palette, no red, no
 * sirens, no shaming language. Copy follows the harm-reduction principle in the
 * privacy and ethics document.
 */
@Composable
fun SoftInterventionScreen(state: SoftInterventionState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "JEDA",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceMuted,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = state.remainingSeconds.toString(),
                fontSize = 112.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                color = OnSurface
            )

            Text(
                text = if (state.remainingSeconds == 1) "second remaining" else "seconds remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text = messageFor(state.level),
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = String.format(Locale.US, "FDS %.1f · %s", state.fds, state.level),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted
            )
        }
    }
}

private fun messageFor(level: String): String = when (level) {
    "HIGH" -> "Take the pause. You do not have to keep going right now."
    else -> "Slow down for a moment. This one is getting intense."
}

private val Surface = Color(0xFF1B1B1B)
private val OnSurface = Color(0xFFE4E1DC)
private val OnSurfaceMuted = Color(0xFF8A8781)
