package catgirlroutes.utils.customtriggers.actions

import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.customtriggers.TypeName

@TypeName("ReplaceMessageAction")
class ReplaceMessageAction(val replacement: String) : TriggerAction() { // TODO ADD CLICKABLE "[123](/hub)"
    override fun execute() {
        ChatUtils.chatMessage(replacement)
    }
}