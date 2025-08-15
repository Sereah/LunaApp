package com.lunacattus.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lunacattus.app.base.common.toDateTimeString
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.model.GalleryVideo
import com.lunacattus.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

class GalleryVideoPagingSource(
    private val context: Context
) : PagingSource<Int, Gallery>() {

    override fun getRefreshKey(state: PagingState<Int, Gallery>): Int? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Gallery> = withContext(
        Dispatchers.IO
    ) {
        val position = params.key ?: 0

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
        )
        val sortOrder =
            "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null, sortOrder
            )
            val imageList = mutableListOf<Gallery>()
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val addDataCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                if (it.moveToPosition(position)) {
                    var count = 0
                    do {
                        val id = it.getLong(idCol)
                        val name = it.getString(nameCol)
                        val data = it.getLong(addDataCol) * 1000
                        val contentUri =
                            ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                        imageList.add(
                            Gallery.Video(
                                GalleryVideo(
                                    id,
                                    name,
                                    data,
                                    data.toDateTimeString(),
                                    contentUri
                                )
                            )
                        )
                        count++
                    } while (it.moveToNext() && count < params.loadSize)
                }
            }

            Logger.d(TAG, "Loaded ${imageList.size} items from position $position")

            LoadResult.Page(
                data = imageList,
                prevKey = if (position == 0) null else max(0, position - params.loadSize),
                nextKey = if (imageList.size < params.loadSize) null else position + imageList.size
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Error load: $e")
            LoadResult.Error(e)
        }
    }

    companion object {
        const val TAG = "GalleryPagingSource"
    }
}