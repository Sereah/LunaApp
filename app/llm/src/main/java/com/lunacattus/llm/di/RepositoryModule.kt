package com.lunacattus.llm.di

import com.lunacattus.llm.domain.base.IBertLlm
import com.lunacattus.llm.domain.base.IGenerateLlm
import com.lunacattus.llm.domain.local.BertLLmRepository
import com.lunacattus.llm.domain.local.GenerateLLmRepository
import com.lunacattus.llm.domain.local.OnnxLlmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindLlm(impl: GenerateLLmRepository): IGenerateLlm

    @Binds
    @LLAMA
    abstract fun bindBert(impl: BertLLmRepository): IBertLlm

    @Binds
    @ONNX
    abstract fun bindOnnx(impl: OnnxLlmRepository): IBertLlm
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ONNX

@Qualifier
@Retention
annotation class LLAMA