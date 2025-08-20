package com.lunacattus.app.data.repository.gallery

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.lunacattus.app.base.common.toDateTimeString
import com.lunacattus.app.base.di.MainScope
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.model.GalleryImage
import com.lunacattus.app.domain.model.GalleryVideo
import com.lunacattus.app.domain.model.id
import com.lunacattus.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MediaStoreQueryManager @AssistedInject constructor(
    context: Context,
    @MainScope private val scope: CoroutineScope,
    @Assisted private val uri: Uri,
    @Assisted private val projection: Array<String>,
    @Assisted private val cursorMapper: (Cursor) -> Gallery?,
) {

    @AssistedFactory
    interface Factory {
        fun create(
            uri: Uri,
            projection: Array<String>,
            cursorMapper: (Cursor) -> Gallery,
        ): MediaStoreQueryManager
    }

    private val _list = MutableStateFlow<List<Gallery>>(emptyList())
    val list = _list.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasMore = true

    private val contentResolver = context.contentResolver
    private val loadingMutex = Mutex()

    private val observer = MediaContentObserver(Handler(Looper.getMainLooper()))

    private inner class MediaContentObserver(handler: Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, changedUri: Uri?) {
            Logger.d(TAG, "onChange: $changedUri")
            if (changedUri == null) return
            scope.launch(Dispatchers.IO) {
                handleContentChange(changedUri)
            }
        }
    }

    init {
        contentResolver.registerContentObserver(
            uri,
            true,
            observer
        )
    }

    fun cleared() {
        Logger.d(TAG, "cleared.")
        contentResolver.unregisterContentObserver(observer)
        _list.value = emptyList()
        _isLoading.value = false
        hasMore = true
    }

    suspend fun loadMore(pageSize: Int) {
        if (_isLoading.value || !hasMore) {
            Logger.d(TAG, "loadMore skipped: isLoading=${_isLoading.value}, hasMore=$hasMore")
            return
        }

        loadingMutex.withLock {
            if (_isLoading.value || !hasMore) {
                Logger.d(
                    TAG,
                    "loadMore skipped inside mutex: isLoading=${_isLoading.value}, hasMore=$hasMore"
                )
                return@withLock
            }
            _isLoading.value = true
        }

        try {
            val currentSize = _list.value.size
            val newList = queryPage(currentSize, pageSize)
            Logger.d(
                TAG,
                "loadMore, currentSize: $currentSize, newListSize: ${newList.size}, pageSize: $pageSize"
            )

            if (newList.isNotEmpty()) {
                _list.emit((_list.value + newList).filterNot { it is Gallery.OtherFile })
            }
            hasMore = newList.size == pageSize
        } catch (e: Exception) {
            Logger.e(TAG, "Error during loadMore, $e")
            e.printStackTrace()
        } finally {
            _isLoading.value = false
            Logger.d(TAG, "loadMore finished, isLoading set to false, hasMore=$hasMore")
        }
    }

    private suspend fun handleContentChange(changedUri: Uri) {
        Logger.d(TAG, "handleContentChange: $changedUri")
        val id = changedUri.lastPathSegment?.toLongOrNull()
        val isNewItemPotentially =
            contentResolver.query(changedUri, arrayOf(projection[0]), null, null, null)
                ?.use { it.moveToFirst() } == true

        if (isNewItemPotentially) {
            // Potentially a new item or an update to an existing one
            val currentMaxId =
                _list.value.maxOfOrNull { it.id ?: -1L } ?: -1L
            val newItems = queryNewData(currentMaxId)
            if (newItems.isNotEmpty()) {
                loadingMutex.withLock {
                    val currentIds = _list.value.map { it.id }.toSet()
                    val trulyNew = newItems.filterNot { currentIds.contains(it.id) }
                    if (trulyNew.isNotEmpty()) {
                        _list.emit((trulyNew + _list.value).filterNot { it is Gallery.OtherFile }
                            .distinctBy { it.id }
                            .sortedByDescending { it.id })
                        Logger.d(TAG, "New data detected and prepended: ${trulyNew.size}")
                    }
                }
            } else if (id != null) { // No brand new items, check if it's an update to an existing one
                contentResolver.query(
                    ContentUris.withAppendedId(
                        uri,
                        id
                    ), projection, null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val updatedItem = cursorMapper(cursor) ?: return@use
                        loadingMutex.withLock {
                            val updatedList = _list.value.map { gallery ->
                                if (gallery.id == updatedItem.id) updatedItem else gallery
                            }
                            if (updatedList != _list.value) {
                                _list.emit(updatedList.filterNot { it is Gallery.OtherFile })
                                Logger.d(TAG, "Item updated: $updatedItem")
                            }
                        }
                    } else { // Item was deleted
                        loadingMutex.withLock {
                            val originalSize = _list.value.size
                            val updatedList = _list.value.filterNot { it.id == id }
                            if (updatedList.size < originalSize) {
                                _list.emit(updatedList.filterNot { it is Gallery.OtherFile })
                                Logger.d(TAG, "Item removed: $id")
                            }
                        }
                    }
                }
            }
        } else if (id != null) { // Item was deleted (not found by query)
            loadingMutex.withLock {
                val originalSize = _list.value.size
                val updatedList = _list.value.filterNot { it.id == id }
                if (updatedList.size < originalSize) {
                    _list.emit(updatedList.filterNot { it is Gallery.OtherFile })
                    Logger.d(TAG, "Item removed (not found): $id")
                }
            }
        }
    }


    private suspend fun queryPage(offset: Int, limit: Int): List<Gallery> =
        withContext(Dispatchers.IO) {
            val queryArgs = Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    "${projection[0]} DESC"
                )
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            }
            val list = mutableListOf<Gallery>()
            contentResolver.query(uri, projection, queryArgs, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    cursorMapper(cursor)?.let { gallery ->
                        list.add(gallery)
                    }
                }
            }
            Logger.d(TAG, "queryPage: ${list.size} from $offset, limit $limit")
            list
        }

    private suspend fun queryNewData(minID: Long): List<Gallery> = withContext(Dispatchers.IO) {
        if (minID == -1L && _list.value.isNotEmpty()) {
            Logger.d(
                TAG,
                "queryNewData called with minID -1 but list is not empty. This might be an issue."
            )
            return@withContext emptyList()
        }

        val selection = "${projection[0]} > ?"
        val selectionArgs = arrayOf(minID.toString())
        val sortOrder = "${projection[0]} DESC"
        val list = mutableListOf<Gallery>()
        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                cursorMapper(cursor)?.let { gallery ->
                    list.add(gallery)
                }
            }
        }
        Logger.d(TAG, "queryNewData (newer than $minID): ${list.size}")
        list
    }

    companion object {
        const val TAG = "MediaStoreQueryManager"
    }
}

fun imageCursorToGallery(cursor: Cursor): Gallery {
    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
    val name =
        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
    val date =
        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)) * 1000
    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    return Gallery.Image(
        GalleryImage(
            id, name, date, date.toDateTimeString(), contentUri
        )
    )
}

val imageProjection = arrayOf(
    MediaStore.Images.Media._ID,
    MediaStore.Images.Media.DISPLAY_NAME,
    MediaStore.Images.Media.DATE_ADDED
)

fun videoCursorToGallery(cursor: Cursor): Gallery {
    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
    val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
    val date =
        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)) * 1000
    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
    return Gallery.Video(
        GalleryVideo(
            id, name, date, date.toDateTimeString(), contentUri
        )
    )
}

val videoProjection = arrayOf(
    MediaStore.Video.Media._ID,
    MediaStore.Video.Media.DISPLAY_NAME,
    MediaStore.Video.Media.DATE_ADDED,
)

fun allMediaCursorToGallery(cursor: Cursor): Gallery {
    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
    val name =
        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
    val addData =
        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000
    val mimeType =
        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)) ?: ""
    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)

    return when {
        mimeType.startsWith("image") -> {
            Gallery.Image(
                GalleryImage(
                    id, name, addData, addData.toDateTimeString(), contentUri
                )
            )
        }

        mimeType.startsWith("video") -> {
            Gallery.Video(
                GalleryVideo(
                    id, name, addData, addData.toDateTimeString(), contentUri
                )
            )
        }

        mimeType.startsWith("audio") -> Gallery.OtherFile

        else -> Gallery.OtherFile
    }
}

val allMediaProjection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.DISPLAY_NAME,
    MediaStore.Files.FileColumns.DATE_ADDED,
    MediaStore.Files.FileColumns.MIME_TYPE
)