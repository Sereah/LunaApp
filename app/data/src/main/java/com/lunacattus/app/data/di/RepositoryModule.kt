package com.lunacattus.app.data.di

import com.lunacattus.app.data.repository.MediaStoreRepository
import com.lunacattus.app.data.repository.VideoRepository
import com.lunacattus.app.domain.repository.IMediaStoreRepository
import com.lunacattus.app.domain.repository.IVideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideVideoRepository(impl: VideoRepository): IVideoRepository

    @Binds
    abstract fun provideMediaStoreRepository(impl: MediaStoreRepository): IMediaStoreRepository
}