package com.lunacattus.app.connection.domain.bluetooth

import android.annotation.SuppressLint
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
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
@SuppressLint("MissingPermission")
class BluetoothRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val adapter: BluetoothAdapter
) {
    private val _state = MutableStateFlow(STATE_OFF)
    val state = _state.asStateFlow()
    private val _isDiscovery = MutableStateFlow(false)
    val isDiscovery = _isDiscovery.asStateFlow()
    private val _foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundDevices = _foundDevices.asStateFlow()
    private val executor = Executors.newSingleThreadExecutor()

    val deviceConnectStateChange: Flow<Pair<String, Boolean>> = callbackFlow {

        val callback = object : BluetoothAdapter.BluetoothConnectionCallback() {
            override fun onDeviceConnected(device: BluetoothDevice) {
                Logger.d(TAG, "onDeviceConnected: $device")
                trySend(device.address to true)
            }

            override fun onDeviceDisconnected(
                device: BluetoothDevice,
                reason: Int
            ) {
                Logger.d(TAG, "onDeviceDisconnected: $device, reason: $reason")
                trySend(device.address to false)
            }
        }

        adapter.registerBluetoothConnectionCallback(executor, callback)

        awaitClose {
            Logger.d(TAG, "deviceConnectStateChange callback awaitClose")
            adapter.unregisterBluetoothConnectionCallback(callback)
        }
    }

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
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
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
            }
        }
    }

    init {
        Logger.d(TAG, "init, bt state: ${adapter.state}")
        _state.value = adapter.state
        IntentFilter().apply {
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

    @Suppress("DEPRECATION")
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
        if (device.isBonded()) return true

        return suspendCancellableCoroutine { continuation ->
            val receiver = createBondStateReceiver(device, continuation)

            context.registerReceiver(
                receiver,
                IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            )
            continuation.invokeOnCancellation { context.unregisterReceiver(receiver) }

            if (!device.createBond()) {
                context.unregisterReceiver(receiver)
            }
        }
    }

    fun fetchUuids(device: BluetoothDevice): Boolean {
        Logger.d(TAG, "fetchUuids: $device")
        return device.fetchUuidsWithSdp()
    }

    suspend fun asyncDeviceUuids(device: BluetoothDevice): List<String> {
        return suspendCancellableCoroutine { continuation ->
            val receiver = createUuidReceiver(device, continuation)
            context.registerReceiver(
                receiver,
                IntentFilter(BluetoothDevice.ACTION_UUID)
            )
            if (!device.fetchUuidsWithSdp()) {
                context.unregisterReceiver(receiver)
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

    fun disconnectDevice(device: BluetoothDevice) {
        device.disconnect()
    }

    fun connectDevice(device: BluetoothDevice) {
        device.connect()
    }

    fun forgetDevice(device: BluetoothDevice) {
        device.removeBond()
    }

    fun connectRfcomm(address: String, uuid: UUID): Flow<RfcommEvent> = channelFlow {
        val session = RfcommSession(adapter)

        val job = launch {
            session.events.collect { send(it) }
        }

        session.connect(address, uuid)

        awaitClose {
            job.cancel()
            session.close()
        }
    }


    @Suppress("DEPRECATION")
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

    private fun BluetoothDevice.isBonded(): Boolean {
        return bondState == BluetoothDevice.BOND_BONDED
    }

    private fun createBondStateReceiver(
        target: BluetoothDevice,
        continuation: CancellableContinuation<Boolean>
    ): BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (!isTargetBondChange(intent, target)) return

            handleBondState(
                intent.getIntExtra(
                    BluetoothDevice.EXTRA_BOND_STATE,
                    BluetoothDevice.ERROR
                ),
                continuation,
                this
            )
        }
    }

    private fun createUuidReceiver(
        target: BluetoothDevice,
        continuation: CancellableContinuation<List<String>>
    ): BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val uuids = intent.getParcelableArrayExtra(
                BluetoothDevice.EXTRA_UUID,
                Array<UUID>::class.java
            )?.map { it.toString() } ?: emptyList()
            val device = intent.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
            if (uuids.isNotEmpty() && target.address == device?.address && continuation.isActive) {
                continuation.resume(uuids)
                context.unregisterReceiver(this)
            }
        }
    }

    private fun isTargetBondChange(
        intent: Intent,
        target: BluetoothDevice
    ): Boolean {
        if (intent.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return false

        val device = intent.getParcelableExtra(
            BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
        )
        return device?.address == target.address
    }

    private fun handleBondState(
        bondState: Int,
        continuation: CancellableContinuation<Boolean>,
        receiver: BroadcastReceiver
    ) {
        when (bondState) {
            BluetoothDevice.BOND_BONDED -> resumeAndUnregister(continuation, receiver, true)
            BluetoothDevice.BOND_NONE -> resumeAndUnregister(continuation, receiver, false)
        }
    }

    private fun resumeAndUnregister(
        continuation: CancellableContinuation<Boolean>,
        receiver: BroadcastReceiver,
        result: Boolean
    ) {
        if (continuation.isActive) {
            continuation.resume(result)
        }
        context.unregisterReceiver(receiver)
    }

    companion object {
        const val TAG = "BluetoothRepository"
    }
}