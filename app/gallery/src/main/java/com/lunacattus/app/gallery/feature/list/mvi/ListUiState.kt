package com.lunacattus.app.gallery.feature.list.mvi

import com.lunacattus.app.base.view.base.IUIState
import com.lunacattus.app.domain.model.Gallery

sealed interface ListUiState : IUIState {
    data object Init : ListUiState
    data object Loading : ListUiState
    data class Success(
        val imageList: List<Gallery> = emptyList(),
        val videoList: List<Gallery> = emptyList(),
        val mediaList: List<Gallery> = emptyList(),
    ) : ListUiState

    data object Error : ListUiState
}