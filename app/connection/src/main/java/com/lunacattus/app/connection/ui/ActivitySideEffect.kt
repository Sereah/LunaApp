package com.lunacattus.app.connection.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object ActivitySideEffect {
    private val _events = MutableSharedFlow<ActivityEvent>()
    val events: SharedFlow<ActivityEvent> = _events

    suspend fun send(event: ActivityEvent) {
        _events.emit(event)
    }
}

sealed interface ActivityEvent {
    data class ShowToast(val message: String, val id: Long = System.currentTimeMillis()) : ActivityEvent
    data class LogError(val throwable: Throwable, val id: Long = System.currentTimeMillis()) : ActivityEvent
}
