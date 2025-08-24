package catgirlroutes.utils.customtriggers.actions

import catgirlroutes.utils.customtriggers.TypeName

@TypeName("TimerAction")
data class StartTimerAction(val name: String, val durationMs: Long) : TriggerAction() {
    override fun execute() {
//        TriggerEvents.onStartTimer(name, durationMs)
    }
}
