package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.ui.hud.HudElementDSL

class HudSetting( // todo improve click gui elements
    name: String = "",
    hud: HudElement,
    description: String? = null,
) : Setting<HudElement>(name, description, Visibility.VISIBLE) {

    constructor(name: String = "", description: String? = null, block: HudElementDSL.() -> Unit) :
            this(name, HudElementDSL(name).apply(block).build(), description)

    override val default: HudElement = hud

    override var value: HudElement = default

    var enabled: Boolean
        get() = value.enabledSett.enabled
        set(value) {
            this.value.enabledSett.enabled = value
        }
}