package com.lunacattus.speech

import android.content.Context
import android.os.Environment
import com.aispeech.DUILiteConfig
import com.aispeech.DUILiteSDK
import com.aispeech.common.Log
import com.aispeech.export.config.AuthConfig
import com.aispeech.export.config.UploadConfig
import com.lunacattus.logger.Logger
import com.lunacattus.speech.wakeup.DUIWakeUp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Speech @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val duiWakeUp: DUIWakeUp
) {

    private val _authState = MutableStateFlow(false)
    val authState = _authState.asStateFlow()

    fun init(authConfig: SpeechAuthConfig) {
        Logger.d(TAG, "init.")
        DUILiteSDK.init(context)
        auth(authConfig)
    }

    fun destroy() {
        duiWakeUp.destroy()
    }

    private fun auth(config: SpeechAuthConfig) {
        val authConfig = AuthConfig().apply {
            authTimeout = 5000
            isLoadSerial = false
            isLoadMacAddress = false
            offlineProfileName = "com.lunacattus.speech"
        }
        val uploadConfig = UploadConfig().apply {
            isUploadEnable = false
        }
        val duiConfig = DUILiteConfig.Builder().apply {
            setApiKey(config.apiKey)
            setProductId(config.productId)
            setProductKey(config.productKey)
            setProductSecret(config.productSecret)
            setAuthConfig(authConfig)
            setUploadConfig(uploadConfig)
        }.create()
        DUILiteSDK.doAuth(context, duiConfig, object : DUILiteSDK.InitListener {
            override fun success() {
                Logger.d(TAG, "Auth success.")
                DUILiteSDK.setDebugMode(Log.V)
                DUILiteSDK.setNativeLogLevel(Log.V)
                DUILiteSDK.setGlobalAudioSavePath("/sdcard/aispeech")
                _authState.value = true
                initComponent()
            }

            override fun error(code: String?, msg: String?) {
                Logger.e(TAG, "Auth fail, code: $code, info: $msg")
                _authState.value = false
            }
        })
    }

    private fun initComponent() {
        duiWakeUp.init()
    }

    companion object {
        const val TAG = "Speech"
    }
}

data class SpeechAuthConfig(
    val apiKey: String = "",
    val productId: String = "",
    val productKey: String = "",
    val productSecret: String = ""
)