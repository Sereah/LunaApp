package com.lunacattus.app.media

import android.app.Application
import com.lunacattus.logger.Logger
import com.lunacattus.speech.Speech
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MediaApp : Application() {

    @Inject lateinit var speech: Speech

    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag(getString(R.string.app_name))
        Logger.d(TAG, "init.")
        speech.init()
    }

    override fun onTerminate() {
        super.onTerminate()
        speech.destroy()
    }

    companion object {
        const val TAG = "MediaApp"
    }
}