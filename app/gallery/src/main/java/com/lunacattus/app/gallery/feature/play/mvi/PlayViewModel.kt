package com.lunacattus.app.gallery.feature.play.mvi

import com.lunacattus.app.base.view.BaseViewModel
import com.lunacattus.logger.Logger

class PlayViewModel : BaseViewModel<PlayUiIntent, PlayUiState, PlayUiEffect>() {

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        Logger.d(TAG, "cleared.")
    }

    override val initUiState: PlayUiState
        get() = PlayUiState.Init

    override fun processUiIntent(intent: PlayUiIntent) {
        when (intent) {
            PlayUiIntent.Init -> {

            }
        }
    }

    companion object {
        const val TAG = "PlayViewModel"
    }
}