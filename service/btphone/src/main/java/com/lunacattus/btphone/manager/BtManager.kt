package com.lunacattus.btphone.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothHeadsetClientCall
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BtManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var headsetClient: BluetoothHeadsetClient? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            Logger.d(TAG, "onReceive, intent: $intent")
            when (intent?.action) {
                BluetoothHeadsetClient.ACTION_CALL_CHANGED -> {
                    val call = intent.getParcelableExtra<BluetoothHeadsetClientCall>(
                        BluetoothHeadsetClient.EXTRA_CALL
                    )
                    Logger.d(TAG, "call: $call")
                }
            }
        }
    }

    private val listener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(
            profile: Int,
            proxy: BluetoothProfile?
        ) {
            if (profile == BluetoothProfile.HEADSET_CLIENT) {
                headsetClient = proxy as BluetoothHeadsetClient
                val devices = headsetClient?.connectedDevices
                Logger.d(TAG, "BluetoothHeadsetClient devices: $devices")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            headsetClient = null
        }

    }

    fun init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.getProfileProxy(context, listener, BluetoothProfile.HEADSET_CLIENT)
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothHeadsetClient.ACTION_CALL_CHANGED)
        }
        context.registerReceiver(receiver, intentFilter)
    }

    companion object {
        const val TAG = "BtManager"
    }
}