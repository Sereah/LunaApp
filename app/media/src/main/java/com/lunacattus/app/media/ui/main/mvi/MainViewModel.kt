package com.lunacattus.app.media.ui.main.mvi

import com.lunacattus.app.media.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() :
    BaseViewModel<MainUiIntent, MainUiState, MainSideEffect>() {

    override val initUiState: MainUiState get() = MainUiState()

    override fun processUiIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.ShowInfoDialog -> {
                updateUiState {
                    copy(infoDialogData = InfoDialogData(intent.title, intent.message))
                }

                sendSideEffect(MainSideEffect.NavigateToInfoDialog)
            }
        }
    }
}
