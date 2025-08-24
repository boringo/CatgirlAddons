package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.utils.customtriggers.TypeName

@TypeName("KeyCondition")
class KeyPressCondition(val keyCode: Int) : TriggerCondition() { // TODO mouse buttons
    override fun check(): Boolean { // handled in key input event
        return false
    }

    fun checkKey(pressedKeyCode: Int): Boolean {
        return pressedKeyCode == keyCode
    }
}
