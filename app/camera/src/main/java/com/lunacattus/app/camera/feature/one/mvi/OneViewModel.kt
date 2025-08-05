package com.lunacattus.app.camera.feature.one.mvi

import androidx.lifecycle.viewModelScope
import com.lunacattus.app.camera.base.BaseViewModel
import com.lunacattus.app.domain.model.Data
import com.lunacattus.app.domain.usecase.InsertDataUseCase
import com.lunacattus.app.domain.usecase.QueryAllDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OneViewModel @Inject constructor(
    private val insertDataUseCase: InsertDataUseCase,
    private val queryAllDataUseCase: QueryAllDataUseCase
) : BaseViewModel<OneUiIntent, OneUIState, OneUiEffect>() {

    init {
        updateUiState { OneUIState.Loading }
        viewModelScope.launch {
            delay(1000)
            queryAllDataUseCase.invoke()
                .onSuccess {
                    it.collect { dataList ->
                        updateUiState { OneUIState.Success(dataList) }
                    }
                }
                .onFailure {
                    sendSideEffect(OneUiEffect.ShowToast("Query all data fail, $it"))
                }
        }
    }

    override val initUiState: OneUIState
        get() = OneUIState.Init

    override fun processUiIntent(intent: OneUiIntent) {
        when (intent) {
            is OneUiIntent.AddData -> {
                viewModelScope.launch {
                    val result = insertDataUseCase.invoke(
                        Data(
                            id = UUID.randomUUID().toString(),
                            name = intent.name
                        )
                    )
                    result
                        .onFailure {
                            sendSideEffect(OneUiEffect.ShowToast("Add data fail, $it"))
                        }.onSuccess {
                            sendSideEffect(OneUiEffect.ShowToast("Add data success."))
                        }
                }
            }
        }
    }
}