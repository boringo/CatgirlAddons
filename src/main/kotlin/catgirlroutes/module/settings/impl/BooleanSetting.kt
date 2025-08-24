package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility

/**
 * A boolean Setting for modules.
 *
 * Can be used to toggle aspects of a Module.
 * Represented by a toggle button in the GUI.
 * @author Aton
 */
class BooleanSetting (
    name: String,
    override val default: Boolean = false,
    description: String? = null,
    visibility: Visibility = Visibility.VISIBLE,
): Setting<Boolean>(name, description, visibility) {
    constructor(name: String, description: String, visibility: Visibility = Visibility.VISIBLE) : this(name, false, description, visibility)

    override var value: Boolean = default
        set(value) {
            field = processInput(value)
        }

    var enabled: Boolean by this::value

    fun toggle() {
        enabled = !enabled
    }
}