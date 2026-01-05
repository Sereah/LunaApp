package com.lunacattus.service.media.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.lunacattus.logger.Logger
import com.lunacattus.service.media.R
import com.lunacattus.speech.Speech
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaService : Service() {

    @Inject
    lateinit var speech: Speech

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "onCreate.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "onStartCommand.")
        startForeground(1, buildNotification())
        collectFlow()
        return START_STICKY
    }

    override fun onDestroy() {
        Logger.d(TAG, "onDestroy.")
        scope.cancel()
        super.onDestroy()
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

    private fun collectFlow() {
        scope.launch {
        }
    }

    private fun handlePost() {

    }

    companion object {
        const val TAG = "MediaService"
    }

}