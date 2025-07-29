package com.lunacattus.app.data.di

import com.lunacattus.app.data.repository.DataRepository
import com.lunacattus.app.domain.repository.IDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideDataRepository(impl: DataRepository): IDataRepository
}