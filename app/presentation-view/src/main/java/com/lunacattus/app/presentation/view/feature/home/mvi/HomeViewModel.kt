package com.lunacattus.app.presentation.view.feature.home.mvi

import androidx.lifecycle.viewModelScope
import com.lunacattus.app.presentation.view.base.BaseViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() :
    BaseViewModel<HomeUiIntent, HomeUIState, HomeUiEffect>() {

    companion object {
        const val TAG = "HomeViewModel"
    }

    init {
        Logger.d(TAG, "init.")
        viewModelScope.launch {
            delay(1000)
            updateUiState { HomeUIState.Loading }
            delay(2000)
            updateUiState { HomeUIState.Success(listOf("success")) }
        }
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared.")
    }

    override val initUiState: HomeUIState
        get() = HomeUIState.Init

    override fun processUiIntent(intent: HomeUiIntent) {
        when (intent) {
            HomeUiIntent.ClickButtonOne -> {
                sendSideEffect(HomeUiEffect.NavToFeatureOne)
            }

            HomeUiIntent.ClickButtonTwo -> {
                sendSideEffect(HomeUiEffect.NavToFeatureTwo)
            }
        }
    }
}