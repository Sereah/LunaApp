package com.lunacattus.app.domain.repository

import androidx.paging.PagingData
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.model.GalleryImage
import com.lunacattus.app.domain.model.GalleryVideo
import kotlinx.coroutines.flow.Flow

interface IMediaStoreRepository {
    fun queryAllPic(): Flow<PagingData<Gallery>>

    fun queryAllVideo(): Flow<PagingData<Gallery>>

    fun queryAllMedia(): Flow<PagingData<Gallery>>

    fun unregisterContentObserver()
}