package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PreKeyInputEvent
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.ui.misc.customtriggers.TriggerGUI
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.customtriggers.TriggerManager
import catgirlroutes.utils.customtriggers.testTrigger
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

object CustomTriggers: Module(
    "Custom Triggers",
    tag = TagType.WHIP
) {
    private val addTrigger by ActionSetting("ADD TEST TRIGGER") {
        val trigger = testTrigger()
        TriggerManager.triggers.add(trigger)
    }

    private val triggerInfo by ActionSetting("TRIGGER INFO") {
        modMessage(TriggerManager.triggers.size)
        TriggerManager.triggers.forEach {
            modMessage("""
                ${it.name}
                ${it.id}
                ${it.actions}
                ${it.conditions}
                -----------------
            """.trimIndent())
        }
    }

    private val gui by ActionSetting("GUI") {
        display = TriggerGUI()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.thePlayer == null || event.phase != TickEvent.Phase.START) return
        TriggerManager.onTick()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        TriggerManager.onChat(event)
    }

    @SubscribeEvent
    fun onSound(event: PlaySoundEvent) {
        TriggerManager.onSound(event)
    }

    @SubscribeEvent
    fun onKeyInput(event: PreKeyInputEvent) {
        if (!Keyboard.getEventKeyState()) return
        TriggerManager.onKeyInput(Keyboard.getEventKey())
    }


}

/**
 * action:
 * send message
 * replace message
 * hide message
 * start timer (name, time (ms or server ticks))
 * command (auto, client, server)
 * press key
 * play sound (name, volume, pitch)
 * show on screen notification
 *
 * condition:
 * location (hub, dungeon, floor, etc)
 * dungeon class
 * holding item (name, sb id)
 * player position
 * skyblock stat (hp, def, etc)
 * message
 * key pressed
 * sound played (name, volume, pitch)
 * command (as alias)
 *
 * delay before running the action after all conditions are met (ms or server ticks)
 */