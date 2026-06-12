package com.lunacattus.conflux.di

import android.content.Context
import android.provider.Settings
import com.lunacattus.network.http.HttpManager
import com.lunacattus.network.http.IHttpClient
import com.lunacattus.network.id.RequestIdGenerator
import com.lunacattus.network.ws.IWebSocketClient
import com.lunacattus.network.ws.WebSocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.math.abs

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRequestIdGenerator(@ApplicationContext context: Context): RequestIdGenerator {
        val deviceCode = computeDeviceCode(context)
        return RequestIdGenerator(deviceCode)
    }

    @Provides
    @Singleton
    fun provideHttpClient(): IHttpClient {
        return HttpManager()
    }

    @Provides
    @Singleton
    fun provideWebSocketClient(): IWebSocketClient {
        return WebSocketManager()
    }

    private fun computeDeviceCode(context: Context): String {
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
