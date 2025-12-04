package com.lunacattus.app.connection.domain

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import com.lunacattus.logger.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothRepository @Inject constructor() {

    var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    fun getBluetoothProfile(): Flow<String> {
        val profiles: List<Int> = adapter.getSupportedProfiles()
        Logger.d(TAG, "profiles: $profiles")
        val list = profiles.map {
            profileIdToString(it)
        }
        return flowOf(list.joinToString(separator = "\n"))
    }

    fun getAddress(): Flow<String> {
        return flowOf(adapter.address)
    }

    fun getName(): Flow<String> {
        return flowOf(adapter.name)
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
//            BluetoothProfile.LE_AUDIO -> "LE_AUDIO"
//            BluetoothProfile.VOLUME_CONTROL -> "VOLUME_CONTROL"
//            BluetoothProfile.MCP_SERVER -> "MCP_SERVER"
//            BluetoothProfile.CSIP_SET_COORDINATOR -> "CSIP_SET_COORDINATOR"
//            BluetoothProfile.LE_AUDIO_BROADCAST -> "LE_AUDIO_BROADCAST"
//            BluetoothProfile.LE_CALL_CONTROL -> "LE_CALL_CONTROL"
//            BluetoothProfile.HAP_CLIENT -> "HAP_CLIENT"
//            BluetoothProfile.LE_AUDIO_BROADCAST_ASSISTANT -> "LE_AUDIO_BROADCAST_ASSISTANT"
//            BluetoothProfile.BATTERY -> "BATTERY"
            else -> "UNKNOWN_PROFILE ($profileId)"
        }
    }

    companion object {
        const val TAG = "BluetoothRepository"
    }
}