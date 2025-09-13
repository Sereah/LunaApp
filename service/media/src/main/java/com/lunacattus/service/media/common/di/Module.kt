package com.lunacattus.service.media.common.di

import android.bluetooth.BluetoothAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(): BluetoothAdapter {
        return BluetoothAdapter.getDefaultAdapter()
    }

}