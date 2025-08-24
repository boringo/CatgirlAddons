package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility

/**
 * A setting which allows for a selection out of a given set of options.
 *
 * In most cases it is more convenient to use the factory function which acts as a constructor which omits the [options]
 * parameter.
 *
 * @author Aton
 */
class tSelectorSetting<T>(
    name: String,
    override val default: T,
    val options: Array<out T>,
    description: String? = null,
    visibility: Visibility = Visibility.VISIBLE,
) : Setting<T>(name, description, visibility) where T : Options, T: Enum<T> {

    override var value: T = default
        set(input) {
            field = processInput(input)
        }

    var index: Int
        get() = value.ordinal
        set(newVal) {
            // guarantees that index is in bounds and enables cycling behaviour
            value = options[if (newVal > options.size - 1)  0 else if ( newVal < 0) options.size - 1 else newVal]
        }

    /**
     * [displayName][Options.displayName] of the selected Enum.
     * Can be used to set [value] based on the displayName of the Enum.
     * This is required for loading data from the config.
     * If possible [value] should be directly instead.
     */
    var selected: String
        get() = value.displayName
        set(input) {
            value = options.find { it.displayName.equals(input, ignoreCase = true) } ?: return
        }

    fun isSelected(option: Options): Boolean {
        return  this.value === option
    }
}

/**
 * This factory function provides a more convenient Constructor for [tSelectorSetting]
 * where [options][tSelectorSetting.options] can be omitted.
 * The options are inferred from the provided [default] value. All available constants will be used.
 *
 * If you want to limit the options to be a subset of the available constants, use the main constructor and specify those explicitly.
 */
inline fun <reified L> SelectorSetting(name: String,
                                       default: L,
                                       description: String? = null,
                                       visibility: Visibility = Visibility.VISIBLE,
) : tSelectorSetting<L> where L : Options, L: Enum<L> =
    tSelectorSetting(name, default, enumValues(), description, visibility)

interface Options {
    val displayName: String
}