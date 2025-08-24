package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes
import catgirlroutes.events.impl.ChatPacket
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.utils.ChatUtils.command
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.dungeon.DungeonUtils
import catgirlroutes.utils.dungeon.Floor
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoPot: Module(
    "Auto Pot",
    Category.DUNGEON,
    "Automatically gets a potion from your potion bag."
){
    private val potOnStart by BooleanSetting("On start", "Gets a pot on dungeon start.")
    private val m7Only by BooleanSetting("M7 only", "Gets a pot only when the player is in M7.").withDependency { potOnStart }

    @SubscribeEvent
    fun onChat(event: ChatPacket) {
        if (!potOnStart || (m7Only && DungeonUtils.floor != Floor.M7)) return
        if (event.message.matches(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\."))) {
            activatePot()
        }
    }

    override fun onKeyBind() {
        activatePot()
    }

    private var awaitPot = false

    private fun activatePot() {
        modMessage("Getting pot!")
        awaitPot = true
        command("pb", false)
    }

    @SubscribeEvent
    fun onS2D(event: PacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow || !awaitPot) return
        if (event.packet.windowTitle?.unformattedText != "Potion Bag") return
        awaitPot = false
        val cwid = event.packet.windowId
        event.isCanceled = true
        scheduleTask(0) {
            if (cwid == -1) return@scheduleTask
            CatgirlRoutes.mc.netHandler.addToSendQueue(C0EPacketClickWindow(cwid, 0, 0, 0, null, 0))
            CatgirlRoutes.mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
        }
    }
}