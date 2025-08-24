package catgirlroutes.ui.misc.searchoverlay

import catgirlroutes.module.impl.misc.Inventory.ahHistory
import catgirlroutes.utils.NeuRepo
import catgirlroutes.utils.RepoItem
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.ui.misc.elements.impl.boolean
import catgirlroutes.ui.misc.elements.impl.selector
import catgirlroutes.ui.misc.elements.util.update
import catgirlroutes.utils.render.HUDRenderUtils.renderRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.tileentity.TileEntitySign

class AuctionOverlay(sign: TileEntitySign? = null) : SearchOverlay(sign) {

    override var history: MutableList<String>
        get() = ahHistory.toMutableList()
        set(value) {
            ahHistory = value
        }

    override var commandPrefix: String = "ahs"

    private var lastSelected = "0"

    private var starSelector = selector {
        options("§6➊", "§6➋", "§6➌", "§6➍", "§6➎", "§c➊", "§c➋", "§c➌", "§c➍", "§c➎")
        horizontal(5, 5)
        size(15.0, 15.0)
        colour = ColorUtil.elementColor
    }

    private val petToggle = boolean {
        text = "Lvl 100 Pets"
        size(15.0, 15.0)
        disabledColour = ColorUtil.elementColor
        onClick {
            petLvl = if (enabled) "[Lvl 100] " else ""
        }
    }

    private var petLvl: String = ""

    override fun filterItems(query: String): List<RepoItem> {
        return NeuRepo.repoItems.filter { item ->
            item.auction &&
            item.name.contains(query, ignoreCase = true) &&
            (!petToggle.enabled || item.name.contains("Lvl"))
        }
    }


    override fun processItem(item: RepoItem): String {
        return "$petLvl${item.name}${getStars()}".clean
    }

    override fun drawScreenExtra(mouseX: Int, mouseY: Int) {
        searchBar.prependText = "§7${petLvl}§r"
        val ahX = x + guiWidth + 10.0
        val ahY = y + 25.0
        val ahW = 110.0
        val ahH = 85.0

        GlStateManager.translate(0.0, 0.0, -50.0)

        drawRoundedBorderedRect(ahX - 10.0, ahY - 5.0, ahW + 10.0, ahH + 10.0, 3.0, 2.0, ColorUtil.bgColor, ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(ahX, ahY, ahW - 5.0, 55.0, 3.0, 2.0, ColorUtil.bgColor, ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(ahX, ahY + 60.0, ahW - 5.0, 25.0, 3.0, 2.0, ColorUtil.bgColor, ColorUtil.clickGUIColor)

        GlStateManager.translate(0.0, 0.0, 100.0)
        renderRect(ahX - 10.0 + 4.0, ahY - 5.0 + 0.5, 2.0, ahH + 10.0 - 1.0, ColorUtil.bgColor)

        // don't care
        renderRect(ahX -10.0 + 4.0 + 0.5, ahY - 5.0 - 0.5, 1.0, 1.0, ColorUtil.bgColor)
        renderRect(ahX -10.0 + 4.0 + 1.5, ahY - 5.0 - 1.0, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(ahX -10.0 + 4.0 + 0.5, ahY - 5.0 - 1.0, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(ahX -10.0 + 4.0 + 1.5, ahY - 5.0, 0.5, 0.5, ColorUtil.clickGUIColor.darker())

        renderRect(ahX -10.0 + 4.0 + 0.5, ahY - 5.0 + ahH + 10.0 - 0.5, 1.0, 1.0, ColorUtil.bgColor)
        renderRect(ahX -10.0 + 4.0 + 1.5, ahY - 5.0 + ahH + 10.0 + 0.5, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(ahX -10.0 + 4.0 + 0.5, ahY - 5.0 + ahH + 10.0 + 0.5, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(ahX -10.0 + 4.0 + 1.5, ahY - 5.0 + ahH + 10.0 - 0.5, 0.5, 0.5, ColorUtil.clickGUIColor.darker())

        GlStateManager.translate(0.0, 0.0, -100.0)

        FontUtil.drawString("Stars:", ahX + 5.0, ahY + 5.0)

        starSelector.update {
            x = ahX + 5.0
            y = ahY + 15.0
        }.draw(mouseX, mouseY)
        petToggle.update {
            x = ahX + 5.0
            y = ahY + 65.0
        }.draw(mouseX, mouseY)

        GlStateManager.translate(0.0, 0.0, 50.0)
    }

    override fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {
        petToggle.onMouseClick(mouseX, mouseY, mouseButton)
        if (starSelector.onMouseClick(mouseX, mouseY, mouseButton)) {
            if (starSelector.selected == this.lastSelected) {
                starSelector.selected = "0"
                this.lastSelected = "00"
            } else {
                this.lastSelected = starSelector.selected
            }
        }
    }

    private fun getStars(): String {
        val res = StringBuilder(" ")
        when {
            starSelector.index in 1..5 -> res.append("✪".repeat(starSelector.index))
            starSelector.index > 5 -> res.append("✪".repeat(5)).append(starSelector.selected.last())
        }
        return res.toString()
    }
}