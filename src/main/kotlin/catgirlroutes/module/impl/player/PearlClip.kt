package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.PlayerUtils.findDistanceToAirBlocks
import catgirlroutes.utils.PlayerUtils.findDistanceToAirBlocksLegacy
import catgirlroutes.utils.PlayerUtils.swapFromName
import catgirlroutes.utils.SwapState
import catgirlroutes.utils.rotation.FakeRotater.rotate
import catgirlroutes.utils.rotation.Rotater.Companion.shouldClick
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs
import kotlin.math.floor

object PearlClip : Module(
    "Pearl Clip",
    category = Category.PLAYER,
    description = "Clips you down selected blocks using an ender pearl."
){
    private val pearlClipDistance by NumberSetting("Distance", 20.0, 0.0, 80.0, 1.0, description = "Distance to clip down")
    private val pearlClipDelay by NumberSetting("Delay", 0.0, 0.0, 10.0, 1.0, description = "Pearl clip delay", unit = "ms")
    private val legacyDetection by BooleanSetting("Legacy detection")

    private var active = false
    override fun onKeyBind() {
        if (!this.enabled) return
        modMessage("Pearl clipping!")
        pearlClip((pearlClipDistance * -1))
    }

    private var clipDepth: Double? = 0.0
    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0

    fun pearlClip(depth: Double? = findDistanceToAirBlocks()) { // todo: move to ClipUtils
        if (!this.enabled) return
        clipDepth = if (depth == 0.0 && !legacyDetection) findDistanceToAirBlocks() else if (depth == 0.0) findDistanceToAirBlocksLegacy() else depth; // fuck k*tlin
        if (clipDepth == null) return

        posX = mc.thePlayer.posX
        posY = mc.thePlayer.posY + 1.0
        posZ = mc.thePlayer.posZ

        val swapResult = swapFromName("ender pearl")
        if (swapResult != SwapState.UNKNOWN) {
            modMessage("Pearl clipping ($depth)!")
            active = true
            rotate(mc.thePlayer.rotationYaw, 90F)
            scheduleTask(pearlClipDelay.toInt()) {
                shouldClick = true
            }
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketReceiveEvent) {
        if (event.packet !is S08PacketPlayerPosLook || !active) return
        scheduleTask(0) {
            if (event.isCanceled) return@scheduleTask
            active = false
            mc.thePlayer.setPosition(floor(posX) + 0.5, posY + -abs(clipDepth!!), floor(posZ) + 0.5)
        }
    }
}
