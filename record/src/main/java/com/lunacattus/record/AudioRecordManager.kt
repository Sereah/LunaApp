package com.lunacattus.record

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds


@Singleton
class AudioRecordManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fileResp: RecordingFileRepository
) {

    private val sampleRate = 16_000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        channelConfig,
        audioFormat
    )

    private val bufferSize = maxOf(minBufferSize, 3200)

    private var recorder: AudioRecord? = null
    private var recordJob: Job? = null
    private var maxDurationJob: Job? = null
    private var fileJob: Job? = null

    private val _byteFlow = MutableSharedFlow<ByteArray>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val recordFlow = _byteFlow.asSharedFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var wavWriter: WavFileWriter? = null

    @Volatile
    private var maxRecordingCount: Int = DEFAULT_MAX_RECORDING_COUNT

    @Volatile
    private var maxRecordDurationMs: Long = DEFAULT_MAX_RECORD_DURATION_MS

    @SuppressLint("MissingPermission")
    fun start(saveWav: Boolean = false) {
        Logger.d(TAG, "start, saveWav: $saveWav")

        if (recordJob != null) return

        if (saveWav) {
            fileResp.cleanupOldRecordings(maxRecordingCount)
        }

        recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        recorder?.startRecording()

        if (saveWav) {
            wavWriter = WavFileWriter(
                context = context,
                sampleRate = sampleRate,
                channels = 1
            )
            wavWriter?.start()
        }

        fileJob = scope.launch {
            recordFlow.collect { data ->
                wavWriter?.write(data)
            }
        }

        recordJob = scope.launch {
            readLoop()
        }

        startMaxDurationTimerIfNeeded()

        _isRecording.value = true
    }

    @Synchronized
    fun stop() {
        Logger.d(TAG, "stop")

        maxDurationJob?.cancel()
        maxDurationJob = null

        recordJob?.cancel()
        recordJob = null

        recorder?.run {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Logger.e(TAG, "recorder stop error: $e")
            }
            release()
        }
        recorder = null

        fileJob?.cancel()
        fileJob = null

        wavWriter?.stop()
        wavWriter = null
        _isRecording.value = false
    }

    fun setMaxRecordingCount(count: Int) {
        maxRecordingCount = count.coerceAtLeast(1)
    }

    fun getMaxRecordingCount(): Int = maxRecordingCount

    fun setMaxRecordDurationMs(durationMs: Long) {
        maxRecordDurationMs = durationMs.coerceAtLeast(0L)
    }

    private fun startMaxDurationTimerIfNeeded() {
        if (maxRecordDurationMs <= 0L) return

        maxDurationJob?.cancel()

        maxDurationJob = scope.launch {
            delay(maxRecordDurationMs.milliseconds)
            Logger.i(TAG, "Max record duration reached, auto stop")
            stop()
        }
    }

    private suspend fun readLoop() {
        val buffer = ByteArray(bufferSize)

        while (recorder != null && currentCoroutineContext().isActive) {
            val read = recorder?.read(buffer, 0, buffer.size) ?: break
            if (read > 0) {
                val data = buffer.copyOf(read)

                _byteFlow.tryEmit(data)
            }
        }
    }

    companion object {
        const val TAG = "AudioRecordManager"
        private const val DEFAULT_MAX_RECORDING_COUNT = 20
        private const val DEFAULT_MAX_RECORD_DURATION_MS = 60_000L
    }
}