package com.lunacattus.conflux.domain.media

import android.content.Context
import android.provider.MediaStore
import com.lunacattus.conflux.ui.sections.media.files.MediaFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun getAllMusic(): List<MediaFile> {
        val resolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )

        val cursor = resolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )

        val list = mutableListOf<MediaFile>()

        cursor?.use {
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val path = it.getString(pathIndex)
                val duration = it.getLong(durationIndex)

                list.add(
                    MediaFile(
                        file = File(path),
                        duration = duration
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