package com.lunacattus.network.ws

sealed interface WebSocketState {
    data object Idle : WebSocketState
    data object Connecting : WebSocketState
    data object Connected : WebSocketState
    data class Reconnecting(val attempt: Int, val delayMs: Long) : WebSocketState
    data class Disconnected(val code: Int, val reason: String) : WebSocketState
    data class Failed(val throwable: Throwable) : WebSocketState
}
