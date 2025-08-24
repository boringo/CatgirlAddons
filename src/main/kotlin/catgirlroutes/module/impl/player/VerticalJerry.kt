package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.LocationManager
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object VerticalJerry : Module(
    "Vertical Jerry",
    Category.PLAYER,
    "Cancels horizontal velocity from Jerry-chine Gun."
){
    @SubscribeEvent
    fun onPacket(event: PacketReceiveEvent) {
        if (!LocationManager.inSkyblock || event.packet !is S12PacketEntityVelocity || event.packet.entityID != mc.thePlayer.entityId) return
        if (event.packet.motionY == 4800) {
            event.isCanceled = true
            mc.thePlayer.setVelocity(mc.thePlayer.motionX, 0.6, mc.thePlayer.motionZ)
        }
    }
}