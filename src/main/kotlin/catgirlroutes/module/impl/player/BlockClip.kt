package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.MovementUtils
import catgirlroutes.utils.Notifications
import catgirlroutes.utils.PlayerUtils.posX
import catgirlroutes.utils.PlayerUtils.posY
import catgirlroutes.utils.PlayerUtils.posZ
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object BlockClip : Module(
    "Block Clip",
    Category.PLAYER,
    "Clips you through blocks"
) {
    private val distance by NumberSetting("Distance", 1.25, 0.0, 1.5, 0.01)
    private val directions by SelectorSetting("Directions", "All", arrayListOf("All", "Axis"))
    private val notifications by BooleanSetting("Notifications", false, "Makes Block Clip send notification on activation.")

    private var clipping = false

    override fun onKeyBind() {
        if ((!mc.thePlayer.onGround) || !this.enabled) return
        if (this.notifications) Notifications.send("Block clipping") else modMessage("Block clipping")
        MovementUtils.stopMovement()
        this.blockClip(0.062)
        clipping = true
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || !clipping) return
        this.blockClip(this.distance)
        MovementUtils.restartMovement()
        clipping = false
    }

    fun blockClip(distance: Double) {
        val radians = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val offsetX = distance * -sin(radians)
        val offsetZ = distance * cos(radians)

        val finalX = posX + if (this.directions.selected == "All" || abs(offsetX) > abs(offsetZ)) offsetX else 0.0
        val finalZ = posZ + if (this.directions.selected == "All" || abs(offsetZ) >= abs(offsetX)) offsetZ else 0.0

        mc.thePlayer.setPosition(finalX, posY, finalZ)
    }
}