package com.jeda.android.ui.intervention

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.jeda.android.monitoring.intervention.SoftInterventionState
import com.jeda.android.ui.theme.JEDATheme
import kotlinx.coroutines.flow.StateFlow

/**
 * Injects the intervention UI above the target app via WindowManager, so the pause
 * appears without an Activity launch (SRS section 4.3).
 *
 * Show is idempotent, and every WindowManager call is guarded: a failed overlay
 * must be recorded as a test failure, never crash the app.
 */
object InterventionOverlay {
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null

    fun canDraw(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    fun show(context: Context, state: StateFlow<SoftInterventionState>) {
        if (composeView != null) return
        if (!canDraw(context)) return

        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = ComposeView(context).apply {
            setContent {
                JEDATheme {
                    val current by state.collectAsState()
                    if (current.active) {
                        SoftInterventionScreen(current)
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )

        runCatching {
            manager.addView(view, params)
            windowManager = manager
            composeView = view
        }
    }

    fun hide() {
        val view = composeView ?: return
        runCatching { windowManager?.removeViewImmediate(view) }
        composeView = null
        windowManager = null
    }
}
