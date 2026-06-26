package com.lunacattus.llm.di

import com.lunacattus.llm.domain.base.IBertLlm
import com.lunacattus.llm.domain.base.IGenerateLlm
import com.lunacattus.llm.domain.local.BertLLmRepository
import com.lunacattus.llm.domain.local.GenerateLLmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindLlm(impl: GenerateLLmRepository): IGenerateLlm

    @Binds
    abstract fun bindBert(impl: BertLLmRepository): IBertLlm
}