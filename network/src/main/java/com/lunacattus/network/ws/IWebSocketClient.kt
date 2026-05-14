package com.lunacattus.network.ws

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IWebSocketClient {

    val state: StateFlow<WebSocketState>

    val events: SharedFlow<WebSocketEvent>

    suspend fun connect(
        url: String,
        headers: Map<String, String> = emptyMap(),
        config: WebSocketConfig = WebSocketConfig(),
    ): Boolean

    suspend fun disconnect()

    suspend fun send(message: String): Boolean

    suspend fun send(data: ByteArray): Boolean
}
