package com.lunacattus.conflux.domain.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lunacattus.conflux.ui.sections.media.files.MediaFileItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun getAllMusic(): List<MediaFileItem> {
        val resolver = context.contentResolver
        val list = mutableListOf<MediaFileItem>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val cursor = resolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val artistIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val mimeIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (it.moveToNext()) {
                val id = it.getLong(idIdx)
                val name = it.getString(nameIdx) ?: ""
                val artist = it.getString(artistIdx) ?: ""
                val album = it.getString(albumIdx) ?: ""
                val duration = it.getLong(durationIdx)
                val size = it.getLong(sizeIdx)
                val dateModified = it.getLong(dateIdx) * 1000 // 转毫秒
                val mimeType = it.getString(mimeIdx) ?: ""

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )

                val mediaItem = MediaItem.Builder()
                    .setMediaId(id.toString())
                    .setUri(contentUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(name)
                            .setArtist(artist)
                            .setAlbumTitle(album)
                            .setIsPlayable(true)
                            .build()
                    )
                    .build()

                list.add(
                    MediaFileItem(
                        mediaItem = mediaItem,
                        size = size,
                        duration = duration,
                        dateModified = dateModified,
                        mimeType = mimeType,
                        isLocalPrivate = false
                    )
                )
            }
        }
        return list
    }

    companion object {
        const val TAG = "MediaRepository"
    }
}