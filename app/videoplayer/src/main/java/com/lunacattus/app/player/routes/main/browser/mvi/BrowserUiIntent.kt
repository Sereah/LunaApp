package com.lunacattus.app.player.routes.main.browser.mvi

import com.lunacattus.app.domain.model.Video

sealed interface BrowserUiIntent {
    data class AddStreamToPlayList(val video: Video): BrowserUiIntent
}