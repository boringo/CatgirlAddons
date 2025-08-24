package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.utils.customtriggers.TypeName

@TypeName("CommandCondition")
class CommandCondition(val alias: String) : TriggerCondition() {
    override fun check(): Boolean { // handled in different event TODO SKIBIDI FROM ST
        return false
    }
}