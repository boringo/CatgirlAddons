package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.ChatPacket
import catgirlroutes.events.impl.ServerTickEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.HudSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.ui.clickgui.util.FontUtil.drawStringWithShadow
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickgui.util.FontUtil.getWidth
import catgirlroutes.utils.dungeon.DungeonUtils.getMageCooldownMultiplier
import catgirlroutes.utils.dungeon.DungeonUtils.inBoss
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import catgirlroutes.utils.render.HUDRenderUtils.renderRect
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object InvincibilityTimer : Module(
    "Invincibility Timer",
    Category.MISC
) {
    private val dungeonOnly by BooleanSetting("Dungeons only", "Active in dungeons only.")
    private val bossOnly by BooleanSetting("Boss only", "Active in boss room only.")
    private val serverTicks by BooleanSetting("Use server ticks", "Uses server ticks instead of real time.")
    private val mageReduction by BooleanSetting("Account for mage cooldown reduction")
    private val colour by ColorSetting("Colour", Color.PINK, false)
    private val cataLevel by NumberSetting("Catacombs level", 0.0, 0.0, 50.0, 1.0, "Catacombs level for Bonzo's mask ability")
    private val hud by HudSetting {
        size("Phoenix: 30.2".getWidth() + 6, fontHeight * 3)
        visibleIf { (!bossOnly || inBoss) && (!dungeonOnly || inDungeons || bossOnly) }
        render {
            val name = mc.thePlayer?.inventory?.armorInventory?.get(3)?.displayName
            val offset = when {
                name?.contains("Bonzo") == true -> 0.0
                name?.contains("Spirit") == true -> 10.0
                else -> 10000.0
            }

            val bonzoReady = stupid("Bonzo", bonzoTicks, phoenix && offset == 0.0 && phoenixTicks < 0.0)
            val spiritReady = stupid("Spirit", spiritTicks, phoenix && offset == 10.0 && phoenixTicks < 0.0)
            val phoenixReady = stupid("Phoenix", phoenixTicks, phoenix && ((offset == 10.0 && spiritTicks < 0.0) || (offset == 0.0 && bonzoTicks < 0.0)))

            drawStringWithShadow(bonzoReady, 6.0, 0.0, colour.rgb)
            drawStringWithShadow(spiritReady, 6.0, 10.0, colour.rgb)
            drawStringWithShadow(phoenixReady, 6.0, 20.0, colour.rgb)

            renderRect(0.0, offset + 2.0, 3.0, 3.0, colour)
            if (phoenix) renderRect(0.0, 22.0, 3.0, 3.0, colour)
        }

        preview {
            drawStringWithShadow("Bonzo: §a✔", 6.0, 0.0, colour.rgb)
            drawStringWithShadow("Spirit: §a✔", 6.0, 10.0, colour.rgb)
            drawStringWithShadow("Phoenix: §c30.2", 6.0, 20.0, colour.rgb)
            renderRect(0.0, 2.0, 3.0, 3.0, colour)
        }
    }

    private var spiritTicks = 0
    private var bonzoTicks = 0
    private var phoenixTicks = 0
    private var phoenix = false

    @SubscribeEvent
    fun onChat(event: ChatPacket) {
        val msg = event.message
        when {
            "Second Wind Activated! Your Spirit Mask saved your life!" in msg -> spiritTicks = (600 * if (this.mageReduction) getMageCooldownMultiplier() else 1.0).toInt()
            "Bonzo's Mask saved your life!" in msg -> bonzoTicks = ((7200 - cataLevel.toInt() * 72) * if (this.mageReduction) getMageCooldownMultiplier() else 1.0).toInt()
            "Your Phoenix Pet saved you from certain death!" in msg -> phoenixTicks = 1200
            "You summoned your" in msg || "Autopet equipped your" in msg -> phoenix = msg.contains("Phoenix")
            "You despawned your Phoenix!" in msg -> phoenix = false
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase !== TickEvent.Phase.START || serverTicks) return
        spiritTicks -= 1
        phoenixTicks -= 1
        bonzoTicks -= 1
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (!serverTicks) return
        this.spiritTicks -= 1
        this.phoenixTicks -= 1
        this.bonzoTicks -= 1
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        this.spiritTicks = 0
        this.bonzoTicks = 0
        this.phoenixTicks = 0
        this.phoenix = false
    }

    private fun stupid(name: String, ticks: Int, highlight: Boolean): String {  // stupid thing for colour highlight
        val c = if (ticks < 0 && highlight) "§c" else if (ticks <= 0) "§a" else "§c"
        val time = if (ticks <= 0) "✔" else "%.1f".format(ticks / 20.0)
        return "$name: $c$time"
    }
}