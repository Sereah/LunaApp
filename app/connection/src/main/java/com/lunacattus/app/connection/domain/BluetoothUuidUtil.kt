package com.lunacattus.app.connection.domain

import android.bluetooth.BluetoothUuid
import android.os.ParcelUuid

private val UUID_TO_NAME_MAP: Map<ParcelUuid, String> by lazy {
    mapOf(
        BluetoothUuid.A2DP_SINK to "A2DP_SINK",
        BluetoothUuid.A2DP_SOURCE to "A2DP_SOURCE",
        BluetoothUuid.ADV_AUDIO_DIST to "ADV_AUDIO_DIST",
        BluetoothUuid.HSP to "HSP",
        BluetoothUuid.HSP_AG to "HSP_AG",
        BluetoothUuid.HFP to "HFP",
        BluetoothUuid.HFP_AG to "HFP_AG",
        BluetoothUuid.AVRCP to "AVRCP",
        BluetoothUuid.AVRCP_CONTROLLER to "AVRCP_CONTROLLER",
        BluetoothUuid.AVRCP_TARGET to "AVRCP_TARGET",
        BluetoothUuid.OBEX_OBJECT_PUSH to "OBEX_OBJECT_PUSH",
        BluetoothUuid.HID to "HID",
        BluetoothUuid.HOGP to "HOGP",
        BluetoothUuid.PANU to "PANU",
        BluetoothUuid.NAP to "NAP",
        BluetoothUuid.BNEP to "BNEP",
        BluetoothUuid.PBAP_PCE to "PBAP_PCE",
        BluetoothUuid.PBAP_PSE to "PBAP_PSE",
        BluetoothUuid.MAP to "MAP",
        BluetoothUuid.MNS to "MNS",
        BluetoothUuid.MAS to "MAS",
        BluetoothUuid.SAP to "SAP",
        BluetoothUuid.HEARING_AID to "HEARING_AID",
        BluetoothUuid.HAS to "HAS",
        BluetoothUuid.LE_AUDIO to "LE_AUDIO",
        BluetoothUuid.DIP to "DIP",
        BluetoothUuid.VOLUME_CONTROL to "VOLUME_CONTROL",
        BluetoothUuid.GENERIC_MEDIA_CONTROL to "GENERIC_MEDIA_CONTROL",
        BluetoothUuid.MEDIA_CONTROL to "MEDIA_CONTROL",
        BluetoothUuid.COORDINATED_SET to "COORDINATED_SET",
        BluetoothUuid.CAP to "CAP",
        BluetoothUuid.BATTERY to "BATTERY",
        BluetoothUuid.BASS to "BASS",
        BluetoothUuid.TMAP to "TMAP",
        BluetoothUuid.BASE_UUID to "BASE_UUID"
    )
}

/**
 * 为 ParcelUuid 添加一个扩展函数，用于获取带 Profile 名称的字符串。
 * 例如: "0000110b-0000-1000-8000-00805f9b34fb(A2DP_SINK)"
 */
fun ParcelUuid.toProfileString(): String {
    val name = UUID_TO_NAME_MAP[this]
    return if (name != null) {
        "$name: $this"
    } else {
        "UNKNOWN: $this"
    }
}