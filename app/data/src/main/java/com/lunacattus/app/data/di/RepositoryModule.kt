package com.lunacattus.app.data.di

import com.lunacattus.app.data.repository.gallery.GalleryRepository
import com.lunacattus.app.data.repository.player.VideoRepository
import com.lunacattus.app.domain.repository.IGalleryRepository
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
    abstract fun provideGalleryRepository(impl: GalleryRepository): IGalleryRepository
}