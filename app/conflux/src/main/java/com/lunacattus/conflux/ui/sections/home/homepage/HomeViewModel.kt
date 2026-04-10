package com.lunacattus.conflux.ui.sections.home.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.conflux.di.GemmaManager
import com.lunacattus.conflux.domain.llm.ILLMManager
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:GemmaManager private val llmManager: ILLMManager
) : ViewModel() {

    init {
        Logger.d(TAG, "init.")
        viewModelScope.launch {
            llmManager.initModel()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
    }

    companion object {
        const val TAG = "HomeViewModel"
    }
}