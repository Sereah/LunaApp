package com.lunacattus.app.player.routes.main.video.mvi

import com.lunacattus.app.domain.model.JsonVideo

sealed interface VideoUiIntent {
    data object Init : VideoUiIntent
    data class AddToPlayList(val video: JsonVideo) : VideoUiIntent
}