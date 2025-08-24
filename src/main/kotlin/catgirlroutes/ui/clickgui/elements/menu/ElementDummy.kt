package catgirlroutes.ui.clickgui.elements.menu

import catgirlroutes.module.settings.Setting
import catgirlroutes.ui.clickgui.elements.Element
import catgirlroutes.ui.clickgui.elements.ElementType
import catgirlroutes.ui.clickgui.elements.ModuleButton
import catgirlroutes.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.Gui
import java.awt.Color

class ElementDummy(parent: ModuleButton, setting: Setting<*>) :
    Element<Setting<*>>(parent, setting, ElementType.DUMMY) {

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Int {
        Gui.drawRect(width, 2, width, 13, Color.WHITE.rgb)
        FontUtil.drawString("THIS ELEMENT IS NOT IMPLEMENTED IN THIS GUI", 1, 3)
        return super.renderElement(mouseX, mouseY, partialTicks)
    }
}