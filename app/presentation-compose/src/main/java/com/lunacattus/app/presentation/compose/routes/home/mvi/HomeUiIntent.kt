package com.lunacattus.app.presentation.compose.routes.home.mvi

sealed interface HomeUiIntent {
    data object Start: HomeUiIntent
}