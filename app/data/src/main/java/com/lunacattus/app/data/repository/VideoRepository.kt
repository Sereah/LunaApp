package com.lunacattus.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.lunacattus.app.data.VideoDao
import com.lunacattus.app.data.entity.mapper
import com.lunacattus.app.domain.model.JsonVideo
import com.lunacattus.app.domain.model.JsonVideos
import com.lunacattus.app.domain.model.Video
import com.lunacattus.app.domain.repository.IVideoRepository
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
) : IVideoRepository {

    override suspend fun getJsonVideos(): List<JsonVideo> {
        val json = context.assets.open("videos.json")
        val reader = BufferedReader(InputStreamReader(json))
        val jsonString = reader.use { it.readText() }
        val videos = gson.fromJson(jsonString, JsonVideos::class.java).videos
        return videos
    }

    override suspend fun insertPlayList(video: Video) {
        dao.insertPlayList(video.mapper())
    }

    override fun queryAllVideo(): Flow<List<Video>> {
        return dao.queryAllVideo().map { list -> list.map { it.mapper() } }
    }
}