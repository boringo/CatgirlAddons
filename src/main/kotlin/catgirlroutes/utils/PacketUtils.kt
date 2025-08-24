package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import net.minecraft.network.Packet

object PacketUtils {

    fun sendPacket(packet: Packet<*>?) {
        mc.netHandler.networkManager.sendPacket(packet)
    }

    fun addToSendQueue(packet: Packet<*>?) {
        mc.netHandler.addToSendQueue(packet)
    }
}