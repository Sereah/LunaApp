package com.lunacattus.conflux.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object ActivityToastEvent {
    private val _events = MutableSharedFlow<ToastEvent>()
    val events: SharedFlow<ToastEvent> = _events

    suspend fun send(event: ToastEvent) {
        _events.emit(event)
    }
}

sealed interface ToastEvent {
    data class ShowToast(val message: String, val id: Long = System.currentTimeMillis()) : ToastEvent
}
