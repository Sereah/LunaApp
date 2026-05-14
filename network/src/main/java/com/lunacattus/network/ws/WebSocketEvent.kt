package com.lunacattus.network.ws

sealed interface WebSocketEvent {
    data class Connected(val headers: Map<String, List<String>>) : WebSocketEvent
    data class Disconnected(val code: Int, val reason: String) : WebSocketEvent
    data class MessageReceived(val message: Message) : WebSocketEvent
    data class Error(val throwable: Throwable) : WebSocketEvent

    sealed interface Message {
        data class Text(val data: String) : Message
        class Binary(val data: ByteArray) : Message {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false
                other as Binary
                return data.contentEquals(other.data)
            }

            override fun hashCode(): Int = data.contentHashCode()
        }
    }
}
