package com.lunacattus.speech.asr

//同步配置asr.xbnf

sealed interface SemanticsKey {
    data object Setting: SemanticsKey
    data object Contact: SemanticsKey
}

interface SemanticsValue

//设置项语义
enum class Setting {
    Bluetooth, WIFI, Setting, Airplane, Silent
}

data class SettingSemantics(
    val isOpen: Boolean,
    val target: Setting
) : SemanticsValue

//联系人语义
data class ContactSemantic(
    val name: String
): SemanticsValue

