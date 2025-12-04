package com.lunacattus.app.player.ui.routes.main.playList.mvi

import com.lunacattus.app.player.model.Video

sealed interface PlayListUiIntent {
    data object Init : PlayListUiIntent
    data class RemoveVideo(val video: Video) : PlayListUiIntent
}