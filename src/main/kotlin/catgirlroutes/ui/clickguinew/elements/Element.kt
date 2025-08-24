package catgirlroutes.ui.clickguinew.elements

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.ui.clickgui.util.FontUtil.wrapText
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseX
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseY
import catgirlroutes.ui.clickguinew.ClickGUI
import catgirlroutes.utils.render.HUDRenderUtils.disableScissor
import catgirlroutes.utils.render.HUDRenderUtils.drawHoveringText
import catgirlroutes.utils.render.HUDRenderUtils.enableScissor
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

abstract class Element<S: Setting<*>>(
    val parent: ModuleButton,
    val setting: S,
    val type: ElementType
) {
    private val clickGui: ClickGUI = parent.window.clickGui

    var x = 0.0
    var y = 0.0

    var width = this.parent.width * 0.836
    var height: Double

    var displayName: String = this.setting.name
    var extended = false
    var listening = false

    val xAbsolute: Double
        get() = this.x + this.parent.window.x

    val yAbsolute: Double
        get() = this.y + this.parent.window.y

    val mouseXRel: Int get() = mouseX - xAbsolute.toInt()
    val mouseYRel: Int get() = mouseY - yAbsolute.toInt()

    private var hoverStartTime: Long? = null

    init {
        this.height = when (this.type) {
            ElementType.TEXT_FIELD -> 25.0
            ElementType.SLIDER -> 18.0
            ElementType.BOOLEAN -> 11.0
            ElementType.KEY_BIND -> 11.0
            ElementType.HUD -> if (setting.name.isEmpty()) -5.0 else 11.0
            else -> DEFAULT_HEIGHT
        }
    }

    fun update() {
        displayName = setting.name
        when (type) {
            ElementType.SELECTOR -> {
                height = if (extended)
                    ((setting as SelectorSetting).options.size * DEFAULT_HEIGHT + DEFAULT_HEIGHT)
                else
                    DEFAULT_HEIGHT
            }
            ElementType.COLOR -> {
                height =
                    if ((setting as ColorSetting).collapsible && !extended) {
                        DEFAULT_HEIGHT
                    } else {
                        DEFAULT_HEIGHT * 9 + 5.0
                    }
            }
            else -> {}
        }
    }

    fun draw() : Double {
        GlStateManager.pushMatrix()
        GlStateManager.translate(this.x, this.y, 0.0)

        val elementLength = renderElement()

        if (isHoveredTemp(mouseX, mouseY) && this.setting.description != null && this.parent.extended) {
            val currentTime = System.currentTimeMillis()
            hoverStartTime = hoverStartTime ?: currentTime

            if (currentTime - hoverStartTime!! >= 1000) {
                disableScissor()
                drawHoveringText(wrapText(setting.description!!, 150.0), mouseX - xAbsolute.toInt(), mouseY - yAbsolute.toInt())
                enableScissor()
            }
        } else {
            hoverStartTime = null
        }

        GlStateManager.popMatrix()
        return elementLength + 5.0
    }

    protected open fun renderElement() : Double { return this.height }

    open fun mouseClicked(mouseButton: Int): Boolean {
        return this.isHovered()
    }

    open fun mouseReleased(state: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean { return false }

    open fun mouseClickMove(mouseButton: Int, timeSinceLastClick: Long) {  }

    open fun onGuiClosed() {  }

    open fun getElementHeight(): Double = height

    private fun isHovered(): Boolean {
        return mouseX >= this.xAbsolute && mouseX <= this.xAbsolute + this.width && mouseY >= this.yAbsolute && mouseY <= this.yAbsolute + this.height
    }

    private fun isHoveredTemp(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= this.xAbsolute && mouseX <= this.xAbsolute + this.width && mouseY >= this.yAbsolute && mouseY <= this.yAbsolute + DEFAULT_HEIGHT
    }

    companion object {
        const val DEFAULT_HEIGHT = 13.0
    }
}

