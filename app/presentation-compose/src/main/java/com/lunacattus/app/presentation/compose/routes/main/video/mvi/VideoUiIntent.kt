package com.lunacattus.app.presentation.compose.routes.main.video.mvi

sealed interface VideoUiIntent {
    data object Init: VideoUiIntent
}