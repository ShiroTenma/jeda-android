package com.jeda.android.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the Retrofit client, rebuilding only when the configuration changes.
 *
 * The logging interceptor is deliberately not wired in. At BODY level it would
 * print behavioral snapshots to logcat, which the privacy requirements forbid.
 */
object BackendClient {
    private var cachedConfig: BackendConfig? = null
    private var cachedApi: JedaApi? = null

    @Synchronized
    fun api(config: BackendConfig): JedaApi {
        cachedApi?.takeIf { cachedConfig == config }?.let { return it }

        val client = OkHttpClient.Builder()
            .connectTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor { chain ->
                val request = if (config.token.isBlank()) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${config.token}")
                        .build()
                }
                chain.proceed(request)
            }
            .build()

        val api = Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JedaApi::class.java)

        cachedConfig = config
        cachedApi = api
        return api
    }
}
