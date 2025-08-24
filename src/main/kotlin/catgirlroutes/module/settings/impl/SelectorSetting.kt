package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility

//class StringSelectorSetting(
//    name: String,
//    defaultSelected: String,
//    var options: ArrayList<String>,
//    description: String? = null,
//    visibility: Visibility = Visibility.VISIBLE,
//) : Setting<Int>(name, description, visibility){
//
//    override val default: Int = optionIndex(defaultSelected)
//    override var value: Int
//        get() = index
//        set(value) {index = value}
//
//    var index: Int = optionIndex(defaultSelected)
//     set(value) {
//         /** guarantees that index is in bounds and enables cycling behaviour */
//         val newVal = processInput(value)
//         field = if (newVal > options.size - 1)  0 else if ( newVal < 0) options.size - 1 else newVal
//     }
//
//    var selected: String
//     set(value) {
//        index = optionIndex(value)
//    }
//    get() {
//        return options[index]
//    }
//
//    /**
//     * Finds the index of given option in the option list.
//     * Ignores the case of the strings and returns 0 if not found.
//     */
//    private fun optionIndex(string: String): Int {
//        return MathHelper.clamp_int(this.options.map { it.lowercase() }.indexOf(string.lowercase()), 0, options.size - 1)
//    }
//
//    fun isSelected(string: String): Boolean {
//        return  this.selected.equals(string, ignoreCase = true)
//    }
//}
class SelectorSetting(
    name: String,
    defaultSelected: String,
    options: List<String>,
    description: String? = null,
    visibility: Visibility = Visibility.VISIBLE,
) : Setting<StringSelector>(name, description, visibility) {

    private val selector = StringSelector(options, options.indexOf(defaultSelected).coerceAtLeast(0))

    override val default: StringSelector = selector

    override var value: StringSelector
        get() = selector
        set(value) {
            selector.select(value.selected)
        }

    var selected: String
        get() = selector.selected
        set(value) { selector.select(value) }

    var index: Int
        get() = selector.index
        set(value) { selector.index = value }

    var options: List<String>
        get() = selector.options
        set(value) { selector.options = value }

    fun isSelected(string: String): Boolean {
        return selector.isSelected(string)
    }
}

class StringSelector(
    options: List<String>,
    private var selectedIndex: Int = 0
) {
    var options: List<String> = options
        set(value) {
            field = value
            selectedIndex = selectedIndex.coerceIn(0, value.lastIndex)
        }

    var selected: String
        get() = options[selectedIndex]
        set(value) = select(value)

    var index: Int
        get() = selectedIndex
        set(value) {
            selectedIndex = when {
                value >= options.size -> 0
                value < 0 -> options.size - 1
                else -> value
            }
        }

    fun isSelected(value: String): Boolean {
        return selected.equals(value, ignoreCase = true)
    }

    fun select(value: String) {
        val newIndex = options.indexOfFirst { it.equals(value, ignoreCase = true) }
        if (newIndex != -1) {
            index = newIndex
        }
    }
}