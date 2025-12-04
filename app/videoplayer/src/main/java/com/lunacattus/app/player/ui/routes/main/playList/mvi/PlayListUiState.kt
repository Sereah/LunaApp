package com.lunacattus.app.player.ui.routes.main.playList.mvi

import com.lunacattus.app.player.model.Video

sealed interface PlayListUiState {
    data object Init : PlayListUiState
    data object Loading : PlayListUiState
    data class Success(val playList: List<Video>) : PlayListUiState
    data class Fail(val msg: String) : PlayListUiState
}