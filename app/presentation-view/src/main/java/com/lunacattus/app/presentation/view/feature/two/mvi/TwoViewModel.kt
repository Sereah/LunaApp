package com.lunacattus.app.presentation.view.feature.two.mvi

import com.lunacattus.app.presentation.view.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TwoViewModel @Inject constructor() :
    BaseViewModel<TwoUiIntent, TwoUIState, TwoUiEffect>() {

    override val initUiState: TwoUIState
        get() = TwoUIState.Init

    override fun processUiIntent(intent: TwoUiIntent) {
    }
}