package com.lunacattus.conflux.di

import android.content.Context
import com.lunacattus.common.utils.isSystemSignature
import com.lunacattus.conflux.domain.bluetooth.BluetoothRepository
import com.lunacattus.conflux.domain.bluetooth.IBluetoothRepository
import com.lunacattus.conflux.domain.bluetooth.SystemBluetoothRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBluetoothRepository(
        @ApplicationContext context: Context,
        bluetoothRepository: dagger.Lazy<BluetoothRepository>,
        systemBluetoothRepository: dagger.Lazy<SystemBluetoothRepository>
    ): IBluetoothRepository {
        return if (context.isSystemSignature()) {
            systemBluetoothRepository.get()
        } else {
            bluetoothRepository.get()
        }
    }
}