package com.lunacattus.app.player.domain

import android.content.Context
import com.google.gson.Gson
import com.lunacattus.app.player.db.VideoDao
import com.lunacattus.app.player.model.JsonVideo
import com.lunacattus.app.player.model.JsonVideos
import com.lunacattus.app.player.model.Video
import com.lunacattus.app.player.model.mapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val gson: Gson,
    private val context: Context,
    private val dao: VideoDao
) {

    suspend fun getJsonVideos(): List<JsonVideo> {
        val json = context.assets.open("videos.json")
        val reader = BufferedReader(InputStreamReader(json))
        val jsonString = reader.use { it.readText() }
        val videos = gson.fromJson(jsonString, JsonVideos::class.java).videos
        return videos
    }

    suspend fun insertPlayList(video: Video) {
        dao.insertPlayList(video.mapper())
    }

    fun queryAllVideo(): Flow<List<Video>> {
        return dao.queryAllVideo().map { list -> list.map { it.mapper() } }
    }

    suspend fun deleteVideo(video: Video) {
        dao.deleteVideo(video.id)
    }
}