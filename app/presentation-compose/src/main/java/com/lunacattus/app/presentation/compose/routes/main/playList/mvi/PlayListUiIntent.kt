package com.lunacattus.app.presentation.compose.routes.main.playList.mvi

sealed interface PlayListUiIntent {
    data object Init : PlayListUiIntent
}