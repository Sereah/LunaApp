package com.lunacattus.app.media.ui.main.mvi

import com.lunacattus.app.media.ui.base.IUIState

data class MainUiState(
    val loading: Boolean = false,
    val infoDialogData: InfoDialogData = InfoDialogData(),
) : IUIState

data class InfoDialogData(
    val dialogTitle: String = "",
    val dialogMessage: String = ""
)