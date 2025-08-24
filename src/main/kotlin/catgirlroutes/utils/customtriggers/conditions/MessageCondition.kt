package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.utils.customtriggers.TypeName
import java.util.regex.Pattern

@TypeName("MessageCondition")
class MessageCondition(val pattern: String, val isRegex: Boolean = false) : TriggerCondition() { // TODO IMPL REGEX LIKE IN ST
    private val regex: Pattern? = if (isRegex) Pattern.compile(pattern) else null

    override fun check(): Boolean { // handled in chat event
        return false
    }

    fun checkMessage(message: String): Boolean {
        return if (isRegex) {
            regex?.matcher(message)?.find() ?: false
        } else {
            message.contains(pattern)
        }
    }
}
