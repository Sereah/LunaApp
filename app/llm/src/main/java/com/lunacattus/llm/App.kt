package com.lunacattus.llm

import android.app.Application
import com.lunacattus.common.CommonLog
import com.lunacattus.logger.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initLog()
    }

    @Suppress("kotlin:S5324")
    private fun initLog() {
        Logger.initBaseTag("LunaLLM")
        getExternalFilesDir(null)?.let {
            Logger.initFileLogger(it)
        }
        CommonLog.setLogger(
            debug = { tag, msg ->
                Logger.d(tag, msg)
            },
            error = { tag, msg, thr ->
                Logger.e(tag, msg, thr)
            }
        )
    }
}