package com.lunacattus.conflux.domain.tts

import com.google.gson.annotations.SerializedName

data class TtsHttpRequest(
    @SerializedName("request_id") val requestId: String,
    val text: String,
    val speaker: String,
    val language: String,
    val instruct: String? = null,
)

data class TtsHttpResponse(
    @SerializedName("request_id") val requestId: String,
    @SerializedName("total_chunks") val totalChunks: Int,
    val chunks: List<TtsChunk>,
)

data class TtsChunk(
    val index: Int,
    val text: String,
    @SerializedName("audio_b64") val audioB64: String,
    @SerializedName("duration_ms") val durationMs: Long,
)

data class HealthResponse(
    val status: String,
    @SerializedName("model_loaded") val modelLoaded: Boolean,
    @SerializedName("active_sessions") val activeSessions: Int,
)

data class TtsWsTextMessage(
    val type: String = "text",
    @SerializedName("request_id") val requestId: String,
    val content: String,
    val speaker: String,
    val language: String,
    val instruct: String? = null,
)

data class TtsWsCancelMessage(
    val type: String = "cancel",
    @SerializedName("request_id") val requestId: String,
)

data class TtsWsPingMessage(
    val type: String = "ping",
)

sealed interface TtsWsEvent {
    data class SessionId(val sessionId: String) : TtsWsEvent
    data class ChunkStart(
        val requestId: String,
        val chunkIndex: Int,
        val total: Int,
        val text: String,
    ) : TtsWsEvent
    data class Audio(
        val requestId: String,
        val chunkIndex: Int,
        val audioB64: String,
        val durationMs: Long,
        val isLast: Boolean,
    ) : TtsWsEvent
    data class Done(val requestId: String, val totalChunks: Int) : TtsWsEvent
    data class Error(val requestId: String, val message: String) : TtsWsEvent
    data object Pong : TtsWsEvent
}
