package com.lunacattus.app.player.ui.routes.main.browser.mvi

import com.lunacattus.app.player.model.Video

sealed interface BrowserUiIntent {
    data class AddStreamToPlayList(val video: Video) : BrowserUiIntent
}