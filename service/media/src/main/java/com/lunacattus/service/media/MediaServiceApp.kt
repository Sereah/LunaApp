package com.lunacattus.service.media

import android.annotation.SuppressLint
import android.app.Application
import android.content.IntentFilter
import com.lunacattus.logger.Logger
import com.lunacattus.service.media.common.Contacts.ACTION_POWER_BUTTON_SHORT_LONG_PRESS
import com.lunacattus.service.media.common.Contacts.TEST_ACTION_SPEECH_START_ASR
import com.lunacattus.service.media.common.Contacts.TEST_ACTION_SPEECH_START_WAKEUP
import com.lunacattus.service.media.common.TestReceiver
import com.lunacattus.service.media.core.CoreReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MediaServiceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag(getString(R.string.app_name))
        Logger.d(TAG, "onCreate.")
        initBroadcast()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun initBroadcast() {
        val testFilter = IntentFilter().apply {
            addAction(TEST_ACTION_SPEECH_START_WAKEUP)
            addAction(TEST_ACTION_SPEECH_START_ASR)
        }
        val coreFilter = IntentFilter().apply {
            addAction(ACTION_POWER_BUTTON_SHORT_LONG_PRESS)
        }
        registerReceiver(TestReceiver(), testFilter)
        registerReceiver(CoreReceiver(), coreFilter)
    }

    companion object {
        const val TAG = "MediaServiceApp"
    }
}