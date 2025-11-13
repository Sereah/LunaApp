package com.lunacattus.app.media.ui.main.mvi

import com.lunacattus.app.media.ui.base.IUIIntent

sealed class MainUiIntent : IUIIntent {
    data class ShowInfoDialog(val title: String, val message: String) : MainUiIntent()
}
