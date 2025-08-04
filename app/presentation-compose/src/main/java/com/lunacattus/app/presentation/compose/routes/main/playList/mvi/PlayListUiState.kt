package com.lunacattus.app.presentation.compose.routes.main.playList.mvi

import com.lunacattus.app.domain.model.JsonVideo
import com.lunacattus.app.domain.model.Video

sealed interface PlayListUiState {
    data object Init : PlayListUiState
    data object Loading : PlayListUiState
    data class Success(val playList: List<Video>) : PlayListUiState
    data class Fail(val msg: String) : PlayListUiState
}