package catgirlroutes.events.impl

import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.Event

open class TermOpenEvent(val packet: S2DPacketOpenWindow) : Event()