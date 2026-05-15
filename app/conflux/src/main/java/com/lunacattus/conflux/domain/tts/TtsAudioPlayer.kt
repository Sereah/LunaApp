package com.lunacattus.conflux.domain.tts

import android.content.Context
import android.util.Base64
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var player: ExoPlayer? = null
    private var chunkFiles: MutableList<File> = mutableListOf()
    private var currentChunkIdx = -1
    private var currentGroupId: String? = null
    private var streamingMode = false

    val audioDir: File
        get() = File(context.filesDir, "tts_audio").also { it.mkdirs() }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playingGroupId = MutableStateFlow<String?>(null)
    val playingGroupId: StateFlow<String?> = _playingGroupId.asStateFlow()

    private val _currentChunkIndex = MutableStateFlow(-1)
    val currentChunkIndex: StateFlow<Int> = _currentChunkIndex.asStateFlow()

    private val _totalChunks = MutableStateFlow(0)
    val totalChunks: StateFlow<Int> = _totalChunks.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackVolume = MutableStateFlow(1.0f)
    val playbackVolume: StateFlow<Float> = _playbackVolume.asStateFlow()

    fun persistBase64Audio(groupId: String, chunkIndex: Int, base64: String): File {
        val file = File(audioDir, "tts_${groupId}_${chunkIndex}.wav")
        val wavBytes = Base64.decode(base64, Base64.DEFAULT)
        FileOutputStream(file).use { it.write(wavBytes) }
        return file
    }

    fun getPersistedFile(groupId: String, chunkIndex: Int): File? {
        val file = File(audioDir, "tts_${groupId}_${chunkIndex}.wav")
        return if (file.exists()) file else null
    }

    fun deleteGroupFiles(groupId: String) {
        audioDir.listFiles()?.filter { it.name.startsWith("tts_${groupId}_") }?.forEach { it.delete() }
    }

    fun clearAllFiles() {
        audioDir.listFiles()?.forEach { it.delete() }
    }

    fun playAllChunks(chunks: List<String>, groupId: String) {
        stop()
        streamingMode = false
        try {
            val files = chunks.mapIndexed { index, b64 ->
                persistBase64Audio(groupId, index, b64)
            }
            chunkFiles = files.toMutableList()
            currentChunkIdx = 0
            currentGroupId = groupId
            _playingGroupId.value = groupId
            _totalChunks.value = files.size
            _currentChunkIndex.value = 0

            val exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer.setPlaybackSpeed(_playbackSpeed.value)
            exoPlayer.setVolume(_playbackVolume.value)
            exoPlayer.addListener(createListener())
            exoPlayer.setMediaItem(MediaItem.fromUri(files[0].toURI().toString()))
            exoPlayer.prepare()
            exoPlayer.play()
            player = exoPlayer
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to play TTS chunks: ${e.message}")
            stop()
        }
    }

    fun startStreamingPlay(chunks: List<String>, groupId: String) {
        stop()
        streamingMode = true
        try {
            val files = chunks.mapIndexed { index, b64 ->
                persistBase64Audio(groupId, index, b64)
            }
            chunkFiles = files.toMutableList()
            currentChunkIdx = 0
            currentGroupId = groupId
            _playingGroupId.value = groupId
            _totalChunks.value = files.size
            _currentChunkIndex.value = 0

            val exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer.setPlaybackSpeed(_playbackSpeed.value)
            exoPlayer.setVolume(_playbackVolume.value)
            exoPlayer.addListener(createListener())
            exoPlayer.setMediaItem(MediaItem.fromUri(files[0].toURI().toString()))
            exoPlayer.prepare()
            exoPlayer.play()
            player = exoPlayer
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to start streaming play: ${e.message}")
            stop()
        }
    }

    fun appendChunk(groupId: String, chunkIndex: Int, base64: String) {
        if (currentGroupId != groupId || !streamingMode) return
        val file = persistBase64Audio(groupId, chunkIndex, base64)
        chunkFiles.add(file)
        _totalChunks.value = chunkFiles.size
        if (currentChunkIdx >= chunkFiles.size - 1 && player != null) {
            playNextChunk()
        }
    }

    fun endStreamingPlay() {
        streamingMode = false
        if (player != null && currentChunkIdx >= chunkFiles.size - 1) {
            stop()
        }
    }

    fun playSingleChunk(base64Audio: String, groupId: String) {
        playAllChunks(listOf(base64Audio), groupId)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed.coerceIn(0.25f, 4.0f)
        player?.setPlaybackSpeed(_playbackSpeed.value)
    }

    fun setVolume(volume: Float) {
        _playbackVolume.value = volume.coerceIn(0f, 1f)
        player?.setVolume(_playbackVolume.value)
    }

    fun stop() {
        streamingMode = false
        player?.stop()
        player?.release()
        player = null
        _isPlaying.value = false
        _playingGroupId.value = null
        _currentChunkIndex.value = -1
        _totalChunks.value = 0
        chunkFiles = mutableListOf()
        currentChunkIdx = -1
        currentGroupId = null
    }

    fun release() {
        stop()
    }

    private fun createListener() = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                playNextChunk()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Logger.e(TAG, "Playback error: ${error.message}")
            playNextChunk()
        }
    }

    private fun playNextChunk() {
        currentChunkIdx++
        if (currentChunkIdx < chunkFiles.size) {
            _currentChunkIndex.value = currentChunkIdx
            player?.setMediaItem(MediaItem.fromUri(chunkFiles[currentChunkIdx].toURI().toString()))
            player?.prepare()
            player?.play()
        } else if (!streamingMode) {
            stop()
        }
    }

    companion object {
        private const val TAG = "TtsAudioPlayer"
    }
}
