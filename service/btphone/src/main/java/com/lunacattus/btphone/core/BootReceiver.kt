package com.lunacattus.btphone.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lunacattus.logger.Logger

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d(TAG, "onReceive, intent: $intent")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Intent("cn.seres.service.bt").apply {
                setPackage(context?.packageName)
            }.let {
                context?.startForegroundService(it)
            }
        }
    }

    companion object {
        const val TAG = "BootReceiver"
    }
}