package com.lunacattus.voice.vehiclecontrol.adapter.keepalive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

/**
 * 前台保活服务。
 *
 * 通过前台通知确保进程不被系统杀死。
 * API 31+ 使用 FOREGROUND_SERVICE_TYPE_SPECIAL_USE。
 */
@AndroidEntryPoint
class KeepAliveService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "KeepAliveService onCreate")
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Logger.d(TAG, "KeepAliveService onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "KeepAliveService onDestroy")
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "车控服务",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "车辆语音控制后台服务"
        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        // 无启动 Activity，使用空 PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("车控服务运行中")
            .setContentText("车辆语音控制后台服务")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val TAG = "KeepAliveService"
        private const val CHANNEL_ID = "vehicle_control_keepalive"
        private const val NOTIFICATION_ID = 1001
    }
}
