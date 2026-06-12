package com.lunacattus.conflux.domain.llm.tts

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lunacattus.logger.Logger
import com.lunacattus.network.id.RequestIdGenerator
import com.lunacattus.network.ws.IWebSocketClient
import com.lunacattus.network.ws.WebSocketConfig
import com.lunacattus.network.ws.WebSocketEvent
import com.lunacattus.network.ws.WebSocketState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsWebSocketService @Inject constructor(
    private val wsClient: IWebSocketClient,
    private val idGenerator: RequestIdGenerator,
) {
    private val gson = Gson()
    private var config: TtsConfig = TtsConfig()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<TtsWsEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )
    val events: SharedFlow<TtsWsEvent> = _events.asSharedFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    init {
        scope.launch {
            wsClient.events.collect { event ->
                handleWsEvent(event)
            }
        }
        scope.launch {
            wsClient.state.collect { state ->
                _connected.value = state is WebSocketState.Connected
            }
        }
    }

    fun updateConfig(newConfig: TtsConfig) {
        config = newConfig
    }

    suspend fun connect() {
        if (_connected.value) return
        wsClient.connect(
            url = config.wsUrl,
            config = WebSocketConfig(
                reconnectEnabled = true,
                pingIntervalMs = 15_000L,
            ),
        )
    }

    suspend fun disconnect() {
        wsClient.disconnect()
    }

    suspend fun sendText(
        content: String,
        speaker: String,
        language: String,
        instruct: String? = null,
    ): String? {
        val requestId = idGenerator.generate()
        val message = TtsWsTextMessage(
            requestId = requestId,
            content = content,
            speaker = speaker,
            language = language,
            instruct = instruct,
        )
        val ok = sendTextMessage(message)
        return if (ok) requestId else null
    }

    suspend fun sendTextMessage(message: TtsWsTextMessage): Boolean {
        val json = gson.toJson(message)
        val ok = wsClient.send(json)
        return ok
    }

    suspend fun cancel(requestId: String) {
        val message = TtsWsCancelMessage(requestId = requestId)
        wsClient.send(gson.toJson(message))
    }

    suspend fun ping() {
        val message = TtsWsPingMessage()
        wsClient.send(gson.toJson(message))
    }

    private suspend fun handleWsEvent(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.MessageReceived -> {
                val text = when (val msg = event.message) {
                    is WebSocketEvent.Message.Text -> msg.data
                    is WebSocketEvent.Message.Binary -> return
                }
                parseAndEmit(text)
            }
            is WebSocketEvent.Error -> {
                Logger.e(TAG, "WebSocket error: ${event.throwable.message}")
            }
            else -> { /* connected/disconnected handled via state flow */ }
        }
    }

    private suspend fun parseAndEmit(text: String) {
        try {
            val json = gson.fromJson(text, JsonObject::class.java)
            val type = json.get("type")?.asString ?: return

            val event = when (type) {
                "session_id" -> {
                    val sessionId = json.get("session_id")?.asString ?: return
                    TtsWsEvent.SessionId(sessionId)
                }
                "chunk_start" -> {
                    TtsWsEvent.ChunkStart(
                        requestId = json.get("request_id")?.asString ?: return,
                        chunkIndex = json.get("chunk_index")?.asInt ?: return,
                        total = json.get("total")?.asInt ?: return,
                        text = json.get("text")?.asString ?: return,
                    )
                }
                "audio" -> {
                    TtsWsEvent.Audio(
                        requestId = json.get("request_id")?.asString ?: return,
                        chunkIndex = json.get("chunk_index")?.asInt ?: return,
                        audioB64 = json.get("audio_b64")?.asString ?: return,
                        durationMs = json.get("duration_ms")?.asLong ?: return,
                        isLast = json.get("is_last")?.asBoolean ?: return,
                    )
                }
                "done" -> {
                    TtsWsEvent.Done(
                        requestId = json.get("request_id")?.asString ?: return,
                        totalChunks = json.get("total_chunks")?.asInt ?: return,
                    )
                }
                "error" -> {
                    TtsWsEvent.Error(
                        requestId = json.get("request_id")?.asString ?: return,
                        message = json.get("message")?.asString ?: return,
                    )
                }
                "pong" -> TtsWsEvent.Pong
                else -> {
                    Logger.e(TAG, "Unknown WS event type: $type")
                    return
                }
            }
            _events.emit(event)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse WS message: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "TtsWebSocketService"
    }
}
