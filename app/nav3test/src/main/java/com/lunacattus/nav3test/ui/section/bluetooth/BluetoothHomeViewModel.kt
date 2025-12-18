package com.lunacattus.nav3test.ui.section.bluetooth

import androidx.lifecycle.ViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BluetoothHomeViewModel @Inject constructor(): ViewModel() {
    init {
        Logger.d(TAG, "init")
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
    }

    companion object {
        const val TAG = "BluetoothHomeViewModel"
    }
}