package com.lunacattus.conflux.di

import com.lunacattus.conflux.domain.llm.ILLMManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
abstract class LLMModule {

    @Binds
    @GemmaManager
    abstract fun bindGemmaManager(impl: GemmaManager): ILLMManager
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GemmaManager