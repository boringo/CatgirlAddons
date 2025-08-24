package catgirlroutes.ui.hud

import catgirlroutes.CatgirlRoutes.Companion.moduleConfig
import catgirlroutes.ui.Screen
import catgirlroutes.ui.clickgui.util.MouseUtils.mx
import catgirlroutes.ui.clickgui.util.MouseUtils.my
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.button
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.equalsOneOf
import catgirlroutes.utils.render.HUDRenderUtils.renderRect
import java.awt.Color
import kotlin.math.abs

/**
 * The GUI for editing the positions and scale of HUD elements.
 *
 * @author Aton
 */
object EditHudGUI : Screen(false) {

    private val hudElements: ArrayList<HudElement> = arrayListOf()
    private var draggingElement: HudElement? = null
    private var startOffsetX = 0.0
    private var startOffsetY = 0.0

    private var snapLines = mutableListOf<Pair<Double, Boolean>>()

    private lateinit var resetButton: MiscElementButton

    fun addHUDElements(newElements: List<HudElement>) {
        val nonDuplicate = newElements.filter { !hudElements.contains(it) }
        hudElements.addAll(0, nonDuplicate)
    }

    override fun onInit() {
        this.resetButton = button {
            text = "Reset HUD"
            at(sr.scaledWidth_double / 2.0 - 60.0 / 2.0, sr.scaledHeight_double - 40.0)
            size(60.0, 20.0)
            onClick { modMessage("Resetting elements") }
        }

        hudElements.forEach { it.updateSize() }
    }

    /**
     * Draw a previes of all hud elements, regardless of whether they are visible.
     */
    override fun draw() {
        if (this.draggingElement == null) {
            resetButton.draw(mx, my)
        } else {
            snapLines.forEach { (pos, isVertical) ->
                val colour = if (pos.equalsOneOf(sr.scaledWidth_double / 2.0, sr.scaledHeight_double / 2.0)) Color.RED else Color.YELLOW
                if (isVertical) {
                    renderRect(pos, 0.0, 0.5, sr.scaledHeight_double, colour)
                } else {
                    renderRect(0.0, pos, sr.scaledWidth_double, 0.5, colour)
                }
            }
        }

        for (element in hudElements) {
            element.renderPreview(element == draggingElement)
        }

        mouseDrag(mx, my)
    }

    override fun onScroll(amount: Int) {
        hudElements.reversed().forEach {
            if (it.isHovered()) it.scroll(amount)
        }
    }

    override fun onMouseClick(mouseButton: Int) {
        when (mouseButton) {
            0 -> {
                this.draggingElement = hudElements.reversed().firstOrNull { it.toggled && it.isHovered() }

                if (this.draggingElement != null) {
                    this.startOffsetX = mx - this.draggingElement!!.x
                    this.startOffsetY = my - this.draggingElement!!.y
                } else if (this.resetButton.onMouseClick(mx, my, mouseButton)) {
                    this.hudElements.reversed().forEach {
                        if (it.toggled) it.resetElement()
                    }
                }
            }
            1 -> hudElements.reversed().firstOrNull { it.toggled && it.isHovered() }?.resetElement()
            2 -> hudElements.reversed().firstOrNull { it.toggled && it.isHovered() }?.parentModule?.openInGui()
        }
    }

    private fun mouseDrag(mouseX: Int, mouseY: Int) {
        draggingElement?.let { element ->
            snapLines.clear()

            var newX = mouseX - startOffsetX
            var newY = mouseY - startOffsetY

            if (!isShiftKeyDown()) {
                val centreX = sr.scaledWidth_double / 2.0
                val centreY = sr.scaledHeight_double / 2.0

                val bounds = element.getBounds(newX, newY)

                val snapPoints = mutableListOf(
                    SnapPoint(centreX, bounds.left, true),
                    SnapPoint(centreX, bounds.centreX, true),
                    SnapPoint(centreX, bounds.right, true),
                    SnapPoint(centreY, bounds.top, false),
                    SnapPoint(centreY, bounds.centreY, false),
                    SnapPoint(centreY, bounds.bottom, false)
                )

                hudElements.filter { it != element && it.toggled }.forEach { other ->
                    val otherBounds = other.getBounds(other.x, other.y)
                    snapPoints += getSnapPoints(bounds, otherBounds)
                }

                val (hOffset, lineX) = snapPoints.getClosestElement(true)
                val (vOffset, lineY) = snapPoints.getClosestElement(false)

                newX += hOffset
                newY += vOffset

                lineX?.let { snapLines.add(it to true) }
                lineY?.let { snapLines.add(it to false) }
            }

            element.x = newX.coerceIn(0.0, (sr.scaledWidth_double - element.width * element.scale).coerceAtLeast(0.0))

            element.y = newY.coerceIn(0.0, (sr.scaledHeight_double - element.height * element.scale).coerceAtLeast(0.0))
        }
    }

    private fun getSnapPoints(bounds: ElementBounds, otherBounds: ElementBounds): List<SnapPoint> {
        val snapPoints = mutableListOf<SnapPoint>()

        val horizontal = listOf(bounds.left, bounds.centreX, bounds.right)
        val otherHorizontal = listOf(otherBounds.left, otherBounds.centreX, otherBounds.right)

        for (other in otherHorizontal) {
            for (pos in horizontal) {
                snapPoints.add(SnapPoint(other, pos, true))
            }
        }

        val vertical = listOf(bounds.top, bounds.centreY, bounds.bottom)
        val otherVertical = listOf(otherBounds.top, otherBounds.centreY, otherBounds.bottom)

        for (other in otherVertical) {
            for (pos in vertical) {
                snapPoints.add(SnapPoint(other, pos, false))
            }
        }

        return snapPoints
    }

    private fun List<SnapPoint>.getClosestElement(vertical: Boolean): Pair<Double, Double?> {
        return this
            .filter { it.isVertical == vertical }
            .minByOrNull { abs(it.distance) }
            ?.takeIf { abs(it.distance) < 5.0 }
            ?.let { it.distance to it.targetPos }
            ?: (0.0 to null)
    }

    override fun onMouseRelease(state: Int) {
        draggingElement = null
    }

    override fun onGuiClosed() {
        moduleConfig.saveConfig()
    }
}

private data class SnapPoint(
    val targetPos: Double,
    val elementEdgePos: Double,
    val isVertical: Boolean
) {
    val distance: Double get() = targetPos - elementEdgePos
}