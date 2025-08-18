package com.lunacattus.app.data.repository

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.repository.IMediaStoreRepository
import com.lunacattus.logger.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreRepository @Inject constructor(
    private val context: Context,
) : IMediaStoreRepository {
    private var allMediaPagingSource: GalleryPagingSource? = null

    override fun queryAllPic(): Flow<PagingData<Gallery>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { GalleryImagePagingSource(context) }
        ).flow
    }

    override fun queryAllVideo(): Flow<PagingData<Gallery>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { GalleryVideoPagingSource(context) }
        ).flow
    }

    override fun queryAllMedia(): Flow<PagingData<Gallery>> {
        val pagingSourceFactory = {
            GalleryPagingSource(context).also { newSource ->
                allMediaPagingSource = newSource
            }
        }
        val currentPager = Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
        context.contentResolver.registerContentObserver(
            MediaStore.Files.getContentUri("external"),
            true,
            contentObserver
        )
        return currentPager
    }

    override fun unregisterContentObserver() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Logger.d(TAG, "ContentObserver onChange: $uri")
            allMediaPagingSource?.invalidate()
        }
    }

    companion object {
        const val TAG = "MediaStoreRepository"
        private const val PAGE_SIZE = 50
    }
}