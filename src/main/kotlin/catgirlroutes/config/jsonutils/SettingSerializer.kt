package catgirlroutes.config.jsonutils

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.impl.*
import com.google.gson.*
import java.lang.reflect.Type

class SettingSerializer : JsonSerializer<Setting<*>> {
    override fun serialize(src: Setting<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonObject().apply {
            when (src) {
                is BooleanSetting -> this.addProperty(src.name, src.enabled)
                is NumberSetting -> this.addProperty(src.name, src.value)
                is SelectorSetting -> this.addProperty(src.name, src.selected)
                is tSelectorSetting -> this.addProperty(src.name, src.selected)
                is StringSetting -> this.addProperty(src.name, src.text)
                is ColorSetting -> this.addProperty(src.name, src.value.rgb)
                is ActionSetting -> this.addProperty(src.name, "Action Setting")
                is KeyBindSetting -> this.addProperty(src.name, src.value.key)
                is ListSetting<*, *> -> add(src.name, JsonArray().apply {
                    src.value.forEach { element ->
                        add(element.toJsonElement())
                    }
                })
                is MapSetting<*, *, *> -> add(src.name, JsonObject().apply {
                    src.value.forEach { (key, value) ->
                        add(key.toString(), value.toJsonElement())
                    }
                })
                is OrderSetting -> add(src.name, JsonObject().apply {
                    src.value.forEach { (key, value) ->
                        addProperty(key, value)
                    }
                })
            }
        }
    }

    private fun Any?.toJsonElement(): JsonElement = when {
        this == null -> JsonNull.INSTANCE
        this is Boolean -> JsonPrimitive(this)
        this is Number -> JsonPrimitive(this)
        this is String -> JsonPrimitive(this)
        this is Collection<*> -> JsonArray().apply {
            this@toJsonElement.forEach {
                add(it?.toJsonElement() ?: JsonNull.INSTANCE)
            }
        }
        this is Pair<*, *> -> JsonObject().apply {
            addProperty("first", first.toString())
            addProperty("second", second.toString())
        }
        this is Triple<*, *, *> -> JsonObject().apply {
            addProperty("first", first.toString())
            addProperty("second", second.toString())
            addProperty("third", third.toString())
        }
        this::class.isData -> {
            JsonObject().apply {
                addProperty("_dataClass", this@toJsonElement::class.java.name)
                this@toJsonElement::class.java.declaredFields.forEach { field ->
                    field.isAccessible = true
                    try {
                        val value = field.get(this@toJsonElement)
                        add(field.name, value?.toJsonElement() ?: JsonNull.INSTANCE)
                    } catch (e: Exception) {
                        add(field.name, JsonNull.INSTANCE)
                    }
                }
            }
        }
        this is Map<*, *> && this.containsKey("_dataClass") -> {
            JsonObject().apply {
                this@toJsonElement.forEach { (key, value) ->
                    add(key.toString(), value?.toJsonElement() ?: JsonNull.INSTANCE)
                }
            }
        }
        else -> throw IllegalArgumentException("can't serialize ${this::class.simpleName}")
    }
}