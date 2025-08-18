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
import javax.inject.Inject

data class GalleryKey(
    val dateAddedMillis: Long,
    val id: Long
)

class GalleryAllPagingSource @Inject constructor(
    private val context: Context
) : PagingSource<GalleryKey, Gallery>() {

    override fun getRefreshKey(state: PagingState<GalleryKey, Gallery>): GalleryKey? = null

    override suspend fun load(params: LoadParams<GalleryKey>): LoadResult<GalleryKey, Gallery> {
        val lastKey = params.key

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
        )

        val sortOrder =
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC, ${MediaStore.Files.FileColumns._ID} DESC"

        var selection: String? = null
        var selectionArgs: Array<String>? = null

        when (params) {
            is LoadParams.Append<*> -> {
                // 往后（更旧的数据）
                lastKey?.let {
                    selection =
                        "(${MediaStore.Files.FileColumns.DATE_ADDED} < ?) OR " +
                                "(${MediaStore.Files.FileColumns.DATE_ADDED} = ? AND ${MediaStore.Files.FileColumns._ID} < ?)"
                    selectionArgs = arrayOf(
                        (it.dateAddedMillis / 1000).toString(),
                        (it.dateAddedMillis / 1000).toString(),
                        it.id.toString()
                    )
                }
            }

            is LoadParams.Prepend<*> -> {
                // 往前（更新的数据）
                lastKey?.let {
                    selection =
                        "(${MediaStore.Files.FileColumns.DATE_ADDED} > ?) OR " +
                                "(${MediaStore.Files.FileColumns.DATE_ADDED} = ? AND ${MediaStore.Files.FileColumns._ID} > ?)"
                    selectionArgs = arrayOf(
                        (it.dateAddedMillis / 1000).toString(),
                        (it.dateAddedMillis / 1000).toString(),
                        it.id.toString()
                    )
                }
            }

            is LoadParams.Refresh<*> -> {
                selection = null
                selectionArgs = null
            }
        }

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            val galleryList = mutableListOf<Gallery>()
            var firstKey: GalleryKey? = null
            var lastKey: GalleryKey? = null

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val addDateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val mimeTypeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                var count = 0
                while (it.moveToNext() && count < params.loadSize) {
                    val id = it.getLong(idCol)
                    val dateAdded = it.getLong(addDateCol) * 1000
                    val name = it.getString(nameCol)
                    val mimeType = it.getString(mimeTypeCol)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        id
                    )

                    if (mimeType.isNullOrEmpty()) continue
                    if (mimeType.startsWith("video")) {
                        galleryList.add(
                            Gallery.Video(
                                GalleryVideo(
                                    id, name, dateAdded, dateAdded.toDateTimeString(), contentUri
                                )
                            )
                        )
                    } else if (mimeType.startsWith("image")) {
                        galleryList.add(
                            Gallery.Image(
                                GalleryImage(
                                    id, name, dateAdded, dateAdded.toDateTimeString(), contentUri
                                )
                            )
                        )
                    } else {
                        continue
                    }

                    val itemKey = GalleryKey(dateAdded, id)
                    if (firstKey == null) firstKey = itemKey
                    lastKey = itemKey
                    count++
                }
            }

            val prevKey = if (galleryList.isEmpty() || firstKey == null) null else firstKey
            val nextKey = if (galleryList.isEmpty() || lastKey == null) null else lastKey
            Logger.d(
                TAG,
                "Loaded ${galleryList.size} items, prevKey=$prevKey, nextKey=$nextKey"
            )

            return LoadResult.Page(
                data = galleryList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e(TAG, "Error load: $e")
            return LoadResult.Error(e)
        }
    }

    companion object {
        const val TAG = "GalleryAllPagingSource"
    }

}