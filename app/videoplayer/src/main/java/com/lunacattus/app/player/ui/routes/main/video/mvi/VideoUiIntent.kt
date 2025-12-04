package com.lunacattus.app.player.ui.routes.main.video.mvi

import com.lunacattus.app.player.model.JsonVideo

sealed interface VideoUiIntent {
    data object Init : VideoUiIntent
    data class AddToPlayList(val video: JsonVideo) : VideoUiIntent
}