package com.lunacattus.app.connection.domain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import com.lunacattus.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.UUID

sealed interface RfcommEvent {

    data class Connected(
        val uuid: UUID,
        val address: String
    ) : RfcommEvent

    data class Disconnected(
        val uuid: UUID,
        val address: String,
        val cause: Throwable? = null
    ) : RfcommEvent

    class Data(
        val uuid: UUID,
        val address: String,
        val buffer: ByteArray,
        val length: Int
    ) : RfcommEvent

    data class Error(
        val throwable: Throwable
    ) : RfcommEvent
}


@SuppressLint("MissingPermission")
class RfcommSession(
    private val adapter: BluetoothAdapter,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Closeable {

    companion object {
        private const val TAG = "RfcommSession"
    }

    /* ========== scope ========== */

    private val scope = CoroutineScope(
        SupervisorJob() + dispatcher
    )

    /* ========== state ========== */

    private var serverSocket: BluetoothServerSocket? = null
    private var socket: BluetoothSocket? = null
    private var transportJob: Job? = null

    /* ========== flow ========== */

    private val _events = MutableSharedFlow<RfcommEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )

    val events: SharedFlow<RfcommEvent> = _events.asSharedFlow()

    /* ========== listen ========== */

    fun listen(
        serviceName: String,
        uuid: UUID
    ) {
        require(serverSocket == null) { "Already listening" }

        scope.launch {
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(
                    serviceName,
                    uuid
                )
                Logger.d(TAG, "listening: $uuid")

                val accepted = serverSocket?.accept() ?: return@launch
                socket = accepted

                val address = accepted.remoteDevice.address
                _events.emit(RfcommEvent.Connected(uuid, address))

                startTransport(uuid, accepted)

            } catch (e: Exception) {
                _events.emit(RfcommEvent.Error(e))
            }
        }
    }

    /* ========== connect ========== */

    fun connect(
        address: String,
        uuid: UUID
    ) {
        require(socket == null) { "Already connected" }

        scope.launch {
            try {
                adapter.cancelDiscovery()

                val device = adapter.getRemoteDevice(address)
                val s = device.createRfcommSocketToServiceRecord(uuid)
                Logger.d(TAG, "rfcomm connecting: $uuid")
                s.connect()
                socket = s
                Logger.d(TAG, "rfcomm connected: $uuid")

                _events.emit(RfcommEvent.Connected(uuid, address))
                startTransport(uuid, s)

            } catch (e: Exception) {
                _events.emit(RfcommEvent.Error(e))
            }
        }
    }

    /* ========== transport ========== */

    private fun startTransport(
        uuid: UUID,
        socket: BluetoothSocket
    ) {
        transportJob?.cancel()

        transportJob = scope.launch {
            Logger.d(TAG, "rfcomm startTransport: $uuid")
            val address = socket.remoteDevice.address
            val input = socket.inputStream
            val buffer = ByteArray(1024)

            try {
                while (isActive) {
                    val len = input.read(buffer)
                    Logger.d(TAG, "rfcomm read: $uuid, len: $len")
                    if (len <= 0) break

                    _events.emit(
                        RfcommEvent.Data(uuid, address, buffer, len)
                    )
                }
            } catch (e: Exception) {
                _events.emit(
                    RfcommEvent.Disconnected(uuid, address, e)
                )
            } finally {
                closeSocket()
                _events.emit(
                    RfcommEvent.Disconnected(uuid, address, null)
                )
            }
        }
    }

    /* ========== write ========== */

    fun write(data: ByteArray, offset: Int, len: Int): Boolean {
        return try {
            socket?.outputStream?.run {
                write(data, offset, len)
                flush()
                true
            } ?: false
        } catch (e: Exception) {
            scope.launch {
                _events.emit(RfcommEvent.Error(e))
            }
            false
        }
    }

    /* ========== close ========== */

    override fun close() {
        transportJob?.cancel()
        closeSocket()
        closeServer()
        scope.cancel()
    }

    private fun closeSocket() {
        try {
            socket?.close()
        } catch (_: Exception) {
        } finally {
            socket = null
        }
    }

    private fun closeServer() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        } finally {
            serverSocket = null
        }
    }
}
