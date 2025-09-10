package com.lunacattus.app.media

import android.app.Application
import com.lunacattus.logger.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MediaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag(getString(R.string.app_name))
        Logger.d(TAG, "init.")
    }

    companion object {
        const val TAG = "MediaApp"
    }
}