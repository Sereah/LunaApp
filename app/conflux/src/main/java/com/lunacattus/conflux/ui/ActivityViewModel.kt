package com.lunacattus.conflux.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor() : ViewModel() {

    var dynamicColor by mutableStateOf(true)
        private set

    fun changeDynamicColor(enable: Boolean) {
        dynamicColor = enable
    }

}