package com.lunacattus.app.player.routes.main.playList.mvi

import com.lunacattus.app.domain.model.Video

sealed interface PlayListUiIntent {
    data object Init : PlayListUiIntent
    data class RemoveVideo(val video: Video): PlayListUiIntent
}