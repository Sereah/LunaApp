package com.lunacattus.app.connection.domain

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAvrcpPlayerSettings.STATE_OFF
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BluetoothRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val _uuids = MutableStateFlow<List<String>>(emptyList())
    val uuids = _uuids.asStateFlow()
    private val _state = MutableStateFlow(STATE_OFF)
    val state = _state.asStateFlow()
    private val _isDiscovery = MutableStateFlow(false)
    val isDiscovery = _isDiscovery.asStateFlow()
    private val _foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundDevices = _foundDevices.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    _state.value = state
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isDiscovery.value = true
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isDiscovery.value = false
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Logger.d(TAG, "device: $device")
                    if (device != null && device.name?.isNotEmpty() == true) {
                        _foundDevices.update { list ->
                            val idx = list.indexOfFirst { it.address == device.address }
                            if (idx >= 0) {
                                list.toMutableList().apply { set(idx, device) }
                            } else {
                                list + device
                            }
                        }
                    }

                }

                BluetoothDevice.ACTION_UUID -> {
                    val uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                        ?.map { it.toString() }
                    Logger.d(TAG, "uuids: $uuids")
                    _uuids.value = uuids ?: emptyList()
                }
            }
        }
    }

    init {
        Logger.d(TAG, "init, bt state: ${adapter.state}")
        _state.value = adapter.state
        IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_UUID)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }.let {
            ContextCompat.registerReceiver(
                context,
                receiver,
                it,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    fun switchEnable(enable: Boolean) {
        Logger.d(TAG, "switchEnable: $enable")
        if (enable) {
            adapter.enable()
        } else {
            adapter.disable()
        }
    }

    fun discoveryDevices(enable: Boolean) {
        Logger.d(TAG, "discoveryDevices: $enable")
        _foundDevices.value = emptyList()
        if (enable) {
            adapter.startDiscovery()
        } else {
            adapter.cancelDiscovery()
        }
    }

    suspend fun pairDevice(device: BluetoothDevice): Boolean {
        Logger.d(TAG, "pairDevice: $device")
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            return true
        }

        return suspendCancellableCoroutine { continuation ->
            val bondStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
                    val receivedDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (receivedDevice?.address != device.address) return
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    when (bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            if (continuation.isActive) {
                                continuation.resume(true)
                            }
                            context.unregisterReceiver(this)
                        }
                        BluetoothDevice.BOND_NONE -> {
                            if (continuation.isActive) {
                                continuation.resume(false)
                            }
                            context.unregisterReceiver(this)
                        }
                    }
                }
            }

            context.registerReceiver(
                bondStateReceiver,
                IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            )

            continuation.invokeOnCancellation {
                try {
                    context.unregisterReceiver(bondStateReceiver)
                } catch (e: IllegalArgumentException) { }
            }

            if (!device.createBond()) {
                if (continuation.isActive) {
                    continuation.resume(false)
                }
                context.unregisterReceiver(bondStateReceiver)
            }
        }
    }

    fun getBondedDevices(): List<BluetoothDevice> {
        val bondedDevices = adapter.bondedDevices
        Logger.d(TAG, "bondedDevices: $bondedDevices")
        return bondedDevices.toList()
    }

    fun getBluetoothProfile(): String {
        val profiles: List<Int> = adapter.getSupportedProfiles()
        Logger.d(TAG, "profiles: $profiles")
        val list = profiles.map {
            profileIdToString(it)
        }
        return list.joinToString(separator = "\n")
    }

    fun getAddress(): String {
        return adapter.address
    }

    fun getName(): String {
        return adapter.name
    }

    private fun profileIdToString(profileId: Int): String {
        return when (profileId) {
            BluetoothProfile.HEADSET -> "HEADSET"
            BluetoothProfile.A2DP -> "A2DP"
            BluetoothProfile.HEALTH -> "HEALTH"
            BluetoothProfile.HID_HOST -> "HID_HOST"
            BluetoothProfile.PAN -> "PAN"
            BluetoothProfile.PBAP -> "PBAP"
            BluetoothProfile.GATT -> "GATT"
            BluetoothProfile.GATT_SERVER -> "GATT_SERVER"
            BluetoothProfile.MAP -> "MAP"
            BluetoothProfile.SAP -> "SAP"
            BluetoothProfile.A2DP_SINK -> "A2DP_SINK"
            BluetoothProfile.AVRCP_CONTROLLER -> "AVRCP_CONTROLLER"
            BluetoothProfile.AVRCP -> "AVRCP"
            BluetoothProfile.HEADSET_CLIENT -> "HEADSET_CLIENT"
            BluetoothProfile.PBAP_CLIENT -> "PBAP_CLIENT"
            BluetoothProfile.MAP_CLIENT -> "MAP_CLIENT"
            BluetoothProfile.HID_DEVICE -> "HID_DEVICE"
            BluetoothProfile.OPP -> "OPP"
            BluetoothProfile.HEARING_AID -> "HEARING_AID"
            BluetoothProfile.LE_AUDIO -> "LE_AUDIO"
            BluetoothProfile.VOLUME_CONTROL -> "VOLUME_CONTROL"
            BluetoothProfile.MCP_SERVER -> "MCP_SERVER"
            BluetoothProfile.CSIP_SET_COORDINATOR -> "CSIP_SET_COORDINATOR"
            BluetoothProfile.LE_AUDIO_BROADCAST -> "LE_AUDIO_BROADCAST"
            BluetoothProfile.LE_CALL_CONTROL -> "LE_CALL_CONTROL"
            BluetoothProfile.HAP_CLIENT -> "HAP_CLIENT"
            BluetoothProfile.LE_AUDIO_BROADCAST_ASSISTANT -> "LE_AUDIO_BROADCAST_ASSISTANT"
            BluetoothProfile.BATTERY -> "BATTERY"
            else -> "UNKNOWN_PROFILE ($profileId)"
        }
    }

    companion object {
        const val TAG = "BluetoothRepository"
    }
}