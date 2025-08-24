package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.HudSetting
import catgirlroutes.ui.clickgui.util.FontUtil.drawStringWithShadow
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.utils.PriceUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ItemValue : Module(
    "Item Value",
    Category.MISC,
    tag = TagType.WHIP
) { // todo: prices in tooltips; container value; dung quality in tooltips
    private val valueBreakdown by HudSetting("Item value hud") {
        size(199, 252)
        preview {
            val dummy = PriceUtils.ItemValueBreakdown().apply {
                addText("Base", "§6Hyperion", 908999999.0)
                addText("Reforge", "Ancient", 3000000.0)
                add("Recombobulator", 7972100.396296294)

                addMulti("Scrolls", mapOf(
                    "§5Implosion" to 500000000.0,
                    "§5Shadow Warp" to 241672255.0
                ))

                addText("HPBs", "§e7§7/§e10", 863239.464987)
                addText("Fumings", "§e3§7/§e5", 5969361.726114649)
                addText("Stars", "§e4§7/§e5", 9356973.488849277)

                addMulti("Gemstones", mapOf(
                    "❈ Perfect Amethyst Gemstone" to 8000000.0
                ))

                addMulti("Enchantments", mapOf(
                    "§9§d§lUltimate Wise 5" to 2014996.428,
                    "§9Luck 6" to 2142.5523809523806,
                    "§9Critical 6" to 1770.8028901734106,
                    "§9Smite 7" to 13619993.1,
                    "§9Looting 4" to 18123.674782608698,
                    "§9Syphon 4" to 347178.05555555556,
                    "§9Ender Slayer 6" to 1613996.6928571425,
                    "§9Giant Killer 6" to 1311137.3,
                    "§9Thunderlord 6" to 3946.8033149171265,
                    "§9Lethality 6" to 72264.235
                ))
            }
            drawBreakdown(dummy)
        }
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if (mc.theWorld == null) return

        if (valueBreakdown.enabled) {
            val stack = (mc.currentScreen as? GuiContainer)?.slotUnderMouse?.stack ?: return
            val breakdown = PriceUtils.getItemValue(stack).second ?: return

            drawBreakdown(breakdown, valueBreakdown.x, valueBreakdown.y)
        }
    }

    private fun drawBreakdown(breakdown:  PriceUtils.ItemValueBreakdown, x: Double = 0.0, y: Double = 0.0) {
        var yPos = y
        breakdown.format().forEach { line ->
            drawStringWithShadow(line, x, yPos)
            yPos += fontHeight + 2
        }
    }
}