package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.GuiContainerEvent
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.Island
import catgirlroutes.utils.LocationManager.currentArea
import catgirlroutes.utils.getTooltip
import catgirlroutes.utils.noControlCodes
import catgirlroutes.utils.render.HUDRenderUtils.highlight
import net.minecraft.inventory.Slot
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color.*

object PartyFinder : Module(
    name = "Party Finder",
    category = Category.DUNGEON
) {
    private var currClass: String = ""
    private var inPartyFinderGui = false
    private var coloredSlots = mutableListOf<Slots>()

    @SubscribeEvent
    fun onS2F(event: PacketReceiveEvent) {
        if (event.packet !is S2FPacketSetSlot) return
        if (currentArea !== Island.DungeonHub) return
        val displayName = event.packet.func_149174_e()?.displayName ?: return
        val tooltip = event.packet.func_149174_e().getTooltip()
        val classRegex = Regex("Currently Selected: (?<class>Mage|Berserk|Archer|Tank|Healer)", RegexOption.IGNORE_CASE)

        if (!displayName.contains("Dungeon Classes", true)) return

        for (line in tooltip) {
            val match = classRegex.find(line.noControlCodes)
            if (match !== null) {
                currClass = match.groupValues[1]
                break
            }
        }
        debugMessage(currClass)
    }

    private val foundClassRegex = Regex("(?<class>Mage|Berserk|Archer|Tank|Healer)")
    private val requirementRegex = Regex("Requires (Catacombs|a Class at) Level (\\d+)!")

    @SubscribeEvent
    fun onRender(event: GuiContainerEvent.DrawSlotEvent) {
        if (!inPartyFinderGui) return
        if (currentArea !== Island.DungeonHub) return
        coloredSlots.forEach{
            s -> s.slot.highlight(s.color)
        }
    }

    @SubscribeEvent
    fun onS2D(event: PacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        if (!inPartyFinderGui) return
        if (currentArea !== Island.DungeonHub) return

        scheduleTask(1) {
            val slots = mc.thePlayer.openContainer.inventorySlots.filter { it.hasStack }.filterNotNull()
            coloredSlots.clear()

            for (slot in slots) {
                val item = slot.stack
                val toolTip = item.getTooltip()
                var missingClass = true
                var requirementsFilled = true

                if (item.displayName.contains("s Party", true)) {
                    for (line in toolTip) {
                        val foundClass = foundClassRegex.find(line.noControlCodes)
                        if (foundClass != null) {
                            if (foundClass.value.noControlCodes == currClass.noControlCodes) missingClass = false
                        }
                        if (requirementRegex.matches(line.noControlCodes)) requirementsFilled = false
                    }

                    val color = when {
                        !requirementsFilled -> red
                        !missingClass -> magenta
                        else -> pink
                    }
                    coloredSlots.add(Slots(slot, color))
                }
            }
        }
    }

    @SubscribeEvent
    fun onWindowPacket(event: PacketReceiveEvent) {
        if (currentArea !== Island.DungeonHub) return
        if (event.packet is S2DPacketOpenWindow) {
            if (event.packet.windowTitle.unformattedText.noControlCodes.contains("Party Finder", true)) {
                inPartyFinderGui = true
            }
        }
        if (event.packet is S2EPacketCloseWindow) {
            inPartyFinderGui = false
        }
    }

    @SubscribeEvent
    fun onWindowClose(event: PacketSentEvent) {
        if (event.packet is C0DPacketCloseWindow) {
            inPartyFinderGui = false
        }
    }
}

data class Slots(val slot: Slot, val color: java.awt.Color)