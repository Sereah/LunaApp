package com.lunacattus.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.lunacattus.app.domain.model.Video
import com.lunacattus.app.domain.model.Videos
import com.lunacattus.app.domain.repository.IVideoRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val gson: Gson,
    private val context: Context
) : IVideoRepository {

    override suspend fun getJsonVideos(): List<Video> {
        val json = context.assets.open("videos.json")
        val reader = BufferedReader(InputStreamReader(json))
        val jsonString = reader.use { it.readText() }
        val videos = gson.fromJson(jsonString, Videos::class.java).videos
        return videos
    }
}