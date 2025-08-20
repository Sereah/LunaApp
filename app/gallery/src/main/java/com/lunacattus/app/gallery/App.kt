package com.lunacattus.app.gallery

import android.app.Application
import com.lunacattus.logger.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag(baseContext.getString(R.string.app_name))
    }
}