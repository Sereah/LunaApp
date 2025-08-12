package com.lunacattus.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.lunacattus.app.domain.repository.IMediaStoreRepository
import com.lunacattus.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreRepository @Inject constructor(
    private val context: Context
) : IMediaStoreRepository {

    override suspend fun queryAllPic() {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
        )

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val addDataCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol)
                val data = it.getString(addDataCol)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Logger.d(TAG, "id: $id, name: $name, data: $data uri: $contentUri")
            }
        }
    }

    companion object {
        const val TAG = "MediaStoreRepository"
    }
}