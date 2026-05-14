package com.lunacattus.conflux.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor() : ViewModel() {

    var dynamicColor by mutableStateOf(false)
        private set

    var nightMode by mutableStateOf(false)
        private set

    fun changeDynamicColor(enable: Boolean) {
        dynamicColor = enable
    }

    fun changeNightMode(enable: Boolean) {
        nightMode = enable
    }
}