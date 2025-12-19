package com.lunacattus.connection.domain.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HfpProfileRepository @Inject constructor(
    private val adapter: BluetoothAdapter,
    @param:ApplicationContext val context: Context
) {

    private val _hfpProfileFlow = MutableStateFlow<BluetoothHeadset?>(null)

    private val hfpProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(p0: Int, p1: BluetoothProfile?) {
            Logger.d(TAG, "onServiceConnected")
            _hfpProfileFlow.value = p1 as BluetoothHeadset
        }

        override fun onServiceDisconnected(p0: Int) {
            Logger.d(TAG, "onServiceDisconnected")
            _hfpProfileFlow.value = null
        }
    }

    init {
        adapter.getProfileProxy(context, hfpProfileListener, BluetoothProfile.HEADSET)
    }

    suspend fun getHfpConnectState(device: BluetoothDevice): Int {
        return getHfpProfile().getConnectionState(device)
    }

    suspend fun connect(device: BluetoothDevice): Boolean {
        Logger.d(TAG, "connect ${device.address}")
        return getHfpProfile().connect(device) ?: false
    }

    suspend fun disconnect(device: BluetoothDevice): Boolean {
        Logger.d(TAG, "disconnect ${device.address}")
        return getHfpProfile().disconnect(device) ?: false
    }

    fun observeConnectState(device: BluetoothDevice): Flow<Int> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED)
                val d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                if (d?.address == device.address) {
                    trySend(state)
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED))
        trySend(getHfpConnectState(device))

        awaitClose { context.unregisterReceiver(receiver) }
    }

    private suspend fun getHfpProfile(): BluetoothHeadset {
        return _hfpProfileFlow.first { it != null }!!
    }

    companion object {
        private const val TAG = "HfpProfileRepository"
    }
}