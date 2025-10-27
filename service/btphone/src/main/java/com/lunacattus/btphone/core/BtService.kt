package com.lunacattus.btphone.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.lunacattus.btphone.manager.BtManager
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BtService : Service() {

    @Inject lateinit var btManager: BtManager

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Logger.d(TAG, "onStartCommand")
        startForeground(1, buildNotification())
        btManager.init()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun buildNotification(): Notification {
        val channelId = "bt_service_channel"
        val channel = NotificationChannel(
            channelId,
            "Bt Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        return Notification.Builder(this, channelId)
            .setContentTitle("Media Service Active")
            .setContentText("")
            .setSmallIcon(null)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val TAG = "BtService"
    }
}