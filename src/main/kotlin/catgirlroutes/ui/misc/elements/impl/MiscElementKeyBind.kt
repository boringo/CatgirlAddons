package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.animations.impl.ColorAnimation
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.getWidth
import catgirlroutes.ui.misc.elements.ElementDSL
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.ui.misc.elements.MiscElementStyle
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class MiscElementKeyBind(
    var keyCode: Int = 0,
    style: MiscElementStyle = MiscElementStyle()
) : MiscElement(style) {

    var name = "None"
    var listening = false

    private val colourAnimation = ColorAnimation(100)

    override fun render(mouseX: Int, mouseY: Int) {
        name = if (this.keyCode > 0) Keyboard.getKeyName(this.keyCode) ?: "Err"
        else if (this.keyCode < 0) Mouse.getButtonName(this.keyCode + 100)
        else "None"

        this.width = name.getWidth()

        val colour = this.colourAnimation.get(colour, outlineColour, listening)
        val colour2 = this.colourAnimation.get(outlineColour.darker().darker(), hoverColour, listening)
        drawRoundedBorderedRect(x, y, width + 5.0, height, 3.0, 1.0, colour2, colour)
        FontUtil.drawString(name, x + 3.0, y + 2.0)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && this.isHovered(mouseX, mouseY) && !listening) {
            if (this.colourAnimation.start()) listening = true
            return true
        } else if (listening) {
            this.keyCode = -100 + mouseButton
            if (this.colourAnimation.start()) listening = false
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!listening) return super.keyTyped(typedChar, keyCode)

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                this.keyCode = Keyboard.KEY_NONE
                if (this.colourAnimation.start()) listening = false
            }
            Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> {
                if (this.colourAnimation.start()) listening = false
            }
            else -> {
                this.keyCode = keyCode
                if (this.colourAnimation.start()) listening = false
            }
        }
        return true
    }
}


class KeyBindBuilder : ElementDSL<MiscElementKeyBind>() {
    var keyCode: Int = 0
    override fun buildElement(): MiscElementKeyBind {
        return MiscElementKeyBind(keyCode, createStyle())
    }
}

fun keyBind(block: KeyBindBuilder.() -> Unit): MiscElementKeyBind {
    return KeyBindBuilder().apply(block).build()
}