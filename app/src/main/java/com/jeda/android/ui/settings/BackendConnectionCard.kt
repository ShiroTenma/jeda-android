package com.jeda.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.jeda.android.data.remote.BackendConfig
import com.jeda.android.data.remote.BackendStatus
import kotlinx.coroutines.launch

/**
 * Replaces the hardcoded backend URL that M1 shipped with. Host, port, token and
 * timeout are all editable, and the card shows live connectivity state.
 */
@Composable
fun BackendConnectionCard(
    config: BackendConfig,
    status: BackendStatus,
    canDrawOverlays: Boolean,
    onSave: (BackendConfig) -> Unit,
    onTest: suspend () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var host by remember(config) { mutableStateOf(config.host) }
    var port by remember(config) { mutableStateOf(config.port.toString()) }
    var token by remember(config) { mutableStateOf(config.token) }
    var testing by remember { mutableStateOf(false) }

    val portValue = port.toIntOrNull()

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Backend Connection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                statusText(status),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = if (status is BackendStatus.Online) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Backend host") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = port,
                onValueChange = { value -> port = value.filter(Char::isDigit).take(5) },
                label = { Text("Port") },
                singleLine = true,
                isError = portValue == null || portValue !in 1..65535,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Backend token") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSave(
                            config.copy(
                                host = host.trim(),
                                port = portValue ?: config.port,
                                token = token.trim()
                            )
                        )
                    },
                    enabled = portValue != null && portValue in 1..65535 && host.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = {
                        testing = true
                        scope.launch {
                            onTest()
                            testing = false
                        }
                    },
                    enabled = !testing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (testing) "Testing..." else "Test")
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Overlay permission", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (canDrawOverlays) "Granted" else "Missing",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!canDrawOverlays) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRequestOverlayPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant overlay permission")
                }
            }
        }
    }
}

private fun statusText(status: BackendStatus): String = when (status) {
    is BackendStatus.Unknown -> "Not tested"
    is BackendStatus.Connecting -> "Connecting..."
    is BackendStatus.Online -> "Connected: ${status.service} ${status.version}"
    is BackendStatus.Offline -> "Unavailable: ${status.reason}"
}
