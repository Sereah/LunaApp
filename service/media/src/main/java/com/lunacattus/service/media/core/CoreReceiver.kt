package com.lunacattus.service.media.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lunacattus.logger.Logger
import com.lunacattus.service.media.common.Contacts.ACTION_POWER_BUTTON_SHORT_LONG_PRESS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d(TAG, "onReceive: ${intent?.action}")
        when (intent?.action) {
            ACTION_POWER_BUTTON_SHORT_LONG_PRESS -> {
            }

            else -> {}
        }
    }

    companion object {
        const val TAG = "CoreReceiver"
    }

}