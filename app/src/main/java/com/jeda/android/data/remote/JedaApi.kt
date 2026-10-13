package com.jeda.android.data.remote

import com.google.gson.annotations.SerializedName
import com.jeda.android.monitoring.behavior.BehavioralSnapshot
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface JedaApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("api/analyze-behavior")
    suspend fun analyzeBehavior(@Body snapshot: BehavioralSnapshot): BehaviorAnalysisResponse
}

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("service") val service: String,
    @SerializedName("version") val version: String
)

data class BehaviorAnalysisResponse(
    @SerializedName("fds") val fds: Double,
    @SerializedName("level") val level: String,
    @SerializedName("action") val action: String,
    @SerializedName("cooldown_seconds") val cooldownSeconds: Int
) {
    val requestsSoftIntervention: Boolean get() = action == ACTION_SOFT_INTERVENTION

    companion object {
        const val ACTION_NONE = "NONE"
        const val ACTION_SOFT_INTERVENTION = "SOFT_INTERVENTION"
    }
}
