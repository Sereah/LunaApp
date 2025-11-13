package com.lunacattus.app.media.ui.main.mvi

import com.lunacattus.app.media.ui.base.ISideEffect

sealed class MainSideEffect : ISideEffect {
    data object NavigateToInfoDialog : MainSideEffect()
}
