package com.lunacattus.app.connection.domain.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HfpProfileRepository @Inject constructor(
    private val adapter: BluetoothAdapter,
    @param:ApplicationContext private val context: Context
) {

    private var profile: BluetoothProfile? = null

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(p0: Int, p1: BluetoothProfile?) {
            Logger.d(TAG, "onServiceConnected")
            profile = p1 as BluetoothHeadset
            Logger.d(TAG, "====${profile?.connectedDevices}")
        }

        override fun onServiceDisconnected(p0: Int) {
            Logger.d(TAG, "onServiceDisconnected")
            profile = null
        }
    }

    init {
        adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
    }

    companion object {
        private const val TAG = "HfpProfileRepository"
    }
}