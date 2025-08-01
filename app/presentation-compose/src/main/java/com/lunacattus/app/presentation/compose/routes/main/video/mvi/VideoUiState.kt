package com.lunacattus.app.presentation.compose.routes.main.video.mvi

import com.lunacattus.app.domain.model.Video

sealed interface VideoUiState {
    data object Init : VideoUiState
    data object Loading : VideoUiState
    data class Success(val jsonVideo: List<Video>) : VideoUiState
    data class Fail(val msg: String) : VideoUiState
}