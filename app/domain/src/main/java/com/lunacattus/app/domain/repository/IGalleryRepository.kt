package com.lunacattus.app.domain.repository

import androidx.paging.PagingData
import com.lunacattus.app.domain.model.Gallery
import kotlinx.coroutines.flow.Flow

interface IGalleryRepository {
    fun queryAllPic(): Flow<List<Gallery>>
    suspend fun loadMorePic(pageSize: Int)

    fun queryAllVideo(): Flow<List<Gallery>>

    suspend fun loadMoreVideo(pageSize: Int)

    fun queryAllMedia(): Flow<List<Gallery>>

    suspend fun loadMoreMedia(pageSize: Int)

    fun cleared()
}