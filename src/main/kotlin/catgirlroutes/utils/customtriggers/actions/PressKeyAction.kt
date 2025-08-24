package catgirlroutes.utils.customtriggers.actions

import catgirlroutes.utils.MovementUtils
import catgirlroutes.utils.customtriggers.TypeName

@TypeName("KeyAction")
class PressKeyAction(private val keyCode: Int) : TriggerAction() {
    override fun execute() {
        MovementUtils.pressKey(keyCode)
    }
}