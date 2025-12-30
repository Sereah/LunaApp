package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.lifecycle.ViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(): ViewModel() {

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared.")
    }

    companion object {
        const val TAG = "MediaViewModel"
    }
}