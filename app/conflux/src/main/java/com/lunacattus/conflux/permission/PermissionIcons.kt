package com.lunacattus.conflux.permission

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector

/** 所有需要用户同意的 Runtime 权限 → 图标映射 */
val PermissionIconMap: Map<String, ImageVector> = mapOf(
    // 日历
    android.Manifest.permission.READ_CALENDAR to Icons.Rounded.DateRange,
    android.Manifest.permission.WRITE_CALENDAR to Icons.Rounded.DateRange,

    // 相机
    android.Manifest.permission.CAMERA to Icons.Rounded.CameraAlt,

    // 通讯录
    android.Manifest.permission.READ_CONTACTS to Icons.Rounded.Contacts,
    android.Manifest.permission.WRITE_CONTACTS to Icons.Rounded.Contacts,
    android.Manifest.permission.GET_ACCOUNTS to Icons.Rounded.Contacts,

    // 位置
    android.Manifest.permission.ACCESS_FINE_LOCATION to Icons.Rounded.LocationOn,
    android.Manifest.permission.ACCESS_COARSE_LOCATION to Icons.Rounded.LocationOn,
    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION to Icons.Rounded.LocationOn,

    // 麦克风
    android.Manifest.permission.RECORD_AUDIO to Icons.Rounded.Mic,

    // 电话
    android.Manifest.permission.READ_PHONE_STATE to Icons.Rounded.Phone,
    android.Manifest.permission.READ_PHONE_NUMBERS to Icons.Rounded.Phone,
    android.Manifest.permission.CALL_PHONE to Icons.Rounded.Call,
    android.Manifest.permission.ANSWER_PHONE_CALLS to Icons.Rounded.Call,
    android.Manifest.permission.READ_CALL_LOG to Icons.Rounded.Call,
    android.Manifest.permission.WRITE_CALL_LOG to Icons.Rounded.Call,
    android.Manifest.permission.ADD_VOICEMAIL to Icons.Rounded.Phone,
    android.Manifest.permission.USE_SIP to Icons.Rounded.Phone,
    android.Manifest.permission.ACCEPT_HANDOVER to Icons.Rounded.Phone,

    // 身体传感器
    android.Manifest.permission.BODY_SENSORS to Icons.Rounded.Sensors,
    android.Manifest.permission.BODY_SENSORS_BACKGROUND to Icons.Rounded.Sensors,

    // 短信
    android.Manifest.permission.SEND_SMS to Icons.Rounded.Sms,
    android.Manifest.permission.RECEIVE_SMS to Icons.Rounded.Sms,
    android.Manifest.permission.READ_SMS to Icons.Rounded.Sms,
    android.Manifest.permission.RECEIVE_WAP_PUSH to Icons.Rounded.Sms,
    android.Manifest.permission.RECEIVE_MMS to Icons.Rounded.Sms,

    // 存储 (传统)
    android.Manifest.permission.READ_EXTERNAL_STORAGE to Icons.Rounded.Folder,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE to Icons.Rounded.Folder,

    // 运动数据
    android.Manifest.permission.ACTIVITY_RECOGNITION to Icons.AutoMirrored.Rounded.DirectionsRun,

    // 附近设备 (Android 12+)
    android.Manifest.permission.BLUETOOTH_SCAN to Icons.Rounded.Bluetooth,
    android.Manifest.permission.BLUETOOTH_CONNECT to Icons.Rounded.Bluetooth,
    android.Manifest.permission.BLUETOOTH_ADVERTISE to Icons.Rounded.Bluetooth,
    android.Manifest.permission.UWB_RANGING to Icons.Rounded.Bluetooth,

    // 通知 (Android 13+)
    android.Manifest.permission.POST_NOTIFICATIONS to Icons.Rounded.Notifications,

    // 媒体 (Android 13+)
    android.Manifest.permission.READ_MEDIA_AUDIO to Icons.Rounded.MusicNote,
    android.Manifest.permission.READ_MEDIA_IMAGES to Icons.Rounded.Image,
    android.Manifest.permission.READ_MEDIA_VIDEO to Icons.Rounded.VideoFile,

    // 媒体（通用/备用）
    android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED to Icons.Rounded.PermMedia
)
