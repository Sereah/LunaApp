package com.lunacattus.speech

import android.content.Context
import com.aispeech.DUILiteConfig
import com.aispeech.DUILiteSDK
import com.aispeech.common.Log
import com.aispeech.export.config.AuthConfig
import com.aispeech.export.config.UploadConfig
import com.lunacattus.common.di.IOScope
import com.lunacattus.logger.Logger
import com.lunacattus.speech.asr.AsrState
import com.lunacattus.speech.asr.DUIAsr
import com.lunacattus.speech.tts.DUITts
import com.lunacattus.speech.tts.TtsState
import com.lunacattus.speech.wakeup.DUIWakeUp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Speech @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IOScope private val ioScope: CoroutineScope,
    private val duiWakeUp: DUIWakeUp,
    private val duiTts: DUITts,
    private val duiAsr: DUIAsr
) {

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Init)
    val speechState = _speechState.asStateFlow()

    private val _asrResult = MutableSharedFlow<String>()
    val asrResult = _asrResult.asSharedFlow()

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
                DUILiteSDK.setDebugMode(Log.I)
                DUILiteSDK.setNativeLogLevel(Log.I)
                DUILiteSDK.setGlobalAudioSavePath("/sdcard/aispeech")
                initComponent()
            }

            override fun error(code: String?, msg: String?) {
                Logger.e(TAG, "Auth fail, code: $code, info: $msg")
            }
        })
    }

    private fun initComponent() {
        duiWakeUp.init()
        duiTts.init()
        duiAsr.init()
        collectComponentState()
    }

    private fun collectComponentState() {
        ioScope.launch {
            launch {
                duiWakeUp.wakeUpState.collect { active ->
                    Logger.d(TAG, "collect wake up active.")
                    _speechState.update {
                        when (it) {
                            is SpeechState.Active -> it.copy(wakeupActive = true)
                            else -> SpeechState.Active(wakeupActive = true)
                        }
                    }
                    Logger.d(TAG, "===== ${_speechState.value} =====")
                    duiTts.start(context.getString(R.string.wake_up_answer))
                }
            }
            launch {
                duiTts.ttsState.collect { ttsState ->
                    Logger.d(TAG, "collect tts complete: $ttsState")
                    when (ttsState) {

                        TtsState.Running -> {
                            _speechState.update {
                                when (it) {
                                    is SpeechState.Active -> it.copy(ttsState = TtsState.Running)
                                    else -> it
                                }
                            }
                        }

                        TtsState.Complete -> {
                            _speechState.update {
                                when (it) {
                                    is SpeechState.Active -> it.copy(ttsState = TtsState.Complete)
                                    else -> it
                                }
                            }
                        }

                        else -> {}
                    }
                    Logger.d(TAG, "===== ${_speechState.value} =====")
                    //当回答时wakeup处于激活时才开始asr，避免asr的tts结束时干扰。
                    if (ttsState == TtsState.Complete && _speechState.value is SpeechState.Active) {
                        if ((_speechState.value as SpeechState.Active).wakeupActive) {
                            duiAsr.start()
                        }
                    }
                }
            }
            launch {
                duiAsr.asrState.collect { state ->
                    Logger.d(TAG, "collect asrState: $state")
                    _speechState.update {
                        when (it) {
                            is SpeechState.Active -> {
                                when (state) {
                                    AsrState.Complete -> {
                                        it.copy(
                                            wakeupActive = false,
                                            ttsState = TtsState.Init,
                                            asrState = state
                                        )
                                    }

                                    else -> it.copy(asrState = state)
                                }
                            }

                            else -> it
                        }
                    }
                    if (state == AsrState.Complete) {
                        duiTts.start(context.getString(R.string.asr_answer))
                    }
                    Logger.d(TAG, "===== ${_speechState.value} =====")
                }
            }
            launch {
                duiAsr.asrResult.consumeEach {
                    _asrResult.emit(it)
                }
            }
        }
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

sealed interface SpeechState {
    data object Init : SpeechState
    data class Active(
        val wakeupActive: Boolean = false,
        val ttsState: TtsState = TtsState.Init,
        val asrState: AsrState = AsrState.Init,
    ) : SpeechState
}