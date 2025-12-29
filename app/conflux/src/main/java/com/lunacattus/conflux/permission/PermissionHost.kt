package com.lunacattus.conflux.permission

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

sealed interface PermissionUiState {

    /** 已全部授权 */
    data object Granted : PermissionUiState

    /** 明确需要发起系统权限请求（唯一触发 permissionLauncher 的状态） */
    data class RequestPermission(
        val permissions: List<String>
    ) : PermissionUiState

    /** 正在跳转系统设置，仅用于过渡，不触发任何副作用 */
    data object JumpingToSettings : PermissionUiState

    /** 被拒绝，但可以解释后再次请求 */
    data class NeedRationale(
        val permissions: List<String>
    ) : PermissionUiState

    /** request 后仍无法弹窗，只能去设置 */
    data class NeedGoSettings(
        val permissions: List<String>
    ) : PermissionUiState
}

@Composable
fun PermissionHost(
    permissions: List<String>,
    rationaleText: String = "为了使用核心功能，请授予以下权限：",
    settingsText: String = "你已关闭系统权限弹窗，请前往设置手动开启：",
    onPermissionDenied: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    var uiState by remember {
        mutableStateOf<PermissionUiState>(
            PermissionUiState.RequestPermission(permissions)
        )
    }

    fun deniedPermissions(): List<String> =
        permissions.filter {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->

            val denied = result.filterValues { !it }.keys.toList()

            if (denied.isEmpty()) {
                uiState = PermissionUiState.Granted
                return@rememberLauncherForActivityResult
            }

            val shouldShowRationale =
                denied.any { activity.shouldShowRequestPermissionRationale(it) }

            uiState = if (shouldShowRationale) {
                PermissionUiState.NeedRationale(denied)
            } else {
                PermissionUiState.NeedGoSettings(denied)
            }
        }

    val settingsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val denied = deniedPermissions()
            uiState = if (denied.isEmpty()) {
                PermissionUiState.Granted
            } else {
                // 🔑 从设置页回来，一律再 request
                PermissionUiState.RequestPermission(denied)
            }
        }

    when (val state = uiState) {

        PermissionUiState.Granted -> {
            content()
        }

        is PermissionUiState.RequestPermission -> {
            LaunchedEffect(state.permissions) {
                permissionLauncher.launch(state.permissions.toTypedArray())
            }
        }

        PermissionUiState.JumpingToSettings -> {
            // 纯过渡态，不渲染任何 UI，不触发副作用
        }

        is PermissionUiState.NeedRationale -> {
            PermissionDialog(
                text = rationaleText,
                permissions = state.permissions,
                confirmText = "继续",
                dismissText = "取消",
                onConfirm = {
                    // 🔑 先切状态，立刻移除 Dialog
                    uiState = PermissionUiState.RequestPermission(state.permissions)
                },
                onDismiss = onPermissionDenied
            )
        }

        is PermissionUiState.NeedGoSettings -> {
            PermissionDialog(
                text = settingsText,
                permissions = state.permissions,
                confirmText = "去设置",
                dismissText = "取消",
                onConfirm = {
                    uiState = PermissionUiState.JumpingToSettings
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts(
                                "package",
                                context.packageName,
                                null
                            )
                        }
                    settingsLauncher.launch(intent)
                },
                onDismiss = onPermissionDenied
            )
        }
    }
}


@Composable
private fun PermissionDialog(
    text: String,
    permissions: List<String>,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column {
                Text(text, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                PermissionsList(permissions.toTypedArray())
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun PermissionsList(permissionsToShow: Array<String>) {
    Column {
        permissionsToShow.forEach {
            val details = getPermissionDetails(it)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = details.icon, contentDescription = details.name)
                Spacer(modifier = Modifier.width(8.dp))
                Text(details.name)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
