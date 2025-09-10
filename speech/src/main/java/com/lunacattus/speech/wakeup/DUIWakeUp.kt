package com.lunacattus.speech.wakeup

import android.content.Context
import com.aispeech.AIError
import com.aispeech.common.AIConstant
import com.aispeech.export.config.AIWakeupConfig
import com.aispeech.export.engines2.AIWakeupEngine
import com.aispeech.export.intent.AIWakeupIntent
import com.aispeech.export.listeners.AIWakeupListener
import com.aispeech.lite.oneshot.OneshotCache
import com.lunacattus.common.di.IOScope
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DUIWakeUp @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IOScope private val scope: CoroutineScope,
    private val audioRecordManager: AudioRecordManager
) {

    private val wakeupIntent = AIWakeupIntent()
    private lateinit var wakeUpEngine: AIWakeupEngine

    fun init() {
        Logger.d(TAG, "init...")
        wakeUpEngine = AIWakeupEngine.createInstance()
        val wakeupConfig = AIWakeupConfig().apply {
            wakeupResource = "wakeup/wakeup_aifar_comm_20180104.bin"
        }
        wakeUpEngine.init(wakeupConfig, wakeupListener)
    }

    fun start() {
        Logger.d(TAG, "start...")
        wakeupIntent.apply {
            isUseCustomFeed = true
            setWakeupWord(wakeUpWords, floatArrayOf(0.1f))
        }
        wakeUpEngine.start(wakeupIntent)
        feedPcm()
        audioRecordManager.start()
//        readTestPcm()
    }

    fun destroy() {
        Logger.d(TAG, "destroy...")
        audioRecordManager.stop()
        wakeUpEngine.stop()
        wakeUpEngine.destroy()
    }

    private fun feedPcm() {
        Logger.d(TAG, "feedPcm...")
        scope.launch {
            try {
                audioRecordManager.byteChannel.consumeEach {
                    wakeUpEngine.feedData(it, it.size)
                }
            } catch (e: IOException) {
                Logger.e(TAG, e.toString())
            }
        }
    }

    private fun readTestPcm() {
        scope.launch {
            Logger.d(TAG, "feed...")
            var inputStream: InputStream? = null
            try {
                inputStream = context.assets.open("test.wav")
                val bytes = ByteArray(3200)
                var ret = inputStream.read(bytes)
                Logger.d(TAG, "ret: $ret")
                while (ret != -1) {
                    wakeUpEngine.feedData(bytes, ret)

                    // 延时 100ms
                    delay(100)

                    ret = inputStream.read(bytes)
                    Logger.d(TAG, "ret2: $ret")
                }
            } catch (e: IOException) {
                Logger.e(TAG, e.toString())
            } finally {
                inputStream?.close()
            }
        }
    }

    private val wakeupListener = object : AIWakeupListener {
        override fun onWakeup(
            recordId: String?,
            confidence: Double,
            wakeupWord: String?,
            jsonResult: JSONObject?
        ) {
            Logger.d(
                TAG, "onWakeUp, recordId: $recordId, confidence: $confidence, " +
                        "word: $wakeupWord, json: $jsonResult"
            )
        }

        /**
         * 低阈值唤醒，低阈值时会回调。低阈值会先于onWakeup回调，但onWakeup不一定会回调
         *
         * @param recordId   recordId
         * @param confidence 唤醒置信度
         * @param wakeupWord 唤醒词, 如唤醒失败，则返回null
         */
        override fun onPreWakeup(recordId: String?, confidence: Double, wakeupWord: String?) {
            Logger.d(TAG, "onPreWakeup")
        }

        override fun onVprintCutDataReceived(
            p0: Int,
            p1: ByteArray?,
            p2: Int
        ) {
        }

        override fun onResultDataReceived(p0: ByteArray?, p1: Int) {
        }

        override fun onRawWakeupDataReceived(p0: ByteArray?, p1: Int) {
        }

        override fun onOneshot(
            p0: String?,
            p1: OneshotCache<ByteArray?>?
        ) {
        }

        override fun onNotOneshot(p0: String?) {
        }

        override fun onReadyForSpeech() {
        }

        override fun onResultDataReceived(p0: ByteArray?, p1: Int, p2: Int) {
        }

        override fun onRawDataReceived(p0: ByteArray?, p1: Int) {
        }

        override fun onInit(status: Int) {
            Logger.d(TAG, "onInit, succuss: ${status == AIConstant.OPT_SUCCESS}")
            start()
        }

        override fun onError(error: AIError?) {
            Logger.d(TAG, "onError: $error")
        }

    }

    companion object {
        const val TAG = "DUIWakeUp"
        val wakeUpWords = arrayOf("ni hao xiao sai")
    }
}