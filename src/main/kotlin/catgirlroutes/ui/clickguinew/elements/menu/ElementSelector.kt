package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.ui.animations.impl.EaseOutQuadAnimation
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseX
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseY
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import catgirlroutes.utils.render.StencilUtils
import java.awt.Color

class ElementSelector(parent: ModuleButton, setting: SelectorSetting) : // todo use misc element
    Element<SelectorSetting>(parent, setting, ElementType.SELECTOR) {

    private val extendAnimation = EaseOutQuadAnimation(300)

    override fun renderElement(): Double {
        height = this.extendAnimation.get(13.0, this.setting.options.size * 13.0 + 13.0, !extended)
        val displayValue = "$displayName: ${this.setting.selected}"

        drawRoundedBorderedRect(0.0, 0.0, width, height, 3.0, 1.0, ColorUtil.bgColor.darker(),  ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(0.0, 0.0, width, 13.0, 3.0, 1.0, ColorUtil.bgColor.darker(),  ColorUtil.clickGUIColor)
        FontUtil.drawTotalCenteredString(displayValue, width / 2.0, 13.0 / 2.0)

        if (!extended && !this.extendAnimation.isAnimating()) return height

        StencilUtils.write(false)
        drawRoundedRect(0.0, 0.0, width, height, 3.0, Color.WHITE)
        StencilUtils.erase(true)
        this.setting.options.forEachIndexed { i, option ->
            val yOff = (i + 1) * 13.0
            if (this.isHovered(yOff = yOff.toInt())) {
                drawRoundedBorderedRect(0.0, yOff, width, 13.0, 3.0, 1.0, ColorUtil.outlineColor.darker(), ColorUtil.outlineColor)
            }
            FontUtil.drawTotalCenteredString(option, width / 2.0, yOff + 13.0 / 2.0)
        }
        StencilUtils.dispose()

        return height
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (this.isHovered()) {
                this.setting.index += 1
                return true
            }

            if (!extended) return false

            this.setting.options.forEachIndexed { i, option ->
                val yOff = (i + 1) * 13
                if (this.isHovered(yOff = yOff)) {
                    this.setting.selected = option
                    return true
                }
            }
        } else if (mouseButton == 1 && this.isHovered()) {
            if (this.extendAnimation.start()) extended = !extended
            return true
        }
        return super.mouseClicked(mouseButton)
    }

    private fun isHovered(xOff: Int = 0, yOff: Int = 0): Boolean {
        return mouseX >= xAbsolute + xOff && mouseX <= xAbsolute + width + xOff &&
                mouseY >= yAbsolute + yOff && mouseY <= yAbsolute + 13.0 + yOff
    }
}