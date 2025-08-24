package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.ElementDSL
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.ui.misc.elements.MiscElementStyle
import catgirlroutes.ui.misc.elements.util.calculateTextPosition
import catgirlroutes.utils.render.HUDRenderUtils
import java.awt.Color

class MiscElementSelector( // todo recode vertical selector
    style: MiscElementStyle = MiscElementStyle(),
    private var default: String,
    val options: List<String>,
    private val orientation: Orientation = Orientation.Vertical,
    val optionsPerRow: Int = 5,
    private val horizontalPadding: Int = 5,
) : MiscElement(style) {

    private var _selected = default
    private var _lastSelected = default
    private var extended = false
    private val isVertical get() = orientation == Orientation.Vertical

    var index: Int = options.indexOfFirst { it.equals(default, true) }.coerceAtLeast(0)
        private set(value) {
            field = value.coerceIn(0, options.lastIndex)
            _selected = options[field]
            _lastSelected = _selected
        }

    var selected: String
        get() = _selected
        set(value) {
            _lastSelected = value
            _selected = value
            options.indexOfFirst { it.equals(value, true) }.takeIf { it >= 0 }?.let {
                index = it
            }
        }

    val lastSelected: String get() = _lastSelected

    fun isSelected(option: String) = selected.equals(option, ignoreCase = true)

    override fun render(mouseX: Int, mouseY: Int) {
        val highlightColor = if (extended || isHovered(mouseX, mouseY)) outlineColour else outlineHoverColour

        if (isVertical) renderVertical(mouseX, mouseY, highlightColor)
        else renderHorizontal(mouseX, mouseY, highlightColor)
    }

    private fun renderVertical(mouseX: Int, mouseY: Int, highlightColor: Color) {

        val totalHeight = if (extended) height * (options.size + 1) else height
        HUDRenderUtils.drawRoundedBorderedRect(
            x, y, width, totalHeight, radii, thickness,
            colour, highlightColor
        )

        val (textX, textY_) = this.calculateTextPosition()

        FontUtil.drawAlignedString(
            selected,
            textX, textY_, style.alignment, style.vAlignment,
            style.textColour.rgb
        )

        if (extended) {
            options.forEachIndexed { i, option ->
                val optionY = y + height * (i + 1)
                val textY = textY_ + height * (i + 1)

                if (isSelected(option) || isHovered(mouseX, mouseY, yOff = optionY - y)) {
                    HUDRenderUtils.drawRoundedRect(
                        x, optionY, width, height, radii, highlightColor
                    )
                }

                FontUtil.drawAlignedString(
                    option.replaceFirstChar { it.titlecase() },
                    textX, textY, style.alignment, style.vAlignment,
                    style.textColour.rgb
                )
            }
        }
    }

    private fun renderHorizontal(mouseX: Int, mouseY: Int, highlightColor: Color) {
        val (textX_, textY_) = calculateTextPosition()
        options.forEachIndexed { i, option ->
            val column = i % optionsPerRow
            val row = i / optionsPerRow
            val optionX = x + column * (width + horizontalPadding)
            val optionY = y + row * (height + horizontalPadding)
            val textX = textX_ + column * (width + horizontalPadding)
            val textY = textY_ + row * (height + horizontalPadding)

            HUDRenderUtils.drawRoundedBorderedRect(
                optionX, optionY, width, height, radii, thickness,
                colour,
                if (isSelected(option)) highlightColor else ColorUtil.outlineColor
            )

            if (isSelected(option) || isHovered(mouseX, mouseY, optionX - x, optionY - y)) {
                HUDRenderUtils.drawRoundedOutline(
                    optionX, optionY, width, height, radii, thickness, highlightColor
                )
            }

            FontUtil.drawAlignedString(
                option.replaceFirstChar { it.titlecase() },
                textX, textY, style.alignment, style.vAlignment,
                style.textColour.rgb
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isVertical) {
                if (!extended && isHovered(mouseX, mouseY)) {
                    index++
                    return true
                }
                if (extended) {
                    options.forEachIndexed { i, _ ->
                        val optionY = y + height * (i + 1)
                        if (isHovered(mouseX, mouseY, yOff = optionY - y)) {
                            index = i
                            extended = false
                            return true
                        }
                    }
                }
            } else {
                options.forEachIndexed { i, option ->
                    val column = i % optionsPerRow
                    val row = i / optionsPerRow
                    val optionX = x + column * (width + horizontalPadding)
                    val optionY = y + row * (height + horizontalPadding)

                    if (isHovered(mouseX, mouseY, optionX - x, optionY - y)) {
                        index = i
                        return true
                    }
                }
            }
        } else if (mouseButton == 1 && isVertical && isHovered(mouseX, mouseY)) {
            extended = !extended
            return true
        }
        return false
    }
}

class SelectorBuilder : ElementDSL<MiscElementSelector>() {
    var text by _style::value
    private var defaultSelected: String = ""
    private val options = mutableListOf<String>()
    private var orientation: Orientation = Orientation.Vertical
    private var optionsPerRow: Int = 5
    private var horizontalPadding: Int = 5

    fun default(option: String) {
        defaultSelected = option
    }

    fun options(vararg items: String) {
        options.addAll(items)
    }

    fun horizontal(perRow: Int = 5, padding: Int = 5) {
        orientation = Orientation.Horizontal
        optionsPerRow = perRow
        horizontalPadding = padding
    }

    override fun buildElement(): MiscElementSelector {
        return MiscElementSelector(
            createStyle(),
            defaultSelected,
            options,
            orientation,
            optionsPerRow,
            if (orientation == Orientation.Horizontal) horizontalPadding else 0,
        )
    }
}

fun selector(block: SelectorBuilder.() -> Unit): MiscElementSelector {
    return SelectorBuilder().apply(block).build()
}

enum class Orientation {
    Vertical, Horizontal
}