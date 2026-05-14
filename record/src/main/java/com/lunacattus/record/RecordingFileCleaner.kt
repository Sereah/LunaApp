package com.lunacattus.record

import android.content.Context
import android.os.Environment

object RecordingFileCleaner {

    fun ensureMaxRecordings(
        context: Context,
        maxCount: Int = 20
    ) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)
            ?: return

        val wavFiles = dir.listFiles()
            ?.filter { it.isFile && it.extension.equals("wav", true) }
            ?: return

        if (wavFiles.size <= maxCount - 1) return

        // 按修改时间升序（最旧的在前）
        val sorted = wavFiles.sortedBy { it.lastModified() }

        val needDelete = wavFiles.size - maxCount + 1

        sorted.take(needDelete).forEach { file ->
            runCatching {
                file.delete()
            }
        }
    }
}
