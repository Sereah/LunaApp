package com.lunacattus.conflux.ui.sections.setting.homepage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(): ViewModel() {

    init {
        Logger.d(TAG, "init.")
    }

    var accessibilityEnable by mutableStateOf(false)
        private set

    fun changeAccessibility(enable: Boolean) {
        accessibilityEnable = enable
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared.")
    }

    companion object {
        const val TAG = "SettingViewModel"
    }
}