package catgirlroutes.utils.customtriggers

import catgirlroutes.utils.customtriggers.actions.HideMessageAction
import catgirlroutes.utils.customtriggers.actions.ReplaceMessageAction
import catgirlroutes.utils.customtriggers.conditions.KeyPressCondition
import catgirlroutes.utils.customtriggers.conditions.MessageCondition
import catgirlroutes.utils.customtriggers.conditions.SoundCondition
import catgirlroutes.utils.customtriggers.actions.TriggerAction
import catgirlroutes.utils.customtriggers.conditions.TriggerCondition

class Trigger(
    val id: String,
    val name: String,
    val group: String? = null,
    var enabled: Boolean = false,
    val conditions: List<TriggerCondition>,
    val actions: List<ActionWithDelay>,
) {
    constructor(conditions: List<TriggerCondition>, actions: List<ActionWithDelay>, group: String? = null) :
            this("aboba", "trigger", group, false, conditions, actions)

    @Transient
    private var executedActions: MutableList<TriggerAction>? = null

    fun toggle() {
        this.enabled = !this.enabled
    }

    fun checkConditions(
        message: String? = null,
        sound: Sound? = null,
        keyCode: Int? = null
    ): Boolean {
        val passed = conditions.all { condition ->
            when (condition) {
                is MessageCondition -> {
                    if (message != null) condition.checkMessage(message) else condition.check()
                }
                is SoundCondition -> {
                    if (sound != null) condition.checkSound(sound.name, sound.volume, sound.pitch) else condition.check()
                }
                is KeyPressCondition -> {
                    if (keyCode != null) condition.checkKey(keyCode) else condition.check()
                }
                else -> condition.check()
            }
        }
        if (!passed) executedActions?.clear()
        return passed
    }

    fun executeActions() {
        actions.forEach { (action, _) ->
            if (executedActions?.contains(action) == true) return@forEach

            action.execute()
            executedActions?.add(action)
        }
    }

    fun hasMessageCondition(): Boolean {
        return conditions.any { it is MessageCondition }
    }

    fun hasSoundCondition(): Boolean {
        return conditions.any { it is SoundCondition }
    }

    fun hasKeyCondition(): Boolean {
        return conditions.any { it is KeyPressCondition }
    }

    fun shouldCancelMessage(): Boolean {
        return actions.any { it.action is HideMessageAction || it.action is ReplaceMessageAction }
    }
}