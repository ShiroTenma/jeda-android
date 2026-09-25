package com.jeda.android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jeda.android.monitoring.MonitoringManager
import com.jeda.android.monitoring.MonitoringState
import com.jeda.android.ui.theme.JEDATheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MonitoringManager.startNotificationSync(applicationContext)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            JEDATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 2001
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val controller = MonitoringManager.controller
    val monitoringState by controller.stateFlow.collectAsState()
    val monitoringEnabled by controller.enabledFlow.collectAsState()

    var backendStatus by remember { mutableStateOf("Not tested") }
    var backendLoading by remember { mutableStateOf(false) }
    var accessibilityEnabled by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityEnabled = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("JEDA", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "M1 - Monitoring Foundation",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Backend Connection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        backendStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (backendStatus.startsWith("Connected")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            backendLoading = true
                            scope.launch {
                                backendStatus = testBackendConnection()
                                backendLoading = false
                            }
                        },
                        enabled = !backendLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (backendLoading) "Testing..." else "Test Backend Connection")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Accessibility Service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusRow("Status:", if (accessibilityEnabled) "Enabled" else "Disabled", accessibilityEnabled)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { openAccessibilitySettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Accessibility Settings")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monitoring Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Monitoring", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = monitoringEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && !accessibilityEnabled) {
                                    openAccessibilitySettings(context)
                                } else if (enabled) {
                                    controller.start()
                                } else {
                                    MonitoringManager.stopMonitoring(context)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusRow("Current State:", monitoringState.name, monitoringState == MonitoringState.MONITORING)
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, positive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

suspend fun testBackendConnection(): String {
    return try {
        val response = OkHttpClient().newCall(
            Request.Builder().url("http://jeda-backend:8000/health").build()
        ).execute()
        response.use {
            if (it.isSuccessful) "Connected\n${it.body?.string().orEmpty()}" else "HTTP ${it.code}"
        }
    } catch (error: Exception) {
        "Connection failed: ${error.message ?: "unknown error"}"
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.split(':').any { it.startsWith(context.packageName + "/") }
}

fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}
