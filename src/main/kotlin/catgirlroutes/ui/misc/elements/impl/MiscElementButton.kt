package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.ellipsize
import catgirlroutes.ui.misc.elements.ElementDSL
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.ui.misc.elements.MiscElementStyle
import catgirlroutes.ui.misc.elements.util.calculateTextPosition
import catgirlroutes.utils.render.HUDRenderUtils

class MiscElementButton(
    style: MiscElementStyle = MiscElementStyle(),
) : MiscElement(style) {

    override fun render(mouseX: Int, mouseY: Int) {
        val isHovered = isHovered(mouseX, mouseY)
        HUDRenderUtils.drawRoundedBorderedRect(
            x, y, width, height, radii, thickness,
            if (isHovered) hoverColour else colour,
            if (isHovered) outlineHoverColour else outlineColour
        )
        val (x, y) = this.calculateTextPosition()
        FontUtil.drawAlignedString(value.ellipsize(width - 3.0), x, y, this.style.alignment, color = style.textColour.rgb, shadow = style.textShadow)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}

class ButtonBuilder : ElementDSL<MiscElementButton>() {
    var text by _style::value
    override fun buildElement(): MiscElementButton {
        return MiscElementButton(createStyle())
    }
}

fun button(block: ButtonBuilder.() -> Unit): MiscElementButton {
    return ButtonBuilder().apply(block).build()
}