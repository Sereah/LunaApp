package com.lunacattus.voice.vehiclecontrol.di

import com.google.gson.Gson
import com.lunacattus.voice.vehiclecontrol.adapter.manufacturer.DefaultVehicleControlAdapter
import com.lunacattus.voice.vehiclecontrol.adapter.nlu.RegexFunctionCallParser
import com.lunacattus.voice.vehiclecontrol.adapter.repository.InMemoryControlTaskRepository
import com.lunacattus.voice.vehiclecontrol.application.ControlTaskExecutorImpl
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlStateObserver
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskExecutor
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskRepository
import com.lunacattus.voice.vehiclecontrol.domain.port.FunctionCallParser
import com.lunacattus.voice.vehiclecontrol.domain.port.VehicleControlAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExecutorDispatcher

@Module
@InstallIn(SingletonComponent::class)
object VehicleControlModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideFunctionCallParser(): FunctionCallParser = RegexFunctionCallParser()

    @Provides
    @Singleton
    fun provideVehicleControlAdapter(): VehicleControlAdapter = DefaultVehicleControlAdapter()

    @Provides
    @Singleton
    fun provideControlTaskRepository(): ControlTaskRepository = InMemoryControlTaskRepository()

    @Provides
    @Singleton
    fun provideControlStateObservers(): @JvmSuppressWildcards List<ControlStateObserver> = emptyList()

    @Provides
    @ExecutorDispatcher
    fun provideExecutorDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun provideControlTaskExecutor(
        adapter: VehicleControlAdapter,
        repository: ControlTaskRepository,
        observers: @JvmSuppressWildcards List<ControlStateObserver>,
        @ExecutorDispatcher dispatcher: CoroutineDispatcher,
    ): ControlTaskExecutor = ControlTaskExecutorImpl(
        adapter = adapter,
        repository = repository,
        observers = observers,
        dispatcher = dispatcher,
    )
}
