package com.lunacattus.conflux.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lunacattus.logger.Logger

private const val TAG = "PermissionState"

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

    internal var waitingForSettingsResult by mutableStateOf(false)

    private var onResultCallback: ((Boolean, List<String>) -> Unit)? = null

    init {
        Logger.d(TAG, "init: permissions=$permissions, allGranted=$allGranted")
    }

    /**
     * @param onResult 结果回调：allGranted 是否全部授权，deniedList 被拒绝的权限列表
     */
    fun request(onResult: (allGranted: Boolean, deniedList: List<String>) -> Unit) {
        if (allGranted) {
            Logger.d(TAG, "request: already all granted")
            onResult(true, emptyList())
            return
        }
        Logger.d(TAG, "request: launching")
        onResultCallback = onResult
        launchRequest()
    }

    fun launchRequest() {
        val (standardPerms, specialPerms) = permissions.partition { !isSpecialPermission(it) }
        val ungrantedSpecial = specialPerms.filter { !checkSpecialPermission(it, context) }

        // 过滤当前设备不存在的权限
        val definedStandard = standardPerms.filter { isPermissionDefined(it, context) }
        val undefinedStandard = standardPerms.filter { !isPermissionDefined(it, context) }
        if (undefinedStandard.isNotEmpty()) {
            Logger.d(TAG, "launchRequest: skipping undefined permissions: $undefinedStandard")
        }

        // 过滤缺少同伴权限的孤儿权限
        val orphanCompanions = definedStandard.filter { isOrphanCompanion(it, permissions) }
        val validStandard = definedStandard - orphanCompanions.toSet()
        if (orphanCompanions.isNotEmpty()) {
            Logger.w(TAG, "launchRequest: skipping orphan companion permissions: $orphanCompanions")
        }

        if (ungrantedSpecial.isNotEmpty()) {
            Logger.d(TAG, "launchRequest: special permissions need settings: $ungrantedSpecial")
            deniedPermissions = ungrantedSpecial
            showSettingsDialog = true
            if (validStandard.isEmpty()) {
                Logger.d(TAG, "launchRequest: no standard permissions to launch, showing settings dialog only")
                return
            }
        }

        if (validStandard.isNotEmpty()) {
            Logger.d(TAG, "launchRequest: launching standard: $validStandard")
            launcher.launch(validStandard.toTypedArray())
        }
    }

    fun goToSettings() {
        val firstSpecial = deniedPermissions.firstOrNull { isSpecialPermission(it) }
        val perm = firstSpecial ?: deniedPermissions.first()
        val intent = getSettingsIntent(perm, context)
        Logger.d(TAG, "goToSettings: perm=$perm, action=${intent.action}")
        context.startActivity(intent)
    }

    /** 从设置页返回后调用，检测权限是否已被手动开启 */
    internal fun checkAfterSettingsReturn() {
        if (!waitingForSettingsResult) return
        waitingForSettingsResult = false
        Logger.d(TAG, "checkAfterSettingsReturn: checking permissions")
        if (checkAllGranted()) {
            allGranted = true
            Logger.d(TAG, "checkAfterSettingsReturn: permissions granted after settings, invoking callback")
            onResultCallback?.invoke(true, emptyList())
            onResultCallback = null
        } else {
            Logger.d(TAG, "checkAfterSettingsReturn: permissions still not granted, re-requesting")
            launchRequest()
        }
    }

    internal fun onRationaleConfirm() {
        Logger.d(TAG, "rationaleConfirm: re-request")
        showRationaleDialog = false
        launchRequest()
    }

    internal fun onRationaleDismiss() {
        Logger.d(TAG, "rationaleDismiss: invoking callback with denied=$deniedPermissions")
        showRationaleDialog = false
        onResultCallback?.invoke(false, deniedPermissions)
        onResultCallback = null
    }

    internal fun onSettingsConfirm() {
        Logger.d(TAG, "settingsConfirm: go to settings")
        showSettingsDialog = false
        waitingForSettingsResult = true
        goToSettings()
    }

    internal fun onSettingsDismiss() {
        Logger.d(TAG, "settingsDismiss: invoking callback with denied=$deniedPermissions")
        showSettingsDialog = false
        onResultCallback?.invoke(false, deniedPermissions)
        onResultCallback = null
    }

    internal fun handleResult(grantResults: Map<String, Boolean>) {
        // 过滤被同伴组覆盖的拒绝项（如 VISUAL_USER_SELECTED 授权覆盖 IMAGES 拒绝）
        val effectiveDenied = grantResults.filterValues { !it }.keys.filter { perm ->
            val group = getCompanionGroup(perm, permissions)
            if (group != null) {
                val groupGranted = grantResults.any { (p, g) -> p in group && g } ||
                    group.any { p -> checkRawPermission(p) }
                if (groupGranted) {
                    Logger.d(TAG, "handleResult: $perm denied but covered by companion group grant, ignoring")
                }
                !groupGranted
            } else true
        }

        val specialPerms = permissions.filter { isSpecialPermission(it) }
        val ungrantedSpecial = specialPerms.filter { !checkSpecialPermission(it, context) }

        allGranted = effectiveDenied.isEmpty() && ungrantedSpecial.isEmpty()

        if (allGranted) {
            Logger.d(TAG, "handleResult: all effective permissions granted")
            onResultCallback?.invoke(true, emptyList())
            onResultCallback = null
            return
        }

        // 展示列表不包含非孤儿同伴权限（主权限已代表相同的拒绝含义）
        val displayDenied = effectiveDenied.filter { perm ->
            !isCompanionPermission(perm) || isOrphanCompanion(perm, permissions)
        }
        val filteredCompanions = effectiveDenied - displayDenied.toSet()
        if (filteredCompanions.isNotEmpty()) {
            Logger.d(TAG, "handleResult: filtered companion permissions from display: $filteredCompanions")
        }
        deniedPermissions = displayDenied + ungrantedSpecial

        val anyCanShowRationale = effectiveDenied.any { perm ->
            (context as? Activity)?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, perm)
            } ?: false
        }

        Logger.d(TAG, "handleResult: effectiveDenied=$effectiveDenied, specialUngranted=$ungrantedSpecial, anyRationale=$anyCanShowRationale")

        if (ungrantedSpecial.isNotEmpty()) {
            deniedPermissions = ungrantedSpecial
            showSettingsDialog = true
            return
        }

        if (anyCanShowRationale) {
            showRationaleDialog = true
        } else {
            showSettingsDialog = true
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 私有方法
    // ═══════════════════════════════════════════════════════════

    private fun checkAllGranted(): Boolean {
        return permissions.all { checkSinglePermission(it) }
    }

    private fun checkSinglePermission(permission: String): Boolean {
        if (!isPermissionDefined(permission, context)) {
            Logger.d(TAG, "checkSinglePermission: $permission not defined on this device, treating as granted")
            return true
        }
        if (isOrphanCompanion(permission, permissions)) {
            Logger.w(TAG, "checkSinglePermission: $permission is orphan companion, treating as granted")
            return true
        }
        if (isSpecialPermission(permission)) {
            return checkSpecialPermission(permission, context)
        }

        // 同伴权限组：任一成员授权即视为本权限授权
        val group = getCompanionGroup(permission, permissions)
        if (group != null) {
            val anyGroupGranted = group.any { checkRawPermission(it) }
            if (anyGroupGranted) {
                Logger.d(TAG, "checkSinglePermission: $permission covered by companion group grant")
                return true
            }
        }

        return checkRawPermission(permission)
    }

    private fun checkRawPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
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
    rationaleDialog: CustomPermissionDialog? = null,
    settingsDialog: CustomPermissionDialog? = null,
    nameProvider: PermissionNameProvider = { permissionDisplayName(it) }
): PermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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

    // 从设置页返回后自动检测权限是否已开启
    DisposableEffect(lifecycleOwner, permissionState) {
        Logger.d(TAG, "rememberPermissionState: lifecycle observer registered")
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Logger.d(TAG, "lifecycle: ON_RESUME, checking permissions")
                permissionState.checkAfterSettingsReturn()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PermissionDialogHost(
        state = permissionState,
        rationaleDialog = rationaleDialog,
        settingsDialog = settingsDialog,
        nameProvider = nameProvider
    )
    return permissionState
}
