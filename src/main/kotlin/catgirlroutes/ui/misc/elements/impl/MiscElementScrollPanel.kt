package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.animations.impl.LinearAnimation
import catgirlroutes.ui.misc.elements.ElementDSL
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.ui.misc.elements.MiscElementStyle
import catgirlroutes.utils.render.HUDRenderUtils.resetScissor
import catgirlroutes.utils.render.HUDRenderUtils.scissor
import kotlin.math.abs

class MiscElementScrollPanel(
    style: MiscElementStyle,
    private val children: List<MiscElement>,
    private val scrollDistance: Int
) : MiscElement(style) {

    private var scrollTarget = 0.0
    private var scrollOffset = 0.0
    private val scrollAnimation = LinearAnimation<Double>(200)

    private val visibleRange get() = y..(y + height)
    val totalHeight: Double
        get() {
            if (children.isEmpty()) return 0.0
            val sortedChildren = children.sortedBy { it.y }

            val firstElement = sortedChildren.first()
            val lastElement = sortedChildren.last()

            return (lastElement.y + lastElement.height) - firstElement.y + 50.0
        }


    override fun render(mouseX: Int, mouseY: Int) {
        scissor(x, y, width, height)

        scrollOffset = scrollAnimation.get(scrollOffset, scrollTarget)

        children.forEachIndexed { i, child ->
            child.y += scrollOffset
            child.draw(mouseX, mouseY)
            child.y -= scrollOffset
        }

        resetScissor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!isHovered(mouseX, mouseY) || mouseY.toDouble() !in visibleRange) return false

        val adjustedMouseY = mouseY + abs(scrollOffset.toInt())
        return children.any { it.onMouseClick(mouseX, adjustedMouseY, mouseButton) }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return children.any { it.onKey(typedChar, keyCode) }
    }

    override fun onScroll(mouseX: Int, mouseY: Int, amount: Int): Boolean {
        if (!isHovered(mouseX, mouseY)) return false
        if (totalHeight < height) return false
        scrollTarget = (scrollTarget + amount * scrollDistance).coerceIn(-totalHeight + height, 0.0)
        scrollAnimation.start(true)
        return true
    }
}

class ScrollPanelBuilder : ElementDSL<MiscElementScrollPanel>() {
    private val children = mutableListOf<MiscElement>()

    var scrollDistance: Int = 25

    fun element(element: MiscElement) {
        children.add(element)
    }

    fun elements(elements: List<MiscElement>) {
        children.addAll(elements)
    }

    fun button(block: ButtonBuilder.() -> Unit) {
        children.add(ButtonBuilder().apply(block).build())
    }

    fun boolean(block: BooleanBuilder.() -> Unit) {
        children.add(BooleanBuilder().apply(block).build())
    }

    fun textField(block: TextFieldBuilder.() -> Unit) {
        children.add(TextFieldBuilder().apply(block).build())
    }

    fun keyBind(block: KeyBindBuilder.() -> Unit) {
        children.add(KeyBindBuilder().apply(block).build())
    }

    fun selector(block: SelectorBuilder.() -> Unit) {
        children.add(SelectorBuilder().apply(block).build())
    }

    override fun buildElement() = MiscElementScrollPanel(createStyle(), children, scrollDistance)
}

fun scrollPanel(block: ScrollPanelBuilder.() -> Unit): MiscElementScrollPanel {
    return ScrollPanelBuilder().apply(block).build()
}