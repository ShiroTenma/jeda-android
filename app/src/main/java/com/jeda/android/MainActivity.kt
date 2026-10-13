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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.jeda.android.monitoring.visual.VisualSamplingManager
import com.jeda.android.ui.visual.VisualSamplingCard
import com.jeda.android.data.remote.BackendRepository
import com.jeda.android.data.remote.BackendSettings
import com.jeda.android.monitoring.MonitoringManager
import com.jeda.android.monitoring.MonitoringState
import com.jeda.android.ui.dashboard.HeroFocusCard
import com.jeda.android.ui.dashboard.StatusPill
import com.jeda.android.ui.dashboard.WellbeingMetersCard
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.jeda.android.ui.dashboard.ShieldScreen
import com.jeda.android.ui.navigation.Screen
import com.jeda.android.ui.rules.RulesScreen
import com.jeda.android.ui.settings.SettingsScreen
import com.jeda.android.ui.theme.JEDATheme
import kotlinx.coroutines.launch

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
    val behaviorMetrics by MonitoringManager.aggregator.metricsFlow.collectAsState()

    var accessibilityEnabled by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }
    var canDrawOverlays by remember { mutableStateOf(canDrawOverlays(context)) }
    val backendConfig by BackendSettings.configFlow.collectAsState()
    val backendStatus by BackendRepository.statusFlow.collectAsState()
    val isVisualServiceRunning by VisualSamplingManager.isServiceRunning.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Shield) }

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            VisualSamplingManager.startService(context, result.resultCode, result.data!!)
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityEnabled = isAccessibilityServiceEnabled(context)
                canDrawOverlays = canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A)
            ) {
                listOf(Screen.Shield, Screen.Rules, Screen.Settings).forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            indicatorColor = Color(0x3310B981),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.Shield -> ShieldScreen(
                context = context,
                controller = controller,
                monitoringEnabled = monitoringEnabled,
                monitoringState = monitoringState,
                foregroundApp = controller.foregroundPackageFlow.collectAsState().value,
                isVisualServiceRunning = isVisualServiceRunning,
                behaviorMetrics = behaviorMetrics,
                backendStatus = backendStatus,
                accessibilityEnabled = accessibilityEnabled,
                canDrawOverlays = canDrawOverlays,
                mediaProjectionLauncher = mediaProjectionLauncher,
                onOpenAccessibility = { openAccessibilitySettings(context) },
                onOpenOverlay = { openOverlaySettings(context) },
                modifier = Modifier.padding(innerPadding)
            )
            Screen.Rules -> RulesScreen(
                modifier = Modifier.padding(innerPadding)
            )
            Screen.Settings -> SettingsScreen(
                accessibilityOk = accessibilityEnabled,
                overlayOk = canDrawOverlays,
                backendConfig = backendConfig,
                backendStatus = backendStatus,
                onOpenAccessibility = { openAccessibilitySettings(context) },
                onOpenOverlay = { openOverlaySettings(context) },
                onSaveConfig = { BackendSettings.update(context, it) },
                onTestBackend = {
                    scope.launch {
                        BackendRepository.checkHealth(BackendSettings.config)
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
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

fun canDrawOverlays(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

fun openOverlaySettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
    )
}
