package com.lunacattus.app.domain.repository

import com.lunacattus.app.domain.model.Video

interface IVideoRepository {
    suspend fun getJsonVideos(): List<Video>
}