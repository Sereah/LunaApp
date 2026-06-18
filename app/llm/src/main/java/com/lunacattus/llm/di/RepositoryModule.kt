package com.lunacattus.llm.di

import com.lunacattus.llm.domain.ILlm
import com.lunacattus.llm.domain.local.LlmCppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @LlmCpp
    abstract fun bindLlm(impl: LlmCppRepository): ILlm
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LlmCpp