package com.lunacattus.network.ws

import com.lunacattus.common.SafeCoroutine
import com.lunacattus.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

class WebSocketManager : IWebSocketClient {

    private var scope: CoroutineScope? = null
    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var currentUrl: String = ""
    private var currentHeaders: Map<String, String> = emptyMap()
    private var currentConfig: WebSocketConfig = WebSocketConfig()
    private var reconnectAttempt = 0
    private var intentionalClose = false

    private val _state = MutableStateFlow<WebSocketState>(WebSocketState.Idle)
    override val state: StateFlow<WebSocketState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WebSocketEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )
    override val events: SharedFlow<WebSocketEvent> = _events.asSharedFlow()

    override suspend fun connect(
        url: String,
        headers: Map<String, String>,
        config: WebSocketConfig,
    ): Boolean {
        if (_state.value == WebSocketState.Connecting || _state.value == WebSocketState.Connected) {
            Logger.d(TAG, "Already connected or connecting, skipping.")
            return false
        }

        intentionalClose = false
        reconnectAttempt = 0
        currentUrl = url
        currentHeaders = headers
        currentConfig = config

        val newScope = SafeCoroutine.createCoroutineScope(scopeName = "WebSocketManager")
        scope = newScope

        return createWebSocket()
    }

    override suspend fun disconnect() {
        intentionalClose = true
        cancelReconnect()
        webSocket?.close(NORMAL_CLOSURE_CODE, "Client closed")
        webSocket = null
        client?.dispatcher?.executorService?.shutdown()
        client = null
        scope?.cancel()
        scope = null
        _state.value = WebSocketState.Disconnected(NORMAL_CLOSURE_CODE, "Client closed")
    }

    override suspend fun send(message: String): Boolean {
        val ws = webSocket
        return if (ws != null && _state.value == WebSocketState.Connected) {
            ws.send(message)
        } else {
            Logger.e(TAG, "Send failed: not connected.")
            false
        }
    }

    override suspend fun send(data: ByteArray): Boolean {
        val ws = webSocket
        return if (ws != null && _state.value == WebSocketState.Connected) {
            ws.send(data.toByteString(0, data.size))
        } else {
            Logger.e(TAG, "Send failed: not connected.")
            false
        }
    }

    private fun buildOkHttpClient(config: WebSocketConfig): OkHttpClient {
        return OkHttpClient.Builder()
            .pingInterval(config.pingIntervalMs, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
            .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun createWebSocket(): Boolean {
        return try {
            _state.value = WebSocketState.Connecting

            val httpClient = buildOkHttpClient(currentConfig)
            client = httpClient

            val requestBuilder = Request.Builder().url(currentUrl)
            currentHeaders.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            webSocket = httpClient.newWebSocket(requestBuilder.build(), object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Logger.d(TAG, "onOpen: connected to $currentUrl")
                    reconnectAttempt = 0
                    _state.value = WebSocketState.Connected
                    launchEvent(WebSocketEvent.Connected(response.headers.toMultimap()))
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Logger.d(TAG, "onMessage(text): ${text.take(100)}")
                    launchEvent(WebSocketEvent.MessageReceived(WebSocketEvent.Message.Text(text)))
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    Logger.d(TAG, "onMessage(binary): ${bytes.size} bytes")
                    launchEvent(
                        WebSocketEvent.MessageReceived(
                            WebSocketEvent.Message.Binary(bytes.toByteArray())
                        )
                    )
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Logger.d(TAG, "onClosing: code=$code, reason=$reason")
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Logger.d(TAG, "onClosed: code=$code, reason=$reason")
                    _state.value = WebSocketState.Disconnected(code, reason)
                    launchEvent(WebSocketEvent.Disconnected(code, reason))
                    this@WebSocketManager.webSocket = null
                    if (!intentionalClose && currentConfig.reconnectEnabled) {
                        startReconnect()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Logger.e(TAG, "onFailure: ${t.message}, response=$response")
                    _state.value = WebSocketState.Failed(t)
                    launchEvent(WebSocketEvent.Error(t))
                    this@WebSocketManager.webSocket = null
                    if (!intentionalClose && currentConfig.reconnectEnabled) {
                        startReconnect()
                    }
                }
            })

            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create WebSocket: ${e.message}")
            _state.value = WebSocketState.Failed(e)
            launchEvent(WebSocketEvent.Error(e))
            false
        }
    }

    private fun startReconnect() {
        cancelReconnect()
        val config = currentConfig
        if (reconnectAttempt >= config.maxReconnectAttempts) {
            Logger.d(TAG, "Max reconnect attempts reached ($reconnectAttempt).")
            return
        }

        reconnectAttempt++
        val delayMs = calculateDelay(reconnectAttempt, config)
        Logger.d(TAG, "Reconnecting in ${delayMs}ms (attempt $reconnectAttempt)")

        _state.value = WebSocketState.Reconnecting(reconnectAttempt, delayMs)

        val currentScope = scope
        if (currentScope != null && currentScope.isActive) {
            reconnectJob = currentScope.launch {
                delay(delayMs.milliseconds)
                if (isActive && !intentionalClose) {
                    createWebSocket()
                }
            }
        }
    }

    private fun cancelReconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
    }

    private fun calculateDelay(attempt: Int, config: WebSocketConfig): Long {
        val delay = config.reconnectBaseDelayMs * config.reconnectDelayMultiplier.toDouble()
            .pow((attempt - 1).toDouble())
        return delay.toLong().coerceAtMost(config.reconnectMaxDelayMs)
    }

    private fun launchEvent(event: WebSocketEvent) {
        _events.tryEmit(event)
    }

    companion object {
        private const val TAG = "WebSocketManager"
        private const val NORMAL_CLOSURE_CODE = 1000
    }
}
