package catgirlroutes.utils.customtriggers.conditions

sealed class TriggerCondition {
    abstract fun check(): Boolean
}