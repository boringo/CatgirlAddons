package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.KeyBindSetting
import catgirlroutes.utils.ChatUtils.command
import catgirlroutes.utils.renderText
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoWardrobe : Module(
    "Auto Wardrobe",
    Category.MISC
) {

    private val wardrobes = (36..44).map { slot ->
        +KeyBindSetting("Wardrobe ${slot-35}").onPress {
            if (!enabled) return@onPress
            targetSlot = slot
            openMenu()
        }
    }

    private var active = false
    private var targetSlot = 0
    private var cwid = -1

    private fun openMenu() {
        active = true
        command("wd", false)
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !active || mc.ingameGUI == null) return
        renderText("§5[§dEquipping WD:§c ${targetSlot - 35}§5]")
    }

    @SubscribeEvent
    fun onS2D(event: PacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        val title = event.packet.windowTitle.unformattedText
        cwid = event.packet.windowId

        if (!title.contains("Wardrobe") || !active) return
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onS2F(event: PacketReceiveEvent) {
        if (event.packet !is S2FPacketSetSlot) return
        if (!active) return
        val slot = event.packet.func_149173_d()
        if (slot == targetSlot) {
            click(slot)
            active = false
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            cwid = -1
        }
        if (slot > 45) {
            cwid = -1
            active = false
        }
    }

    fun click(slot: Int) {
        if (cwid == -1) return
        mc.netHandler.addToSendQueue(C0EPacketClickWindow(cwid, slot, 0, 0, null, 0))
    }
}
