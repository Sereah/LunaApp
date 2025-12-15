package com.lunacattus.app.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.lunacattus.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * A generic class for querying a ContentProvider with pagination support.
 *
 * @param T The data type of the items being queried.
 * @property context The application context.
 * @property scope A CoroutineScope for launching background tasks.
 * @property uri The ContentProvider's URI to query.
 * @property projection The list of columns to return. **Note:** The first column is assumed to be the numeric, sortable ID.
 * @property cursorMapper A function to map a Cursor row to an object of type T.
 * @property idExtractor A function to extract the Long ID from an object of type T.
 */
class ContentProviderQueryManager<T : Any> @AssistedInject constructor(
    context: Context,
    @Assisted private val uri: Uri,
    @Assisted private val projection: Array<String>,
    @Assisted private val cursorMapper: (Cursor) -> T?,
    @Assisted private val idExtractor: (T) -> Long?,
) {

    @AssistedFactory
    interface Factory<T : Any> {
        fun create(
            uri: Uri,
            projection: Array<String>,
            cursorMapper: (Cursor) -> T?,
            idExtractor: (T) -> Long?,
        ): ContentProviderQueryManager<T>
    }

    private val _list = MutableStateFlow<List<T>>(emptyList())
    val list: StateFlow<List<T>> = _list.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasMore = true

    private val contentResolver = context.contentResolver
    private val loadingMutex = Mutex()

    private val observer = MediaContentObserver(Handler(Looper.getMainLooper()))
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
            true, // notifyForDescendants
            observer
        )
    }

    fun cleared() {
        Logger.d(TAG, "cleared.")
        contentResolver.unregisterContentObserver(observer)
        _list.value = emptyList()
        _isLoading.value = false
        hasMore = true
        scope.cancel()
    }

    suspend fun loadMore(pageSize: Int) {
        if (_isLoading.value || !hasMore) {
            Logger.d(TAG, "loadMore skipped: isLoading=${_isLoading.value}, hasMore=$hasMore")
            return
        }

        loadingMutex.withLock {
            if (_isLoading.value || !hasMore) {
                Logger.d(TAG, "loadMore skipped inside mutex: isLoading=${_isLoading.value}, hasMore=$hasMore")
                return@withLock
            }
            _isLoading.value = true
        }

        try {
            val currentSize = _list.value.size
            val newList = queryPage(currentSize, pageSize)
            Logger.d(TAG, "loadMore, currentSize: $currentSize, newListSize: ${newList.size}, pageSize: $pageSize")

            if (newList.isNotEmpty()) {
                _list.emit((_list.value + newList).distinctBy { idExtractor(it) })
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

        // This assumes the first projection column is the ID column, which must be numeric and sortable.
        val idColumn = projection.firstOrNull() ?: return

        val isNewItemPotentially =
            contentResolver.query(changedUri, arrayOf(idColumn), null, null, null)
                ?.use { it.moveToFirst() } == true

        if (isNewItemPotentially) {
            // Potentially a new item or an update
            val currentMaxId = _list.value.maxOfOrNull { idExtractor(it) ?: -1L } ?: -1L
            val newItems = queryNewData(currentMaxId, idColumn)
            if (newItems.isNotEmpty()) {
                loadingMutex.withLock {
                    val currentIds = _list.value.map { idExtractor(it) }.toSet()
                    val trulyNew = newItems.filterNot { currentIds.contains(idExtractor(it)) }
                    if (trulyNew.isNotEmpty()) {
                        _list.emit((trulyNew + _list.value)
                            .distinctBy { idExtractor(it) }
                            .sortedByDescending { idExtractor(it) })
                        Logger.d(TAG, "New data detected and prepended: ${trulyNew.size}")
                    }
                }
            } else if (id != null) { // No brand new items, check for an update
                contentResolver.query(ContentUris.withAppendedId(uri, id), projection, null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val updatedItem = cursorMapper(cursor) ?: return@use
                            loadingMutex.withLock {
                                val updatedList = _list.value.map { item ->
                                    if (idExtractor(item) == idExtractor(updatedItem)) updatedItem else item
                                }
                                if (updatedList != _list.value) {
                                    _list.emit(updatedList)
                                    Logger.d(TAG, "Item updated: $updatedItem")
                                }
                            }
                        } else { // Item was deleted
                            removeItem(id)
                        }
                    }
            }
        } else if (id != null) { // Item was deleted (not found by query)
            removeItem(id)
        }
    }

    private suspend fun removeItem(id: Long) {
        loadingMutex.withLock {
            val originalSize = _list.value.size
            val updatedList = _list.value.filterNot { idExtractor(it) == id }
            if (updatedList.size < originalSize) {
                _list.emit(updatedList)
                Logger.d(TAG, "Item removed: $id")
            }
        }
    }

    private suspend fun queryPage(offset: Int, limit: Int): List<T> =
        withContext(Dispatchers.IO) {
            val idColumn = projection.firstOrNull() ?: return@withContext emptyList()
            val queryArgs = Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "$idColumn DESC")
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            }
            val items = mutableListOf<T>()
            try {
                contentResolver.query(uri, projection, queryArgs, null)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        cursorMapper(cursor)?.let { item ->
                            items.add(item)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "queryPage failed: $e")
            }
            Logger.d(TAG, "queryPage: ${items.size} from $offset, limit $limit")
            items
        }

    private suspend fun queryNewData(minID: Long, idColumn: String): List<T> = withContext(Dispatchers.IO) {
        if (minID == -1L && _list.value.isNotEmpty()) {
            Logger.d(TAG, "queryNewData called with minID -1 but list is not empty. This might be an issue.")
            return@withContext emptyList()
        }

        val selection = "$idColumn > ?"
        val selectionArgs = arrayOf(minID.toString())
        val sortOrder = "$idColumn DESC"
        val items = mutableListOf<T>()
        try {
            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                while (cursor.moveToNext()) {
                    cursorMapper(cursor)?.let { item ->
                        items.add(item)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "queryNewData failed: $e")
        }
        Logger.d(TAG, "queryNewData (newer than $minID): ${items.size}")
        items
    }

    companion object {
        const val TAG = "ContentProviderQueryManager"
    }
}
