package com.lunacattus.service.media

import android.annotation.SuppressLint
import android.app.Application
import android.content.IntentFilter
import com.lunacattus.logger.Logger
import com.lunacattus.service.media.common.TestReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MediaServiceApp : Application() {

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag(getString(R.string.app_name))
        Logger.d(TAG, "onCreate.")
        val filter = IntentFilter().apply {
            addAction("com.lunacattus.test")
        }
        registerReceiver(TestReceiver(), filter)
    }

    companion object {
        const val TAG = "MediaServiceApp"
    }
}