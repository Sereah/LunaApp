package com.lunacattus.speech.asr

import android.annotation.SuppressLint
import android.content.Context
import com.aispeech.AIError
import com.aispeech.AIResult
import com.aispeech.export.ASRMode
import com.aispeech.export.bean.BuildGrammarResult
import com.aispeech.export.config.AILocalASRConfig
import com.aispeech.export.engines2.AILocalASREngine
import com.aispeech.export.engines2.AILocalGrammarEngine
import com.aispeech.export.intent.AILocalASRIntent
import com.aispeech.export.listeners.AIASRListener
import com.aispeech.export.listeners.AILocalGrammarListener
import com.lunacattus.common.di.IOScope
import com.lunacattus.logger.Logger
import com.lunacattus.speech.record.AudioRecordManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DUIAsr @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IOScope private val ioScope: CoroutineScope,
    private val audioRecordManager: AudioRecordManager
) {

    private lateinit var engine: AILocalASREngine
    private lateinit var grammarEngine: AILocalGrammarEngine

    private val _asrState = MutableStateFlow<AsrState>(AsrState.Init)
    val asrState = _asrState.asStateFlow()

    private val _asrResult = Channel<String>(Channel.BUFFERED)
    val asrResult: ReceiveChannel<String> get() = _asrResult

    fun init() {
        Logger.d(TAG, "init...")
        grammarEngine = AILocalGrammarEngine.createInstance()
        grammarEngine.init("asr/gram/ebnfc.aicar.1.2.0_cn_en_merg.bin", gramListener)
    }

    fun start() {
        Logger.d(TAG, "start...")
        val intent = AILocalASRIntent().apply {
            pauseTime = 500
            isUseConf = true //识别结果返回阈值
            isUsePinyin = true //识别结果返回拼音
            isUseXbnfRec = true //识别结果返回语义信息
            mode = ASRMode.MODE_ASR
            isUseRealBack = true //实时返回
            isUseCustomFeed = true
            noSpeechTimeOut = 10_000 //超时10s
            useFiller = true
            vadEnable = true
        }
        engine.start(intent)
        feedData()
    }

    private fun buildGrammarRes() {
        val contactString = "张三 | 李四 | 周峰冰"
        val ebnf = EbnfUtil.generateEbnfFromAssets(
            context,
            "asr/gram/asr.xbnf",
            mapOf(
                "#CONTACTS#" to contactString
            )
        )
        grammarEngine.startBuild(ebnf, EBNF_PATH)
    }

    private fun initAsr(grammarPath: String) {
        val asrConfig = AILocalASRConfig().apply {
            acousticResources = "asr/ebnfr.aicar.1.3.0.bin" //声学资源
            netbinResource = grammarPath
            vadEnable = true
            vadResource = "vad/vad_aicar_v0.16.bin" //vad资源
        }
        engine = AILocalASREngine.createInstance()
        engine.init(asrConfig, asrListener)
    }

    private fun feedData() {
        Logger.d(TAG, "feed data...")
        ioScope.launch {
            try {
                audioRecordManager.recordFlow.collect {
                    engine.feedData(it, it.size)
                }
            } catch (e: IOException) {
                Logger.e(TAG, e.toString())
            }
        }
    }

    private val gramListener = object : AILocalGrammarListener {
        override fun onInit(status: Int) {
            Logger.d(TAG, "Grammar onInit, success: ${status == 0}")
            buildGrammarRes()
        }

        override fun onError(error: AIError?) {
            Logger.e(TAG, "Grammar onError: $error")
        }

        override fun onBuildCompleted(path: String?) {
            Logger.d(TAG, "Grammar onBuildCompleted, path: $path")
            path?.let { initAsr(it) }
        }

        override fun onBuildMultiCompleted(p0: List<BuildGrammarResult?>?) {
            Logger.d(TAG, "Grammar onBuildMultiCompleted, result: $p0")
        }

        override fun onReadyForSpeech() {
            Logger.d(TAG, "Grammar onReadyForSpeech")
        }

        override fun onResultDataReceived(byte: ByteArray?, p1: Int, p2: Int) {
            Logger.d(TAG, "Grammar onResultDataReceived")
        }

        override fun onRawDataReceived(p0: ByteArray?, p1: Int) {
            Logger.d(TAG, "Grammar onRawDataReceived")
        }
    }

    private val asrListener = object : AIASRListener {
        override fun onInit(status: Int) {
            Logger.d(TAG, "Asr onInit success: ${status == 0}")
        }

        override fun onError(p0: AIError?) {
            Logger.e(TAG, "Asr onError: $p0")
            _asrState.value = AsrState.Complete
        }

        override fun onResults(p0: AIResult?) {
            Logger.d(TAG, "Asr result: ${p0?.resultObject.toString()}")
            p0?.let { result ->
                ioScope.launch {
                    _asrResult.send(result.resultObject.toString())
                }
            }
        }

        override fun onRmsChanged(p0: Float) {
            Logger.d(TAG, "Asr onRmsChanged: $p0")
        }

        override fun onBeginningOfSpeech() {
            Logger.d(TAG, "Asr onBeginningOfSpeech")
        }

        override fun onEndOfSpeech() {
            Logger.d(TAG, "Asr onEndOfSpeech")
            _asrState.value = AsrState.Complete
        }

        override fun onRawDataReceived(p0: ByteArray?, p1: Int) {
            Logger.d(TAG, "Asr onRawDataReceived")
        }

        override fun onResultDataReceived(p0: ByteArray?, p1: Int) {
            Logger.d(TAG, "Asr onResultDataReceived")
        }

        override fun onNotOneShot() {
            Logger.d(TAG, "Asr onNotOneShot")
        }

        override fun onReadyForSpeech() {
            Logger.d(TAG, "Asr onReadyForSpeech")
            _asrState.value = AsrState.Running
        }

        override fun onResultDataReceived(p0: ByteArray?, p1: Int, p2: Int) {
            Logger.d(TAG, "Asr onResultDataReceived")
        }

    }

    companion object {
        const val TAG = "DUIAsr"

        @SuppressLint("SdCardPath")
        private const val EBNF_PATH = "/sdcard/speech/media_service.net.bin"
    }
}

sealed interface AsrState {
    data object Init: AsrState
    data object Running: AsrState
    data object Complete: AsrState
}