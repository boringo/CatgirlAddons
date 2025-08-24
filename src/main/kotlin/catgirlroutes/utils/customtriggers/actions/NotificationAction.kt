package catgirlroutes.utils.customtriggers.actions

import catgirlroutes.utils.customtriggers.TypeName

@TypeName("NotificationAction")
class NotificationAction(val text: String, val durationMs: Int = 3000) : TriggerAction() {
    override fun execute() { // TODO add on-screen notifications thing
    }
}