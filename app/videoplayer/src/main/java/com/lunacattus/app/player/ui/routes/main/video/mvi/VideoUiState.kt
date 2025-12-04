package com.lunacattus.app.player.ui.routes.main.video.mvi

import com.lunacattus.app.player.model.JsonVideo

sealed interface VideoUiState {
    data object Init : VideoUiState
    data object Loading : VideoUiState
    data class Success(val jsonVideo: List<JsonVideo>) : VideoUiState
    data class Fail(val msg: String) : VideoUiState
}