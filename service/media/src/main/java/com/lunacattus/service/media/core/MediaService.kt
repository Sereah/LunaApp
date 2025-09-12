package com.lunacattus.service.media.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.lunacattus.common.di.IOScope
import com.lunacattus.logger.Logger
import com.lunacattus.service.media.R
import com.lunacattus.speech.Speech
import com.lunacattus.speech.SpeechAuthConfig
import com.lunacattus.speech.asr.AsrResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaService : Service() {

    @Inject
    lateinit var speech: Speech

    @Inject
    @IOScope
    lateinit var ioScope: CoroutineScope

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
        startForeground(1, buildNotification())
        handleAsrMsg()
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

    private fun buildNotification(): Notification {
        val channelId = "media_service_channel"
        val channel = NotificationChannel(
            channelId,
            "Media Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        return Notification.Builder(this, channelId)
            .setContentTitle("Media Service Active")
            .setContentText("")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun handleAsrMsg() {
        ioScope.launch {
            speech.asrResult.collect {
                when(it) {
                    is AsrResult.Final -> {

                    }
                    is AsrResult.Partial -> {}
                }
            }
        }
    }

    private fun handlePost() {

    }

    companion object {
        const val TAG = "MediaService"
    }

}