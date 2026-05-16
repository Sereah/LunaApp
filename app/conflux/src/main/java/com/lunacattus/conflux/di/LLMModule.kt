package com.lunacattus.conflux.di

import com.lunacattus.conflux.domain.llm.GemmaManager
import com.lunacattus.conflux.domain.llm.ILLMManager
import com.lunacattus.conflux.domain.llm.LunaLlmManager
import com.lunacattus.conflux.domain.llm.tts.ITts
import com.lunacattus.conflux.domain.llm.tts.TtsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LLMModule {

    @Binds
    @Gemma
    abstract fun bindGemmaManager(impl: GemmaManager): ILLMManager

    @Binds
    @Luna
    abstract fun bindLunaManager(impl: LunaLlmManager): ILLMManager

    @Binds
    @Singleton
    abstract fun bindTts(impl: TtsRepository): ITts
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Gemma

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Luna