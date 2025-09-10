package com.lunacattus.speech.tts

import android.annotation.SuppressLint
import com.aispeech.AIError
import com.aispeech.common.AIConstant
import com.aispeech.export.config.AILocalTTSConfig
import com.aispeech.export.engines2.AILocalTTSEngine
import com.aispeech.export.intent.AILocalTTSIntent
import com.aispeech.export.listeners.AILocalTTSListener
import com.lunacattus.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DUITts @Inject constructor() {

    private lateinit var engine: AILocalTTSEngine

    @SuppressLint("SdCardPath")
    fun init() {
        Logger.d(TAG, "init.")
        val ttsConfig = AILocalTTSConfig().apply {
            useCache = false //缓存音频文件到目录
            enableOptimization = true
            addSpeakerResource(
                arrayOf("tts/cjhaof_lstm_210920.bin")
            ) //合成音色资源
            setDictResource(
                "tts/v2.1.65_aitts_sent_dict_local.db"
            ) //普通话字典
            setFrontBinResource(
                "tts/v2.1.65_local_front.bin"
            ) //普通话
            language = 0 //0中文 4粤语 5英语 6法语 7泰语
        }
        engine = AILocalTTSEngine.createInstance()
        engine.init(ttsConfig, ttsListener)
    }

    fun start(text: String) {
        val ttsIntent = AILocalTTSIntent().apply {
            speed = 0.85f  //语速
            useSSML = false //使用ssml语法
            volume = 300   // 设置合成音频的音量，范围为1～500
            setLmargin(10) //设置头部静音段，范围5-20
            setRmargin(10) //设置尾部静音段，范围5-20
            setSaveAudioFilePath("/sdcard/aispeech/tts")
            setUseTimeStamp(true)
            setSleepTime(300)
        }
        engine.isUseCache = false
        engine.speak(ttsIntent, text, "1024")
    }

    private val ttsListener = object : AILocalTTSListener {
        override fun onInit(status: Int) {
            Logger.d(TAG, "onInit success: ${status == AIConstant.OPT_SUCCESS}")
        }

        override fun onError(utteranceId: String?, error: AIError?) {
            Logger.e(TAG, "onError, $error")
        }

        override fun onSynthesizeStart(utteranceId: String?) {
            Logger.d(TAG, "onSynthesizeStart...")
        }

        override fun onSynthesizeDataArrived(utteranceId: String?, audioData: ByteArray?) {
            Logger.d(TAG, "onSynthesizeDataArrived, pcm data size: ${audioData?.size}")
        }

        override fun onSynthesizeFinish(utteranceId: String?) {
            Logger.d(TAG, "onSynthesizeFinish...")
        }

        override fun onSpeechStart(utteranceId: String?) {
            Logger.d(TAG, "onSpeechStart...")
        }

        override fun onSpeechProgress(
            currentTime: Int,
            totalTime: Int,
            isRefTextTTSFinished: Boolean
        ) {
            Logger.d(
                TAG, "onSpeechProgress, progress: $currentTime/$totalTime, " +
                        "isRef: $isRefTextTTSFinished"
            )
        }

        override fun onSpeechFinish(utteranceId: String?) {
            Logger.d(TAG, "onSpeechFinish")
        }

        override fun onTimestampReceived(bytes: ByteArray?, size: Int) {
            Logger.d(TAG, "onTimestampReceived, size: $size")
        }

    }

    companion object {
        const val TAG = "DUITts"
    }
}