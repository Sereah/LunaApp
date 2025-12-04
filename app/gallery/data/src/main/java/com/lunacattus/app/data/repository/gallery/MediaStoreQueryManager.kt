package com.lunacattus.app.data.repository.gallery

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.lunacattus.app.data.repository.ContentProviderQueryManager
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.model.GalleryImage
import com.lunacattus.app.domain.model.GalleryVideo
import com.lunacattus.app.domain.model.id
import com.lunacattus.common.util.toDateTimeString
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * Manages querying MediaStore for gallery items, using a generic ContentProviderQueryManager.
 */
class MediaStoreQueryManager @AssistedInject constructor(
    queryManagerFactory: ContentProviderQueryManager.Factory<Gallery>,
    @Assisted private val uri: Uri,
    @Assisted private val projection: Array<String>,
    @Assisted private val cursorMapper: (Cursor) -> Gallery?,
) {

    @AssistedFactory
    interface Factory {
        fun create(
            uri: Uri,
            projection: Array<String>,
            cursorMapper: (Cursor) -> Gallery?,
        ): MediaStoreQueryManager
    }

    private val queryManager: ContentProviderQueryManager<Gallery>

    init {
        // The original implementation filters out `OtherFile`. We can do that in the cursorMapper
        // before it even gets into the query manager.
        val filteredCursorMapper = { cursor: Cursor ->
            val item = cursorMapper(cursor)
            if (item is Gallery.OtherFile) null else item
        }

        queryManager = queryManagerFactory.create(
            uri = uri,
            projection = projection,
            cursorMapper = filteredCursorMapper,
            idExtractor = { gallery -> gallery.id }
        )
    }

    val list = queryManager.list
    val isLoading = queryManager.isLoading

    fun cleared() {
        queryManager.cleared()
    }

    suspend fun loadMore(pageSize: Int) {
        queryManager.loadMore(pageSize)
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
