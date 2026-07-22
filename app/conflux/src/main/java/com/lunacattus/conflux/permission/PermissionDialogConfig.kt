package com.lunacattus.conflux.permission

/** 理由弹窗配置 */
data class RationaleDialogConfig(
    val title: String = "需要权限",
    val message: String = "为了提供完整的功能，我们需要获取以下权限。",
    val confirmText: String = "继续授权",
    val dismissText: String = "取消"
)

/** 设置弹窗配置 */
data class SettingsDialogConfig(
    val title: String = "需要权限",
    val message: String = "以下权限已被永久拒绝，请前往设置页面手动开启。",
    val confirmText: String = "前往设置",
    val dismissText: String = "取消"
)
