package com.lunacattus.conflux.di

import android.content.Context
import android.provider.Settings
import com.lunacattus.conflux.domain.tts.ITts
import com.lunacattus.conflux.domain.tts.TtsRepository
import com.lunacattus.network.http.HttpManager
import com.lunacattus.network.http.IHttpClient
import com.lunacattus.network.id.DeviceCode
import com.lunacattus.network.ws.IWebSocketClient
import com.lunacattus.network.ws.WebSocketManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.math.abs

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindHttpClient(impl: HttpManager): IHttpClient

    @Binds
    @Singleton
    abstract fun bindWebSocketClient(impl: WebSocketManager): IWebSocketClient

    @Binds
    @Singleton
    abstract fun bindTts(impl: TtsRepository): ITts

    companion object {
        @Provides
        @Singleton
        @DeviceCode
        fun provideDeviceCode(@ApplicationContext context: Context): String {
            val raw = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    ?: "0000000000000000"
            } catch (e: Exception) {
                "0000000000000000"
            }
            val safe = raw.substring(0, minOf(10, raw.length))
            val code = abs(safe.toLong(16) % 1_000_000L)
            return code.toString().padStart(6, '0')
        }
    }
}
