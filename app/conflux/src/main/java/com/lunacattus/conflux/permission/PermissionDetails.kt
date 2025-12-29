package com.lunacattus.conflux.permission

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class PermissionDetails(val icon: ImageVector, val name: String)

@Composable
internal fun getPermissionDetails(permission: String): PermissionDetails {
    return when (permission) {
        // Audio
        Manifest.permission.RECORD_AUDIO -> PermissionDetails(Icons.Filled.Mic, "麦克风")

        // Location
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionDetails(Icons.Filled.LocationOn, "位置")

        // Camera
        Manifest.permission.CAMERA -> PermissionDetails(Icons.Filled.CameraAlt, "相机")

        // Storage & Media
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> PermissionDetails(Icons.Filled.Image, "存储空间")
        // Android 13+ Media Permissions

        // Bluetooth
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE -> PermissionDetails(Icons.Filled.Bluetooth, "蓝牙")

        // Contacts
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS -> PermissionDetails(Icons.Filled.Contacts, "联系人")

        // Calendar
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR -> PermissionDetails(Icons.Filled.CalendarToday, "日历")

        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> PermissionDetails(Icons.Filled.Notifications, "通知")
                Manifest.permission.READ_MEDIA_IMAGES -> PermissionDetails(Icons.Filled.Image, "照片")
                Manifest.permission.READ_MEDIA_VIDEO -> PermissionDetails(Icons.Filled.Image, "视频")
                Manifest.permission.READ_MEDIA_AUDIO -> PermissionDetails(Icons.Filled.Mic, "音频")
                else -> PermissionDetails(Icons.Filled.Lock, permission.substringAfterLast('.'))
            }
        } else {
            PermissionDetails(Icons.Filled.Lock, permission.substringAfterLast('.'))
        }
    }
}
