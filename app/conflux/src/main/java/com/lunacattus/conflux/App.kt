package com.lunacattus.conflux

import android.app.Application
import android.content.Context
import com.lunacattus.logger.Logger
import com.lunacattus.speech.util.ReceiverSafeContext
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag("Conflux", true)
    }

    override fun attachBaseContext(base: Context) {
        //aar包中动态注册广播没有添加 RECEIVER_EXPORTED flag的解决方案
        super.attachBaseContext(ReceiverSafeContext(base))
    }
}