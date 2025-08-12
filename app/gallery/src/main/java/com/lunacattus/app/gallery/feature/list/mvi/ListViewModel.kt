package com.lunacattus.app.gallery.feature.list.mvi

import androidx.lifecycle.viewModelScope
import com.lunacattus.app.base.view.BaseViewModel
import com.lunacattus.app.data.repository.MediaStoreRepository
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository
) : BaseViewModel<ListUiIntent, ListUiState, ListUiEffect>() {

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        Logger.d(TAG, "cleared.")
    }

    override val initUiState: ListUiState
        get() = ListUiState.Init

    override fun processUiIntent(intent: ListUiIntent) {
        when (intent) {
            ListUiIntent.Init -> {
                viewModelScope.launch {
                    mediaStoreRepository.queryAllPic()
                }
            }
        }
    }

    companion object {
        const val TAG = "ListViewModel"
    }
}