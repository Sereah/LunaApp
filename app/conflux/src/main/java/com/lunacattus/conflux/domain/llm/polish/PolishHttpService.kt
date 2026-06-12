package com.lunacattus.conflux.domain.llm.polish

import com.google.gson.Gson
import com.lunacattus.network.http.IHttpClient
import com.lunacattus.network.id.RequestIdGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PolishHttpService @Inject constructor(
    private val httpClient: IHttpClient,
    private val idGenerator: RequestIdGenerator,
) {
    private val gson = Gson()
    private var config: PolishConfig = PolishConfig()

    fun updateConfig(newConfig: PolishConfig) {
        config = newConfig
    }

    suspend fun polish(
        txt: String,
        temperature: Double,
    ): Result<PolishResponse> {
        val request = PolishRequest(
            requestId = idGenerator.generate(),
            txt = txt,
            temperature = temperature,
        )
        return polish(request)
    }

    suspend fun polish(request: PolishRequest): Result<PolishResponse> {
        val json = gson.toJson(request)
        val result = httpClient.post(
            url = "${config.httpBaseUrl}/polish",
            body = json,
        ).map { responseBody ->
            gson.fromJson(responseBody, PolishResponse::class.java)
        }
        return result
    }

}
