package com.lunacattus.service.media.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lunacattus.service.media.common.Contacts.TEST_ACTION_SPEECH_START_WAKEUP
import com.lunacattus.speech.wakeup.DUIWakeUp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TestReceiver : BroadcastReceiver() {

    @Inject
    lateinit var duiWakeUp: DUIWakeUp

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            TEST_ACTION_SPEECH_START_WAKEUP -> {
                duiWakeUp.start()
            }
        }
    }

}