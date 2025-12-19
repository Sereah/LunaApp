package com.lunacattus.connection.model.bluetooth

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothA2dpSink
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothUuid
import android.os.ParcelUuid

enum class BluetoothProfileType(
    val profileId: Int,
    val displayName: String,
    val uuid: ParcelUuid,
    val stateBroadcast: String,
) {
    A2DP_SOURCE(
        BluetoothProfile.A2DP,
        "A2DP SOURCE",
        BluetoothUuid.A2DP_SOURCE,
        BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED
    ),

    A2DP_SINK(
        BluetoothProfile.A2DP_SINK,
        "A2DP SINK",
        BluetoothUuid.A2DP_SINK,
        BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED
    ),

    HEADSET(
        BluetoothProfile.HEADSET,
        "Headset",
        BluetoothUuid.HFP_AG,
        BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED
    ),

    HEADSET_CLIENT(
        BluetoothProfile.HEADSET_CLIENT,
        "Headset Client",
        BluetoothUuid.HFP,
        BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED
    )
    ;

    override fun toString(): String {
        return displayName
    }

    companion object {
        fun fromUuid(uuid: ParcelUuid): BluetoothProfileType? =
            entries.firstOrNull { uuid == it.uuid }
    }
}

@Suppress("DEPRECATION")
fun Int.toBluetoothProfileString(): String {
    return when (this) {
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
        else -> "UNKNOWN_PROFILE ($this)"
    }
}
