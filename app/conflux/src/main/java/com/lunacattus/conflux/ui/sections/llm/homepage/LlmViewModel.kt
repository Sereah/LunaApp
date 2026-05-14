package com.lunacattus.conflux.ui.sections.llm.homepage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LlmViewModel @Inject constructor() : ViewModel() {

    companion object {
        const val TAG = "LlmViewModel"
    }
}