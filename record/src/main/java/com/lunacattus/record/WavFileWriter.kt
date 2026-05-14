package com.lunacattus.record

import android.content.Context
import android.os.Environment
import com.lunacattus.common.utils.toDateTimeString
import com.lunacattus.logger.Logger
import java.io.File
import java.io.RandomAccessFile

class WavFileWriter(
    private val context: Context,
    private val sampleRate: Int,
    private val channels: Int
) {

    private var outputStream: RandomAccessFile? = null
    private var dataSize = 0

    private lateinit var finalFile: File
    private var tempFile: File? = null

    fun start() {

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)
            ?: return
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val timestamp = System.currentTimeMillis().toDateTimeString("yyyyMMdd_HHmmSS")
        // 1. 定义最终的文件名
        finalFile = File(dir, "record_$timestamp.wav")
        // 2. 创建临时文件对象
        tempFile = File(dir, "record_$timestamp.wav.temp")

        outputStream = RandomAccessFile(tempFile, "rw")
        writeWavHeader()
    }

    fun write(pcmData: ByteArray) {
        outputStream?.write(pcmData)
        dataSize += pcmData.size
    }

    fun stop() {
        outputStream?.let {
            updateWavHeader(it, dataSize)
            it.close()
        }
        outputStream = null
        tempFile?.let {
            if (it.exists()) {
                val success = it.renameTo(finalFile)
                if (!success) {
                    Logger.e("WavFileWriter", "Failed to rename temp file to wav.")
                }
            }
        }
    }

    fun getFile(): File = finalFile

    /**
     * 写占位 WAV 头
     */
    private fun writeWavHeader() {
        val header = ByteArray(44)

        val byteRate = sampleRate * channels * 2

        // RIFF
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // ChunkSize (占位)
        writeInt(header, 4, 36)

        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        writeInt(header, 16, 16) // SubChunk1Size
        writeShort(header, 20, 1) // PCM
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * 2).toShort())
        writeShort(header, 34, 16)

        // data
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        writeInt(header, 40, 0) // dataSize 占位

        outputStream?.write(header)
    }

    /**
     * 回填 WAV 头
     */
    private fun updateWavHeader(file: RandomAccessFile, dataSize: Int) {
        file.seek(4)
        file.writeIntLE(36 + dataSize)

        file.seek(40)
        file.writeIntLE(dataSize)
    }

    private fun writeInt(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xff).toByte()
        buffer[offset + 1] = (value shr 8 and 0xff).toByte()
        buffer[offset + 2] = (value shr 16 and 0xff).toByte()
        buffer[offset + 3] = (value shr 24 and 0xff).toByte()
    }

    private fun writeShort(buffer: ByteArray, offset: Int, value: Short) {
        buffer[offset] = (value.toInt() and 0xff).toByte()
        buffer[offset + 1] = (value.toInt() shr 8 and 0xff).toByte()
    }

    private fun RandomAccessFile.writeIntLE(value: Int) {
        write(
            byteArrayOf(
                (value and 0xff).toByte(),
                (value shr 8 and 0xff).toByte(),
                (value shr 16 and 0xff).toByte(),
                (value shr 24 and 0xff).toByte()
            )
        )
    }
}
