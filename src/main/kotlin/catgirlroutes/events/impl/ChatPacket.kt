package catgirlroutes.events.impl

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class ChatPacket(val message: String, val formatted: String) : Event()