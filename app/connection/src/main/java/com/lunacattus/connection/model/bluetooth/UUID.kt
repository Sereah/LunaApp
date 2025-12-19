package com.lunacattus.connection.model.bluetooth

import android.bluetooth.BluetoothUuid
import android.os.ParcelUuid

const val CarplayDeviceUUID = "00000000-DECA-FADE-DECA-DEAFDECACAFE"
const val CarplayHostUUID = "00000000-DECA-FADE-DECA-DEAFDECACAFF"

enum class BluetoothUuidCategory {
    COMMON,
    VENDOR,
    UNKNOWN
}

data class BluetoothUuidMeta(
    val name: String,
    val category: BluetoothUuidCategory,
)

private val UUID_META_MAP: Map<ParcelUuid, BluetoothUuidMeta> by lazy {
    mapOf(
        // -------- Audio --------
        BluetoothUuid.A2DP_SINK to BluetoothUuidMeta(
            name = "A2DP_SINK",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.A2DP_SOURCE to BluetoothUuidMeta(
            name = "A2DP_SOURCE",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.ADV_AUDIO_DIST to BluetoothUuidMeta(
            name = "ADV_AUDIO_DIST",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.AVRCP to BluetoothUuidMeta(
            name = "AVRCP",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.AVRCP_CONTROLLER to BluetoothUuidMeta(
            name = "AVRCP_CONTROLLER",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.AVRCP_TARGET to BluetoothUuidMeta(
            name = "AVRCP_TARGET",
            category = BluetoothUuidCategory.COMMON
        ),

        // -------- Telephony --------
        BluetoothUuid.HFP to BluetoothUuidMeta(
            name = "HFP",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.HFP_AG to BluetoothUuidMeta(
            name = "HFP_AG",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.HSP to BluetoothUuidMeta(
            name = "HSP",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.HSP_AG to BluetoothUuidMeta(
            name = "HSP_AG",
            category = BluetoothUuidCategory.COMMON
        ),

        // -------- Input --------
        BluetoothUuid.HID to BluetoothUuidMeta(
            name = "HID",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.HOGP to BluetoothUuidMeta(
            name = "HOGP",
            category = BluetoothUuidCategory.COMMON
        ),

        // -------- Data / Network --------
        BluetoothUuid.OBEX_OBJECT_PUSH to BluetoothUuidMeta(
            name = "OBEX_OBJECT_PUSH",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.PANU to BluetoothUuidMeta(
            name = "PANU",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.NAP to BluetoothUuidMeta(
            name = "NAP",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.BNEP to BluetoothUuidMeta(
            name = "BNEP",
            category = BluetoothUuidCategory.COMMON
        ),

        // -------- Phonebook / Message --------
        BluetoothUuid.PBAP_PCE to BluetoothUuidMeta(
            name = "PBAP_PCE",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.PBAP_PSE to BluetoothUuidMeta(
            name = "PBAP_PSE",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.MAP to BluetoothUuidMeta(
            name = "MAP",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.MAS to BluetoothUuidMeta(
            name = "MAS",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.MNS to BluetoothUuidMeta(
            name = "MNS",
            category = BluetoothUuidCategory.COMMON
        ),

        // -------- LE Audio / BLE --------
        BluetoothUuid.LE_AUDIO to BluetoothUuidMeta(
            name = "LE_AUDIO",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.HEARING_AID to BluetoothUuidMeta(
            name = "HEARING_AID",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.DIP to BluetoothUuidMeta(
            name = "DIP",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.BATTERY to BluetoothUuidMeta(
            name = "BATTERY",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.VOLUME_CONTROL to BluetoothUuidMeta(
            name = "VOLUME_CONTROL",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.MEDIA_CONTROL to BluetoothUuidMeta(
            name = "MEDIA_CONTROL",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.GENERIC_MEDIA_CONTROL to BluetoothUuidMeta(
            name = "GENERIC_MEDIA_CONTROL",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.COORDINATED_SET to BluetoothUuidMeta(
            name = "COORDINATED_SET",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.CAP to BluetoothUuidMeta(
            name = "CAP",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.BASS to BluetoothUuidMeta(
            name = "BASS",
            category = BluetoothUuidCategory.COMMON
        ),
        BluetoothUuid.TMAP to BluetoothUuidMeta(
            name = "TMAP",
            category = BluetoothUuidCategory.COMMON
        ),

        // -------- BaseUUID --------
        BluetoothUuid.BASE_UUID to BluetoothUuidMeta(
            name = "BASE",
            category = BluetoothUuidCategory.UNKNOWN
        ),

        // -------- Vendor --------
        ParcelUuid.fromString(CarplayHostUUID) to BluetoothUuidMeta(
            name = "CARPLAY_HOST",
            category = BluetoothUuidCategory.VENDOR
        ),
        ParcelUuid.fromString(CarplayDeviceUUID) to BluetoothUuidMeta(
            name = "CarPlay",
            category = BluetoothUuidCategory.VENDOR
        )
    )
}

fun ParcelUuid.displayName(): String =
    UUID_META_MAP[this]?.name ?: "UNKNOWN"

fun ParcelUuid.displayNameAndId(): String =
    "${UUID_META_MAP[this]?.name ?: "UNKNOWN"}: $this"

fun ParcelUuid.category(): BluetoothUuidCategory =
    UUID_META_MAP[this]?.category ?: BluetoothUuidCategory.UNKNOWN

fun ParcelUuid.isCommonUuid() =
    category() == BluetoothUuidCategory.COMMON

fun ParcelUuid.isVendorUuid() =
    category() == BluetoothUuidCategory.VENDOR

fun ParcelUuid.isUnknownUuid() =
    category() == BluetoothUuidCategory.UNKNOWN