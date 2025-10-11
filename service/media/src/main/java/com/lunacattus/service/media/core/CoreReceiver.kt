package com.lunacattus.service.media.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lunacattus.logger.Logger
import com.lunacattus.service.media.common.Contacts.ACTION_POWER_BUTTON_SHORT_LONG_PRESS
import com.lunacattus.speech.wakeup.DUIWakeUp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CoreReceiver : BroadcastReceiver() {

    @Inject lateinit var duiWakeUp: DUIWakeUp

    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d(TAG, "onReceive: ${intent?.action}")
        when (intent?.action) {
            ACTION_POWER_BUTTON_SHORT_LONG_PRESS -> {
                duiWakeUp.wakeUp()
            }

            else -> {}
        }
    }

    companion object {
        const val TAG = "CoreReceiver"
    }

}