package com.lunacattus.conflux.permission

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.logger.Logger

/** 自定义权限弹窗签名：state 当前状态, dismiss 关闭回调, confirm 确认回调 */
typealias CustomPermissionDialog = @Composable (PermissionState, () -> Unit, () -> Unit) -> Unit

private const val TAG = "PermissionDialog"

/**
 * 权限理由弹窗 —— 用户拒绝过一次权限后展示，引导再次授权。
 */
@Composable
fun PermissionRationaleDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Permission Required",
    message: String = "This permission is needed for full functionality.",
    confirmText: String = "Continue",
    dismissText: String = "Cancel",
    cancelable: Boolean = false,
    permissions: List<String> = emptyList(),
    permissionIcons: Map<String, ImageVector> = PermissionIconMap,
    nameProvider: PermissionNameProvider = { permissionDisplayName(it) }
) {
    if (show) {
        Logger.d(TAG, "showRationaleDialog")
        AlertDialog(
            onDismissRequest = {
                if (cancelable) {
                    Logger.d(TAG, "rationaleDialog: dismiss")
                    onDismiss()
                } else {
                    Logger.d(TAG, "rationaleDialog: dismiss blocked (not cancelable)")
                }
            },
            title = { Text(title) },
            text = {
                DialogText(permissions, permissionIcons, message, nameProvider)
            },
            confirmButton = {
                TextButton(onClick = {
                    Logger.d(TAG, "rationaleDialog: confirm -> re-request")
                    onConfirm()
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                if (cancelable) {
                    TextButton(onClick = {
                        Logger.d(TAG, "rationaleDialog: cancel")
                        onDismiss()
                    }) {
                        Text(dismissText)
                    }
                }
            }
        )
    }
}

/**
 * 权限设置弹窗 —— 权限被永久拒绝后展示，引导前往系统设置页。
 */
@Composable
fun PermissionSettingsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Permission Required",
    message: String = "This permission has been permanently denied. Please enable it in system settings.",
    confirmText: String = "Go to Settings",
    dismissText: String = "Cancel",
    cancelable: Boolean = true,
    permissions: List<String> = emptyList(),
    permissionIcons: Map<String, ImageVector> = PermissionIconMap,
    nameProvider: PermissionNameProvider = { permissionDisplayName(it) }
) {
    if (show) {
        Logger.d(TAG, "showSettingsDialog")
        AlertDialog(
            onDismissRequest = {
                if (cancelable) {
                    Logger.d(TAG, "settingsDialog: dismiss")
                    onDismiss()
                } else {
                    Logger.d(TAG, "settingsDialog: dismiss blocked (not cancelable)")
                }
            },
            title = { Text(title) },
            text = {
                DialogText(permissions, permissionIcons, message, nameProvider)
            },
            confirmButton = {
                TextButton(onClick = {
                    Logger.d(TAG, "settingsDialog: confirm -> go to settings")
                    onConfirm()
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                if (cancelable) {
                    TextButton(onClick = {
                        Logger.d(TAG, "settingsDialog: cancel")
                        onDismiss()
                    }) {
                        Text(dismissText)
                    }
                }
            }
        )
    }
}

/**
 * 权限弹窗宿主 —— 已集成到 [rememberPermissionState] 中。
 * 如需自定义弹窗，传入 [rationaleDialog] 或 [settingsDialog]。
 */
@Composable
fun PermissionDialogHost(
    state: PermissionState,
    rationaleDialog: CustomPermissionDialog? = null,
    settingsDialog: CustomPermissionDialog? = null,
    nameProvider: PermissionNameProvider = { permissionDisplayName(it) }
) {
    if (state.showRationaleDialog) {
        if (rationaleDialog != null) {
            rationaleDialog(state, state::onRationaleDismiss, state::onRationaleConfirm)
        } else {
            PermissionRationaleDialog(
                show = true,
                onDismiss = state::onRationaleDismiss,
                onConfirm = state::onRationaleConfirm,
                title = state.rationaleConfig.title,
                message = state.rationaleConfig.message,
                confirmText = state.rationaleConfig.confirmText,
                dismissText = state.rationaleConfig.dismissText,
                cancelable = state.rationaleConfig.cancelable,
                permissions = state.deniedPermissions,
                nameProvider = nameProvider
            )
        }
    }

    if (state.showSettingsDialog) {
        if (settingsDialog != null) {
            settingsDialog(state, state::onSettingsDismiss, state::onSettingsConfirm)
        } else {
            PermissionSettingsDialog(
                show = true,
                onDismiss = state::onSettingsDismiss,
                onConfirm = state::onSettingsConfirm,
                title = state.settingsConfig.title,
                message = state.settingsConfig.message,
                confirmText = state.settingsConfig.confirmText,
                dismissText = state.settingsConfig.dismissText,
                cancelable = state.settingsConfig.cancelable,
                permissions = state.deniedPermissions,
                nameProvider = nameProvider
            )
        }
    }
}

@Composable
private fun DialogText(
    permissions: List<String>,
    icons: Map<String, ImageVector>,
    message: String,
    nameProvider: PermissionNameProvider
) {
    Column {
        permissions.forEach { perm ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                icons[perm]?.let {
                    Icon(it, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Text(nameProvider(perm))
            }
            Spacer(Modifier.height(12.dp))
        }
        if (permissions.isNotEmpty()) {
            Spacer(Modifier.height(3.dp))
        }
        Text(message, fontSize = 15.sp)
    }
}
