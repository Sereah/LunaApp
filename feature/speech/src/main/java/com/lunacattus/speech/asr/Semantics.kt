package com.lunacattus.speech.asr

//同步配置asr.xbnf

enum class Domain(val value: String) {
    PHONE("phone"),
    CONTROL("control");

    companion object {
        private val map = entries.associateBy { it.value.lowercase() }
        fun fromString(s: String) = map[s.lowercase()] ?: throw IllegalArgumentException("Unknown domain: $s")
    }
}

enum class ActionType(val value: String) {
    OPEN("open"),
    CLOSE("close"),
    CALL("call"),
    SMS("sms");

    companion object {
        private val map = entries.associateBy { it.value.lowercase() }
        fun fromString(s: String) = map[s.lowercase()] ?: throw IllegalArgumentException("Unknown action: $s")
    }
}

enum class SettingItem(val value: String) {
    BT("蓝牙"),
    WIFI("WIFI"),
    AIRPLANE("飞行模式"),
    SILENT("静音模式");

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromString(s: String) = map[s] ?: throw IllegalArgumentException("Unknown setting item: $s")
    }
}

enum class AppName(val value: String) {
    PLAYER("播放器"),
    CAMERA("相机");

    companion object {
        private val map = entries.associateBy { it.value }
        fun fromString(s: String) = map[s] ?: throw IllegalArgumentException("Unknown app: $s")
    }
}

// 基类
open class Command(
    open val domain: Domain,
    open val action: ActionType
)

// phone domain
data class PhoneCommand(
    override val domain: Domain,
    override val action: ActionType,
    val person: String? = null,
    val number: String? = null
) : Command(domain, action)

// control domain
data class ControlCommand(
    override val domain: Domain,
    override val action: ActionType,
    val app: AppName? = null,
    val settingItem: SettingItem? = null
) : Command(domain, action)


