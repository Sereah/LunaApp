package com.lunacattus.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lunacattus.app.base.common.toDateTimeString
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.model.GalleryImage
import com.lunacattus.app.domain.model.GalleryVideo
import com.lunacattus.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

class GalleryPagingSource @Inject constructor(
    private val context: Context
) : PagingSource<Int, Gallery>() {

    override fun getRefreshKey(state: PagingState<Int, Gallery>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Gallery> = withContext(
        Dispatchers.IO
    ) {
        val position = params.key ?: 0

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                sortOrder
            )

            val galleryList = mutableListOf<Gallery>()

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val addDataCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeTypeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                if (it.moveToPosition(position)) {
                    var count = 0
                    do {
                        val id = it.getLong(idCol)
                        val name = it.getString(nameCol)
                        val data = it.getLong(addDataCol) * 1000
                        val contentUri =
                            ContentUris.withAppendedId(
                                MediaStore.Files.getContentUri("external"),
                                id
                            )
                        val mimeType = it.getString(mimeTypeCol)
                        if (mimeType.isNullOrEmpty()) {
                            continue
                        }
                        if (mimeType.startsWith("video")) {
                            galleryList.add(
                                Gallery.Video(
                                    GalleryVideo(
                                        id, name, data, data.toDateTimeString(), contentUri
                                    )
                                )
                            )
                        } else if (mimeType.startsWith("image")) {
                            galleryList.add(
                                Gallery.Image(
                                    GalleryImage(
                                        id, name, data, data.toDateTimeString(), contentUri
                                    )
                                )
                            )
                        } else {
                            continue
                        }
                        count++
                    } while (it.moveToNext() && count < params.loadSize)
                }
            }
            val prevKey = if (position == 0) null else max(0, position - params.loadSize)
            val nextKey =
                if (galleryList.size < params.loadSize) null else position + galleryList.size

            Logger.d(
                TAG,
                "Loaded ${galleryList.size} items from position $position, prevKey: $prevKey, nextKey: $nextKey"
            )
            LoadResult.Page(
                data = galleryList, prevKey = prevKey, nextKey = nextKey
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e(TAG, "Error load: $e")
            LoadResult.Error(e)
        }
    }

    companion object {
        const val TAG = "GalleryPagingSource"
    }
}
