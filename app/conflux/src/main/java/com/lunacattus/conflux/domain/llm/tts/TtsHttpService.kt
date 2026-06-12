package com.lunacattus.conflux.domain.llm.tts

import com.google.gson.Gson
import com.lunacattus.network.http.IHttpClient
import com.lunacattus.network.id.RequestIdGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsHttpService @Inject constructor(
    private val httpClient: IHttpClient,
    private val idGenerator: RequestIdGenerator,
) {
    private val gson = Gson()
    private var config: TtsConfig = TtsConfig()

    fun updateConfig(newConfig: TtsConfig) {
        config = newConfig
    }

    suspend fun synthesize(
        text: String,
        speaker: String,
        language: String,
        instruct: String? = null,
    ): Result<TtsHttpResponse> {
        val request = TtsHttpRequest(
            requestId = idGenerator.generate(),
            text = text,
            speaker = speaker,
            language = language,
            instruct = instruct,
        )
        return synthesize(request)
    }

    suspend fun synthesize(request: TtsHttpRequest): Result<TtsHttpResponse> {
        val json = gson.toJson(request)
        val result = httpClient.post(
            url = "${config.httpBaseUrl}/api/tts",
            body = json,
        ).map { responseBody ->
            gson.fromJson(responseBody, TtsHttpResponse::class.java)
        }
        return result
    }

    suspend fun healthCheck(): Result<HealthResponse> {
        return httpClient.get(
            url = "${config.httpBaseUrl}/api/health",
        ).map { responseBody ->
            gson.fromJson(responseBody, HealthResponse::class.java)
        }
    }
}
