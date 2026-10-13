package com.jeda.android.data.remote

import com.google.gson.annotations.SerializedName
import com.jeda.android.monitoring.behavior.BehavioralSnapshot
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface JedaApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("api/analyze-behavior")
    suspend fun analyzeBehavior(@Body snapshot: BehavioralSnapshot): BehaviorAnalysisResponse

    @Multipart
    @POST("api/analyze-frame")
    suspend fun analyzeFrame(
        @Part file: MultipartBody.Part,
        @Part("session_id") sessionId: RequestBody,
        @Part("exposure_seconds") exposureSeconds: RequestBody,
        @Part("soft_intervention_count") softInterventionCount: RequestBody,
        @Part("hour_of_day") hourOfDay: RequestBody
    ): RrsAnalysisResponse

    @POST("api/cognitive-message")
    suspend fun getCognitiveMessage(
        @Body prompt: CognitivePromptRequest
    ): CognitiveMessageResponse
}

data class CognitivePromptRequest(
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("trigger") val trigger: String = "sustained_visual_risk",
    @SerializedName("intervention_style") val interventionStyle: String = "reflective",
    @SerializedName("max_sentences") val maxSentences: Int = 1
)

data class CognitiveMessageResponse(
    @SerializedName("message") val message: String,
    @SerializedName("source") val source: String
)

data class RrsAnalysisResponse(
    @SerializedName("visual_confidence") val visualConfidence: Double,
    @SerializedName("p_avg") val pAvg: Double,
    @SerializedName("dn") val dn: Double,
    @SerializedName("ct") val ct: Double,
    @SerializedName("rrs") val rrs: Double,
    @SerializedName("level") val level: String,
    @SerializedName("action") val action: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("sampling_interval_seconds") val samplingIntervalSeconds: Int
) {
    val requestsHardIntervention: Boolean get() = action == "HARD_INTERVENTION"
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
