package com.lunacattus.network.http

import com.lunacattus.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
class HttpManager : IHttpClient {

    @Volatile
    private var config: HttpConfig = HttpConfig()

    private val client by lazy {
        buildClient()
    }

    fun updateConfig(newConfig: HttpConfig) {
        config = newConfig
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    override suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String>,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = body.toRequestBody(JSON_MEDIA_TYPE)
            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)
            headers.forEach { (key, value) -> requestBuilder.addHeader(key, value) }
            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body.string()
            if (response.isSuccessful) {
                Result.success(responseBody)
            } else {
                Result.failure(
                    HttpException(response.code, responseBody)
                )
            }
        } catch (e: Exception) {
            Logger.e(TAG, "HTTP POST failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun get(
        url: String,
        headers: Map<String, String>,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().url(url).get()
            headers.forEach { (key, value) -> requestBuilder.addHeader(key, value) }
            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body.string()
            if (response.isSuccessful) {
                Result.success(responseBody)
            } else {
                Result.failure(
                    HttpException(response.code, responseBody)
                )
            }
        } catch (e: Exception) {
            Logger.e(TAG, "HTTP GET failed: ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "HttpManager"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

class HttpException(val code: Int, message: String) : Exception("HTTP $code: $message")
