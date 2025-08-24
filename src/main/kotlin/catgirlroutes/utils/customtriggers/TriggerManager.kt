package catgirlroutes.utils.customtriggers

import catgirlroutes.utils.configList
import catgirlroutes.utils.customtriggers.actions.PressKeyAction
import catgirlroutes.utils.customtriggers.actions.SendMessageAction
import catgirlroutes.utils.customtriggers.conditions.PositionCondition
import catgirlroutes.utils.customtriggers.actions.TriggerAction
import catgirlroutes.utils.customtriggers.conditions.TriggerCondition
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.sound.PlaySoundEvent
import org.lwjgl.input.Keyboard

object TriggerManager {
    val triggers by configList<Trigger>("custom_triggers.json")

    fun onTick() {
        triggers.forEach { trigger ->
            if (trigger.checkConditions()) {
                trigger.executeActions()
            }
        }
    }

    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText

        triggers.forEach { trigger ->
            if (trigger.hasMessageCondition()) {
                if (trigger.checkConditions(message = message)) {
                    if (trigger.shouldCancelMessage()) {
                        event.isCanceled = true
                    }
                    trigger.executeActions()
                }
            }
        }
    }

    fun onSound(event: PlaySoundEvent) {
        triggers.forEach { trigger ->
            if (trigger.hasSoundCondition()) {
                if (trigger.checkConditions(sound = Sound(event.name, event.sound.volume, event.sound.pitch))) {
                    trigger.executeActions()
                }
            }
        }
    }

    fun onKeyInput(keyCode: Int) {
        triggers.forEach { trigger ->
            if (trigger.hasKeyCondition()) {
                if (trigger.checkConditions(keyCode = keyCode)) {
                    trigger.executeActions()
                }
            }
        }
    }
}

data class ActionWithDelay(val action: TriggerAction, val delay: Long = 0, val delayTicks: Int = 0)
data class Sound(val name: String, val volume: Float = 1.0f, val pitch: Float = 1.0f)


// TEMP
class TriggerBuilder {
    private val conditions = mutableListOf<TriggerCondition>()
    private val actions = mutableListOf<ActionWithDelay>()

    fun addCondition(condition: TriggerCondition): TriggerBuilder {
        conditions.add(condition)
        return this
    }

    fun addAction(action: TriggerAction, delayMs: Long = 0): TriggerBuilder {
        actions.add(ActionWithDelay(action, delayMs))
        return this
    }

    fun build(): Trigger {
        return Trigger(conditions, actions, "some group")
    }
}

fun testTrigger(): Trigger {
    return TriggerBuilder()
        .addCondition(PositionCondition(103.0, 7.0, 79.0, 2.0))
        .addAction(SendMessageAction("TEST"))
        .addAction(PressKeyAction(Keyboard.KEY_SPACE))
        .build()
}