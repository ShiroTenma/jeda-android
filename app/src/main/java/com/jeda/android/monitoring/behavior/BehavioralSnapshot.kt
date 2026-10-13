package com.jeda.android.monitoring.behavior

import com.google.gson.annotations.SerializedName

/**
 * Wire contract for `POST /api/analyze-behavior`.
 *
 * Carries raw counters only. Normalization and the FDS formula belong to the
 * backend; the client is not allowed to pre-apply them, otherwise a config change
 * would silently invalidate historical comparisons.
 *
 * [softInterventionCount24h] is reserved for M3, which owns the rolling escalation
 * counter. It is part of the contract now so the shape does not shift later.
 */
data class BehavioralSnapshot(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("request_id") val requestId: String,
    @SerializedName("observation_window_seconds") val observationWindowSeconds: Long,
    @SerializedName("scroll_events") val scrollEvents: Int,
    @SerializedName("social_app_switches") val socialAppSwitches: Int,
    @SerializedName("active_social_session_seconds") val activeSocialSessionSeconds: Long,
    @SerializedName("idle_seconds") val idleSeconds: Long,
    @SerializedName("foreground_package") val foregroundPackage: String?,
    @SerializedName("soft_intervention_count_24h") val softInterventionCount24h: Int = 0
)
