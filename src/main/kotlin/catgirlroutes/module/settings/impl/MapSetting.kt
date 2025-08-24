package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility

class MapSetting<K, V, T : MutableMap<K, V>>(
    name: String,
    override val default: T,
    description: String = ""
): Setting<T>(name, description, Visibility.HIDDEN) {
    override var value: T = default
}