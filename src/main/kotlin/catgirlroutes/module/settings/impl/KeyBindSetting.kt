package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

/**
 * A KeyBind Setting for Modules
 */
class KeyBindSetting(
    name: String,
    override val default: Keybinding,
    description: String? = null,
    visibility: Visibility = Visibility.VISIBLE,
) : Setting<Keybinding>(name, description, visibility) {

    constructor(name: String, key: Int = Keyboard.KEY_NONE, description: String? = null, visibility: Visibility = Visibility.VISIBLE) : this(name, Keybinding(key), description, visibility)

    override var value: Keybinding = default
        set(value) {
            field = processInput(value)
        }

    fun onPress(block: () -> Unit): KeyBindSetting {
        value.onPress = block
        return this
    }

    override fun reset() {
        value.key = default.key
    }
}

class Keybinding(var key: Int) {

    /**
     * Intended to active when keybind is pressed.
     */
    var onPress: (() -> Unit)? = null

    /**
     * @return `true` if [key] is held down.
     */
    fun isDown(): Boolean {
        return if (key == 0) false else (if (key < 0) Mouse.isButtonDown(key + 100) else Keyboard.isKeyDown(key))
    }
}