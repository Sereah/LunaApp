package com.lunacattus.speech

import android.content.Context
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

    fun init() {
        Logger.d(TAG, "init.")
        DUILiteSDK.init(context)
        auth()
    }

    fun destroy() {
        duiWakeUp.destroy()
    }

    private fun auth() {
        val authConfig = AuthConfig().apply {
            authTimeout = 5000
            isLoadSerial = false
            isLoadMacAddress = false
            offlineProfileName = "20230920103745013"
        }
        val uploadConfig = UploadConfig().apply {
            isUploadEnable = false
        }
        val duiConfig = DUILiteConfig.Builder().apply {
            setApiKey("23bdcbea884b23bdcbea884b68bfcf87")
            setProductId("279632188")
            setProductKey("7b8fdd95562c767257979f5dd87d3318")
            setProductSecret("8f3ec6dafa59135d6e34e341bcc3a96b")
            setAuthConfig(authConfig)
            setUploadConfig(uploadConfig)
        }.create()
        DUILiteSDK.doAuth(context, duiConfig, object : DUILiteSDK.InitListener {
            override fun success() {
                Logger.d(TAG, "Auth success.")
                DUILiteSDK.setDebugMode(Log.V)
                DUILiteSDK.setNativeLogLevel(Log.V)
                _authState.value = true
                duiWakeUp.init()
            }

            override fun error(code: String?, msg: String?) {
                Logger.e(TAG, "Auth fail, code: $code, info: $msg")
                _authState.value = false
            }
        })
    }

    companion object {
        const val TAG = "Speech"
    }
}