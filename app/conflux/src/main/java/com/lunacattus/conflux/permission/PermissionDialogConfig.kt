package com.lunacattus.conflux.permission

/** 理由弹窗配置 */
data class RationaleDialogConfig(
    val title: String = "Permission Required",
    val message: String = "This permission is needed for full functionality.",
    val confirmText: String = "Continue",
    val dismissText: String = "Cancel",
    /** 是否可取消，false 时无取消按钮且点击外部不关闭 */
    val cancelable: Boolean = false
)

/** 设置弹窗配置 */
data class SettingsDialogConfig(
    val title: String = "Permission Required",
    val message: String = "This permission has been permanently denied. Please enable it in system settings.",
    val confirmText: String = "Go to Settings",
    val dismissText: String = "Cancel",
    /** 是否可取消，false 时无取消按钮且点击外部不关闭 */
    val cancelable: Boolean = true
)
