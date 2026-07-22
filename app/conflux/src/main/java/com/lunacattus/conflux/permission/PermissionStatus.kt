package com.lunacattus.conflux.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lunacattus.logger.Logger

private const val TAG = "PermissionState"

/** 权限 → 中文名映射 */
fun permissionDisplayName(permission: String): String = when (permission) {
    android.Manifest.permission.RECORD_AUDIO -> "麦克风"
    android.Manifest.permission.CAMERA -> "相机"
    android.Manifest.permission.ACCESS_FINE_LOCATION -> "精确位置"
    android.Manifest.permission.ACCESS_COARSE_LOCATION -> "大致位置"
    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "后台位置"
    android.Manifest.permission.POST_NOTIFICATIONS -> "通知"
    android.Manifest.permission.READ_MEDIA_AUDIO -> "音乐和音频"
    android.Manifest.permission.READ_MEDIA_IMAGES -> "照片"
    android.Manifest.permission.READ_MEDIA_VIDEO -> "视频"
    android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED -> "照片和视频"
    android.Manifest.permission.READ_EXTERNAL_STORAGE -> "存储空间"
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> "存储空间"
    android.Manifest.permission.BLUETOOTH_CONNECT -> "蓝牙"
    android.Manifest.permission.BLUETOOTH_SCAN -> "蓝牙扫描"
    android.Manifest.permission.BLUETOOTH_ADVERTISE -> "蓝牙广播"
    android.Manifest.permission.UWB_RANGING -> "超宽带"
    android.Manifest.permission.READ_PHONE_STATE -> "电话状态"
    android.Manifest.permission.READ_PHONE_NUMBERS -> "电话号码"
    android.Manifest.permission.CALL_PHONE -> "拨打电话"
    android.Manifest.permission.ANSWER_PHONE_CALLS -> "接听电话"
    android.Manifest.permission.READ_CALL_LOG -> "通话记录"
    android.Manifest.permission.WRITE_CALL_LOG -> "通话记录"
    android.Manifest.permission.ADD_VOICEMAIL -> "语音信箱"
    android.Manifest.permission.USE_SIP -> "网络电话"
    android.Manifest.permission.ACCEPT_HANDOVER -> "通话转移"
    android.Manifest.permission.READ_CONTACTS -> "通讯录"
    android.Manifest.permission.WRITE_CONTACTS -> "通讯录"
    android.Manifest.permission.GET_ACCOUNTS -> "账户"
    android.Manifest.permission.READ_CALENDAR -> "日历"
    android.Manifest.permission.WRITE_CALENDAR -> "日历"
    android.Manifest.permission.BODY_SENSORS -> "身体传感器"
    android.Manifest.permission.BODY_SENSORS_BACKGROUND -> "身体传感器"
    android.Manifest.permission.ACTIVITY_RECOGNITION -> "运动数据"
    android.Manifest.permission.READ_SMS -> "短信"
    android.Manifest.permission.SEND_SMS -> "短信"
    android.Manifest.permission.RECEIVE_SMS -> "短信"
    android.Manifest.permission.RECEIVE_WAP_PUSH -> "WAP推送"
    android.Manifest.permission.RECEIVE_MMS -> "彩信"
    else -> permission.substringAfterLast(".")
}

class PermissionState(
    val permissions: List<String>,
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    val rationaleConfig: RationaleDialogConfig = RationaleDialogConfig(),
    val settingsConfig: SettingsDialogConfig = SettingsDialogConfig()
) {
    var allGranted by mutableStateOf(checkAllGranted())
        private set

    internal var showRationaleDialog by mutableStateOf(false)
    internal var showSettingsDialog by mutableStateOf(false)

    /** 最近一次请求中被拒绝的权限列表（用于弹窗展示） */
    internal var deniedPermissions by mutableStateOf(emptyList<String>())

    private var onGrantedCallback: (() -> Unit)? = null

    init {
        Logger.d(TAG, "init: permissions=$permissions, allGranted=$allGranted")
    }

    fun request(onGranted: () -> Unit) {
        if (allGranted) {
            Logger.d(TAG, "request: already all granted")
            onGranted()
            return
        }
        Logger.d(TAG, "request: launching")
        onGrantedCallback = onGranted
        launchRequest()
    }

    fun launchRequest() {
        Logger.d(TAG, "launchRequest: permissions=$permissions")
        launcher.launch(permissions.toTypedArray())
    }

    fun goToSettings() {
        Logger.d(TAG, "goToSettings")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    internal fun onRationaleConfirm() {
        Logger.d(TAG, "rationaleConfirm: re-request")
        showRationaleDialog = false
        launchRequest()
    }

    internal fun onRationaleDismiss() {
        Logger.d(TAG, "rationaleDismiss")
        showRationaleDialog = false
        onGrantedCallback = null
    }

    internal fun onSettingsConfirm() {
        Logger.d(TAG, "settingsConfirm: go to settings")
        showSettingsDialog = false
        goToSettings()
    }

    internal fun onSettingsDismiss() {
        Logger.d(TAG, "settingsDismiss")
        showSettingsDialog = false
        onGrantedCallback = null
    }

    internal fun handleResult(grantResults: Map<String, Boolean>) {
        val granted = grantResults.values.all { it }
        allGranted = granted

        if (granted) {
            Logger.d(TAG, "handleResult: all granted")
            onGrantedCallback?.invoke()
            onGrantedCallback = null
            return
        }

        val deniedList = grantResults.filterValues { !it }.keys.toList()
        deniedPermissions = deniedList

        val anyCanShowRationale = deniedList.any { perm ->
            (context as? Activity)?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, perm)
            } ?: false
        }

        Logger.d(TAG, "handleResult: denied=$deniedList, anyRationale=$anyCanShowRationale")

        if (anyCanShowRationale) {
            showRationaleDialog = true
        } else {
            onGrantedCallback = null
            showSettingsDialog = true
        }
    }

    private fun checkAllGranted(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

/**
 * @param permissions        需要请求的权限（vararg）
 * @param rationaleConfig    理由弹窗配置，可选
 * @param settingsConfig     设置弹窗配置，可选
 * @param rationaleDialog    自定义理由弹窗（提供 null 使用默认弹窗）
 * @param settingsDialog     自定义设置弹窗（提供 null 使用默认弹窗）
 */
@Composable
fun rememberPermissionState(
    vararg permissions: String,
    rationaleConfig: RationaleDialogConfig = RationaleDialogConfig(),
    settingsConfig: SettingsDialogConfig = SettingsDialogConfig(),
    rationaleDialog: (@Composable (
        PermissionState,
        () -> Unit,  // dismiss
        () -> Unit   // confirm
    ) -> Unit)? = null,
    settingsDialog: (@Composable (
        PermissionState,
        () -> Unit,  // dismiss
        () -> Unit   // confirm
    ) -> Unit)? = null
): PermissionState {
    val context = LocalContext.current
    var permissionState: PermissionState? = null
    val permList = permissions.toList()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        Logger.d(TAG, "requestResult: results=$grantResults")
        permissionState?.handleResult(grantResults)
    }

    permissionState = remember(permList) {
        PermissionState(permList, context, launcher, rationaleConfig, settingsConfig)
    }

    PermissionDialogHost(
        state = permissionState,
        rationaleDialog = rationaleDialog,
        settingsDialog = settingsDialog
    )
    return permissionState
}
