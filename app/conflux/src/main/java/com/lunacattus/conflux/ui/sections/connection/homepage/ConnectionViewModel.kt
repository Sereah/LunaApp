package com.lunacattus.conflux.ui.sections.connection.homepage

import androidx.lifecycle.ViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(): ViewModel() {

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared.")
    }

    companion object {
        const val TAG = "ConnectionViewModel"
    }
}