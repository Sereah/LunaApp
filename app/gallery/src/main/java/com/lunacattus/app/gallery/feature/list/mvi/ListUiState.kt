package com.lunacattus.app.gallery.feature.list.mvi

import com.lunacattus.app.base.view.base.IUIState

sealed interface ListUiState: IUIState {
    data object Init: ListUiState
    data object Loading: ListUiState
    data object Success: ListUiState
    data object Error: ListUiState
}