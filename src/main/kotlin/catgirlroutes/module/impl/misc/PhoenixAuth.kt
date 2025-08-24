package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.ChatPacket
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.render.ClickGui
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.ClientListener.scheduleTask
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object PhoenixAuth: Module(
    "Phoenix Auth",
    Category.MISC,
    "Automatically authenticate and toggle modules when connecting to your phoenix proxy."
) {
    val phoenixProxy by StringSetting("Proxy IP", "penixhypixel.duckdns.org", 0, "Your phoenix proxy IP.")
    private val phoenixModule by SelectorSetting("Module", "autoterms", arrayListOf("autoterms", "zeropingterminals"))
    val addToMainMenu by BooleanSetting("Main menu button").withDependency { ClickGui.customMenu }
    private var onPenix = false

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        this.onPenix = mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.lowercase()?.equals(phoenixProxy)
                ?: currentServerData?.serverIP?.lowercase()?.equals(phoenixProxy)) == true)
        }.getOrDefault(false)
        if (this.onPenix) scheduleTask(1) { ChatUtils.commandAny("phoenixauth") }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        this.onPenix = false
    }

    @SubscribeEvent
    fun onChat(event: ChatPacket) {
        if (this.onPenix && event.message == "[Phoenix] Successfully transferred!") {
            scheduleTask(1) { ChatUtils.sendChat("p.toggle ${this.phoenixModule.selected}") }
        }
    }
}