package com.lunacattus.speech.record

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.lunacattus.common.di.IOScope
import com.lunacattus.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecordManager @Inject constructor(
    @param:IOScope private val scope: CoroutineScope
) {

    private val minBufferSize = AudioRecord.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    private val bufferSize = 3200.coerceAtLeast(minBufferSize)
    private lateinit var recorder: AudioRecord
    private var isRecording = false

    private val _channel = Channel<ByteArray>(Channel.Factory.CONFLATED)
    val byteChannel: ReceiveChannel<ByteArray> get() = _channel

    @SuppressLint("MissingPermission")
    fun start() {
        Logger.d(TAG, "start, bufferSize: $bufferSize")
        recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        recorder.startRecording()
        isRecording = true
        readBytes()
    }

    fun stop() {
        isRecording = false
        recorder.stop()
        recorder.release()
    }

    private fun readBytes() {
        val byteBuffer = ByteArray(bufferSize)
        scope.launch {
            while (isRecording) {
                val bytes = recorder.read(byteBuffer, 0, byteBuffer.size)
                if (bytes > 0) {
                    _channel.send(byteBuffer.copyOf(bytes))
                } else {
                    delay(100)
                }
            }
        }
    }

    companion object {
        const val TAG = "AudioRecordManager"
    }
}