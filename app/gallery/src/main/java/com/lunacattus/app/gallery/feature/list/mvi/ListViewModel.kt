package com.lunacattus.app.gallery.feature.list.mvi

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.lunacattus.app.base.view.base.BaseViewModel
import com.lunacattus.app.data.repository.MediaStoreRepository
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository
) : BaseViewModel<ListUiIntent, ListUiState, ListUiEffect>() {

    val imageFlow = mediaStoreRepository.queryAllPic().cachedIn(viewModelScope)
    val videoFlow = mediaStoreRepository.queryAllVideo().cachedIn(viewModelScope)
    val galleryFlow = mediaStoreRepository.queryAllMedia().cachedIn(viewModelScope)

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        mediaStoreRepository.unregisterContentObserver()
        Logger.d(TAG, "cleared.")
    }

    override val initUiState: ListUiState
        get() = ListUiState.Init

    override fun processUiIntent(intent: ListUiIntent) {
        when (intent) {
            ListUiIntent.Init -> {
            }
        }
    }

    companion object {
        const val TAG = "ListViewModel"
    }
}