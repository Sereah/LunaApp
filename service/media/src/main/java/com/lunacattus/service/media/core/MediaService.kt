package com.lunacattus.service.media.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.lunacattus.logger.Logger
import com.lunacattus.speech.Speech
import com.lunacattus.speech.SpeechAuthConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaService : Service() {

    @Inject
    lateinit var speech: Speech

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "onCreate.")
        speech.init(
            SpeechAuthConfig(
                apiKey = "734458de88dd734458de88dd68c0e6e7",
                productId = "279632188",
                productKey = "7b8fdd95562c767257979f5dd87d3318",
                productSecret = "8f3ec6dafa59135d6e34e341bcc3a96b"
            )
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "onStartCommand.")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy.")
        speech.destroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        const val TAG = "MediaService"
    }

}