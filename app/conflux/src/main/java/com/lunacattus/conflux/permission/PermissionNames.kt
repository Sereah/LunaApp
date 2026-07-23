package com.lunacattus.conflux.permission

import androidx.compose.runtime.Composable
/** 权限名提供者：接收权限常量字符串，返回可读名称（可在 Composable 上下文中调用 stringResource 等） */
typealias PermissionNameProvider = @Composable (String) -> String

/** 权限 → 英文名映射（默认实现，使用者可通过 [PermissionNameProvider] 注入本地化版本） */
fun permissionDisplayName(permission: String): String = when (permission) {
    android.Manifest.permission.RECORD_AUDIO -> "Microphone"
    android.Manifest.permission.CAMERA -> "Camera"
    android.Manifest.permission.ACCESS_FINE_LOCATION -> "Precise Location"
    android.Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "Background Location"
    android.Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
    android.Manifest.permission.READ_MEDIA_AUDIO -> "Music & Audio"
    android.Manifest.permission.READ_MEDIA_IMAGES -> "Photos"
    android.Manifest.permission.READ_MEDIA_VIDEO -> "Videos"
    android.Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage"
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Storage"
    android.Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth"
    android.Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth Scan"
    android.Manifest.permission.BLUETOOTH_ADVERTISE -> "Bluetooth Advertise"
    android.Manifest.permission.UWB_RANGING -> "Ultra-Wideband"
    android.Manifest.permission.READ_PHONE_STATE -> "Phone State"
    android.Manifest.permission.READ_PHONE_NUMBERS -> "Phone Numbers"
    android.Manifest.permission.CALL_PHONE -> "Phone Calls"
    android.Manifest.permission.ANSWER_PHONE_CALLS -> "Answer Calls"
    android.Manifest.permission.READ_CALL_LOG -> "Call Log"
    android.Manifest.permission.WRITE_CALL_LOG -> "Call Log"
    android.Manifest.permission.ADD_VOICEMAIL -> "Voicemail"
    android.Manifest.permission.USE_SIP -> "SIP Calling"
    android.Manifest.permission.ACCEPT_HANDOVER -> "Call Handover"
    android.Manifest.permission.READ_CONTACTS -> "Contacts"
    android.Manifest.permission.WRITE_CONTACTS -> "Contacts"
    android.Manifest.permission.GET_ACCOUNTS -> "Accounts"
    android.Manifest.permission.READ_CALENDAR -> "Calendar"
    android.Manifest.permission.WRITE_CALENDAR -> "Calendar"
    android.Manifest.permission.BODY_SENSORS -> "Body Sensors"
    android.Manifest.permission.BODY_SENSORS_BACKGROUND -> "Body Sensors"
    android.Manifest.permission.ACTIVITY_RECOGNITION -> "Physical Activity"
    android.Manifest.permission.READ_SMS -> "SMS"
    android.Manifest.permission.SEND_SMS -> "SMS"
    android.Manifest.permission.RECEIVE_SMS -> "SMS"
    android.Manifest.permission.RECEIVE_WAP_PUSH -> "WAP Push"
    android.Manifest.permission.RECEIVE_MMS -> "MMS"
    android.Manifest.permission.NEARBY_WIFI_DEVICES -> "Nearby Wi-Fi Devices"
    // 特殊权限
    android.Manifest.permission.SYSTEM_ALERT_WINDOW -> "Display Overlay"
    android.Manifest.permission.WRITE_SETTINGS -> "Modify System Settings"
    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE -> "All Files Access"
    else -> permission.substringAfterLast(".")
}
