package com.lunacattus.speech.asr

import com.google.gson.*
import java.lang.reflect.Type

class CommandDeserializer : JsonDeserializer<Command> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Command {
        val obj = json.asJsonObject

        // domain 自动映射
        val domain = Domain.fromString(obj.get("domain").asString)

        return when (domain) {
            Domain.PHONE -> {
                val action = ActionType.fromString(obj.get("action").asString)
                val person = obj.get("person")?.asString
                val number = obj.get("number")?.asString
                PhoneCommand(domain, action, person, number)
            }
            Domain.CONTROL -> {
                val action = ActionType.fromString(obj.get("action").asString)
                val app = obj.get("app")?.asString?.let { AppName.fromString(it) }
                val settingItem = obj.get("settingItem")?.asString?.let { SettingItem.fromString(it) }
                ControlCommand(domain, action, app, settingItem)
            }
        }
    }
}

