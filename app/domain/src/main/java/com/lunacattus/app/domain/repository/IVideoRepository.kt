package com.lunacattus.app.domain.repository

import com.lunacattus.app.domain.model.JsonVideo
import com.lunacattus.app.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface IVideoRepository {
    suspend fun getJsonVideos(): List<JsonVideo>

    suspend fun insertPlayList(video: Video)

    fun queryAllVideo(): Flow<List<Video>>
}