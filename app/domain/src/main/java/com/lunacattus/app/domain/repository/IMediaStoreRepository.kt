package com.lunacattus.app.domain.repository

interface IMediaStoreRepository {
    suspend fun queryAllPic()
}