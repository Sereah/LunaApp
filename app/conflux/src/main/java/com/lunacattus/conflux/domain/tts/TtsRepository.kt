package com.lunacattus.conflux.domain.tts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsRepository @Inject constructor(
    private val httpService: TtsHttpService,
    private val wsService: TtsWebSocketService,
) : ITts {

    override val wsEvents: Flow<TtsWsEvent> get() = wsService.events
    override val wsConnectionState: StateFlow<Boolean> get() = wsService.connected

    override fun updateConfig(config: TtsConfig) {
        httpService.updateConfig(config)
        wsService.updateConfig(config)
    }

    override suspend fun synthesize(request: TtsHttpRequest): Result<TtsHttpResponse> {
        return httpService.synthesize(request)
    }

    override suspend fun healthCheck(): Result<HealthResponse> {
        return httpService.healthCheck()
    }

    override suspend fun connectWs() {
        wsService.connect()
    }

    override suspend fun disconnectWs() {
        wsService.disconnect()
    }

    override suspend fun sendWsText(request: TtsWsTextMessage): Boolean {
        return wsService.sendTextMessage(request)
    }

    override suspend fun cancelWs(requestId: String) {
        wsService.cancel(requestId)
    }

    override suspend fun pingWs() {
        wsService.ping()
    }
}
