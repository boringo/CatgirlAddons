package catgirlroutes.utils.customtriggers.actions

import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.customtriggers.TypeName

@TypeName("SendMessageAction")
class SendMessageAction(val message: String) : TriggerAction() {
    override fun execute() {
        ChatUtils.chatMessage(message)
    }
}