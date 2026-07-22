package com.lunacattus.conflux.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private const val TAG = "PermissionDialog"

/**
 * 权限理由弹窗 —— 用户拒绝过一次权限后展示，引导再次授权。
 */
@Composable
fun PermissionRationaleDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "需要权限",
    message: String = "为了提供完整的功能，我们需要获取此权限。",
    confirmText: String = "继续授权",
    dismissText: String = "取消",
    permissions: List<String> = emptyList(),
    permissionIcons: Map<String, ImageVector> = PermissionIconMap
) {
    if (show) {
        Logger.d(TAG, "showRationaleDialog")
        AlertDialog(
            onDismissRequest = {
                Logger.d(TAG, "rationaleDialog: dismiss")
                onDismiss()
            },
            title = { Text(title) },
            text = {
                DialogText(permissions, permissionIcons, message)
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
                TextButton(onClick = {
                    Logger.d(TAG, "rationaleDialog: cancel")
                    onDismiss()
                }) {
                    Text(dismissText)
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
    title: String = "需要权限",
    message: String = "该权限已被永久拒绝，请前往设置页面手动开启。",
    confirmText: String = "前往设置",
    dismissText: String = "取消",
    permissions: List<String> = emptyList(),
    permissionIcons: Map<String, ImageVector> = PermissionIconMap
) {
    if (show) {
        Logger.d(TAG, "showSettingsDialog")
        AlertDialog(
            onDismissRequest = {
                Logger.d(TAG, "settingsDialog: dismiss")
                onDismiss()
            },
            title = { Text(title) },
            text = {
                DialogText(permissions, permissionIcons, message)
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
                TextButton(onClick = {
                    Logger.d(TAG, "settingsDialog: cancel")
                    onDismiss()
                }) {
                    Text(dismissText)
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
    rationaleDialog: (@Composable (
        PermissionState,
        () -> Unit,
        () -> Unit
    ) -> Unit)? = null,
    settingsDialog: (@Composable (
        PermissionState,
        () -> Unit,
        () -> Unit
    ) -> Unit)? = null
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
                permissions = state.deniedPermissions
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
                permissions = state.deniedPermissions
            )
        }
    }
}

@Composable
private fun DialogText(
    permissions: List<String>,
    icons: Map<String, ImageVector>,
    message: String
) {
    Column {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = permissions, key = { it }) { perm ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icons[perm]?.let {
                        Icon(it, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(permissionDisplayName(perm))
                }
            }
        }
        if (permissions.isNotEmpty()) {
            Spacer(Modifier.height(15.dp))
        }
        Text(message, fontSize = 15.sp)
    }
}
