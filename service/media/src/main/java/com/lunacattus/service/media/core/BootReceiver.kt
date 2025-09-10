package com.lunacattus.service.media.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lunacattus.logger.Logger
import com.lunacattus.service.media.common.Contacts.ACTION_MEDIA_SERVICE

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d(TAG, "onReceive, intent: $intent")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Intent(ACTION_MEDIA_SERVICE).apply {
                setPackage(context?.packageName)
            }.let {
                context?.startService(it)
            }
        }
    }

    companion object {
        const val TAG = "BootReceiver"
    }
}