package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility

/**
 * Provides a Setting which stores a String.
 *
 * This Setting is represented by a text field in the gui.
 * @author Aton
 */
class StringSetting(
    name: String,
    override val default: String = "",
    val length: Int = 30,
    val placeholder: String = "",
    description: String? = null,
    visibility: Visibility = Visibility.VISIBLE,
) : Setting<String>(name, description, visibility) {

    override var value: String = default
        set(newStr) {
            val tempStr = processInput(newStr)
            field = if (length > 0 && tempStr.length > length) {
                tempStr.substring(0, length - 1)
            }else
                tempStr
        }

    var text: String by this::value
}