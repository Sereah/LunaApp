package com.lunacattus.record

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Environment
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingFileRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun getWavFiles(): List<File> {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS) ?: return emptyList()

        return dir.listFiles { file ->
            file.isFile && file.extension.equals("wav", ignoreCase = true)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun getWavDuration(file: File): Long {
        return try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(file.absolutePath)
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun cleanupOldRecordings(maxCount: Int) {
        RecordingFileCleaner.ensureMaxRecordings(
            context = context,
            maxCount = maxCount
        )
    }

    fun playWithSystemPlayer(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, "audio/wav")
            setData(uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
