package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.AlwaysActive
import catgirlroutes.module.settings.impl.*
import catgirlroutes.ui.clickgui.util.Alignment
import catgirlroutes.ui.clickgui.util.FontUtil.drawAlignedString
import catgirlroutes.ui.clickgui.util.FontUtil.getWidth
import catgirlroutes.ui.clickgui.util.VAlignment
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.autop3.Ring
import catgirlroutes.utils.autop3.RingsManager
import catgirlroutes.utils.autop3.RingsManager.blinkEditMode
import catgirlroutes.utils.autop3.actions.BlinkRing
import catgirlroutes.utils.render.HUDRenderUtils.displayHeight
import catgirlroutes.utils.render.HUDRenderUtils.displayWidth
import catgirlroutes.utils.render.HUDRenderUtils.sr
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import java.awt.Color

@AlwaysActive
object Blink : Module(
    "Blink",
    category = Category.DUNGEON
) {
    val renderAutoP3Text by BooleanSetting("AutoP3 text", true, "Renders packets amount on AutoP3 rings.")
    private val alwaysRender by BooleanSetting("Always render packets", true, "Displays the packet amount on the screen at all times, rather than only when the module is active.")
    private val recordBind by KeyBindSetting("Blink record", Keyboard.KEY_NONE, "Starts recording a blink if you are on a blink ring and in edit mode.")
        .onPress {
            if (recorderActive) {
                recorderActive = false
                modMessage("Done recording")
                RingsManager.loadSaveRoute()
                return@onPress
            }
            if (RingsManager.currentRoute.isEmpty()) return@onPress
            if (RingsManager.currentRoute.first().rings.isEmpty()) return@onPress
            RingsManager.currentRoute.first().rings.forEach { ring ->
                if (ring.inside() && ring.action is BlinkRing) {
                    if (!blinkEditMode) return@onPress modMessage("Enable blink edit mode: /p3 bem")
                    modMessage("Started recording")
                    mc.thePlayer.setPosition(ring.position.xCoord, mc.thePlayer.posY, ring.position.zCoord)
                    recorderActive = true
                    currentRing = ring
                    ring.action.packets = mutableListOf()
                }
            }
        }
    val lineColour by ColorSetting("AutoP3 line colour", Color.PINK, description = "Blink line colour for AutoP3 module.", collapsible = false)
    private val recordLength by NumberSetting("Recording length", 28.0, 1.0, 50.0, description = "Maximum blink recording length.", unit = " packets")
    private val clearPackets by ActionSetting("Clear packets") { packetArray = 0 }
    private val hud by HudSetting {
        at(displayWidth / 2.0 / sr.scaleFactor - "0".getWidth() / 2, displayHeight / 2.0 / sr.scaleFactor)
        width { packetArray.toString().getWidth() }
        visibleIf { packetArray != 0 && mc.ingameGUI != null }
        render { drawPackets() }
        preview { drawPackets() }
    }

    var packetArray = 0
    private var recorderActive = false
    private var currentRing: Ring? = null

    @SubscribeEvent
    fun onPacketRecorder(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer || !recorderActive) return
        val packets = (currentRing!!.action as BlinkRing).packets
        if (packets.size + 1 == recordLength.toInt()) {
            recorderActive = false
            RingsManager.loadSaveRoute()
            modMessage("Done recording")
        }
        if (event.packet is C06PacketPlayerPosLook || event.packet is C04PacketPlayerPosition) {
            val lastPacket = packets.lastOrNull()
            println(event.packet.yaw)
            val newPacket = BlinkC06(
                event.packet.yaw,
                event.packet.pitch,
                event.packet.positionX,
                event.packet.positionY,
                event.packet.positionZ,
                event.packet.isOnGround
            )

            if (lastPacket != newPacket) {
                packets.add(newPacket)
            }
        }
    }

    data class BlinkC06(
        val yaw: Float,
        val pitch: Float,
        val x: Double,
        val y: Double,
        val z: Double,
        val onGround: Boolean = mc.thePlayer.onGround
    )

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        packetArray = 0
    }

    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    private var lastYaw = 0f
    private var lastPitch = 0f

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer || !this.enabled) return
        val currentTime = System.currentTimeMillis()
        var dontCancel = false
        if (event.packet.rotating) {
            if (lastYaw != event.packet.yaw || lastPitch != event.packet.pitch) dontCancel = true
            lastYaw = event.packet.yaw
            lastPitch = event.packet.pitch
        }
        if (event.packet.isMoving) {
            if (lastX != event.packet.positionX || lastY != event.packet.positionY || lastZ != event.packet.positionZ) dontCancel = true
            lastX = event.packet.positionX
            lastY = event.packet.positionY
            lastZ = event.packet.positionZ
        }
        if (dontCancel) return
        event.isCanceled = true
        packetArray += 1
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) { // FIXME
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || mc.ingameGUI == null || packetArray == 0 || !alwaysRender) return
        GlStateManager.pushMatrix()
        GlStateManager.translate(hud.x, hud.y, 1.0)
        GlStateManager.scale(hud.scale, hud.scale, 1.0)
        drawPackets()
        GlStateManager.popMatrix()
    }

    private fun drawPackets() {
        drawAlignedString(packetArray.toString(), hud.width / 2.0, hud.height / 2.0, Alignment.CENTRE, VAlignment.CENTRE)
    }
}