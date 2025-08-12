package com.lunacattus.app.gallery.feature.play.mvi

import com.lunacattus.app.base.view.IUIState

sealed interface PlayUiState: IUIState {
    data object Init: PlayUiState
    data object Loading: PlayUiState
    data object Success: PlayUiState
    data object Error: PlayUiState
}