package com.lunacattus.app.gallery.feature.list.mvi

import androidx.lifecycle.viewModelScope
import com.lunacattus.app.base.view.base.BaseViewModel
import com.lunacattus.app.data.repository.gallery.GalleryRepository
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.model.GalleryDate
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val mediaStoreRepository: GalleryRepository,
) : BaseViewModel<ListUiIntent, ListUiState, ListUiEffect>() {

    init {
        Logger.d(TAG, "init.")
        observeFlow()
    }

    override fun onCleared() {
        mediaStoreRepository.cleared()
        Logger.d(TAG, "cleared.")
    }

    override val initUiState: ListUiState
        get() = ListUiState.Init

    override fun processUiIntent(intent: ListUiIntent) {
        when (intent) {
            ListUiIntent.Init -> {
                loadMoreMedia()
                loadMorePic()
                loadMoreVideo()
            }

            ListUiIntent.LoadMoreImage -> loadMorePic()
            ListUiIntent.LoadMoreVideo -> loadMoreVideo()
            ListUiIntent.LoadMoreMedia -> loadMoreMedia()
        }
    }

    private fun loadMorePic() {
        viewModelScope.launch {
            mediaStoreRepository.loadMorePic(PAGE_SIZE)
        }
    }

    private fun loadMoreVideo() {
        viewModelScope.launch {
            mediaStoreRepository.loadMoreVideo(PAGE_SIZE)
        }
    }

    private fun loadMoreMedia() {
        viewModelScope.launch {
            mediaStoreRepository.loadMoreMedia(PAGE_SIZE)
        }
    }

    private fun observeFlow() {
        viewModelScope.launch {
            mediaStoreRepository.queryAllPic().collect { galleries ->
                updateUiState { state ->
                    when (state) {
                        is ListUiState.Success -> {
                            state.copy(imageList = galleries.groupByDay())
                        }

                        else -> ListUiState.Success(imageList = galleries.groupByDay())
                    }
                }
            }
        }
        viewModelScope.launch {
            mediaStoreRepository.queryAllVideo().collect { galleries ->
                updateUiState { state ->
                    when (state) {
                        is ListUiState.Success -> {
                            state.copy(videoList = galleries.groupByDay())
                        }

                        else -> ListUiState.Success(videoList = galleries.groupByDay())
                    }
                }
            }
        }
        viewModelScope.launch {
            mediaStoreRepository.queryAllMedia().collect { galleries ->
                updateUiState { state ->
                    when (state) {
                        is ListUiState.Success -> {
                            state.copy(mediaList = galleries.groupByDay())
                        }

                        else -> ListUiState.Success(mediaList = galleries.groupByDay())
                    }
                }
            }
        }
    }

    private fun List<Gallery>.groupByDay(): List<Gallery> {
        val groupByDate = this.groupBy { gallery ->
            val dateStamp = when (gallery) {
                is Gallery.Image -> gallery.galleryImage.addData
                is Gallery.Video -> gallery.galleryVideo.addData
                else -> throw IllegalArgumentException("Unknown gallery type: $gallery")
            }
            Instant.ofEpochMilli(dateStamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val sortedGroups = groupByDate.toSortedMap(compareByDescending { it })
        val resultList = mutableListOf<Gallery>()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.getDefault())
        for ((date, itemsInGroup) in sortedGroups) {
            resultList.add(Gallery.Date(GalleryDate(date = date.format(dateFormatter))))
            val sortedItems = itemsInGroup.sortedByDescending {
                when (it) {
                    is Gallery.Image -> it.galleryImage.addData
                    is Gallery.Video -> it.galleryVideo.addData
                    else -> Long.MAX_VALUE
                }
            }
            resultList.addAll(sortedItems)
        }
        return resultList
    }

    companion object {
        const val TAG = "ListViewModel"

        const val PAGE_SIZE = 50
    }
}