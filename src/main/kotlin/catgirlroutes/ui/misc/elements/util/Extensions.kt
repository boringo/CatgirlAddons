package catgirlroutes.ui.misc.elements.util

import catgirlroutes.ui.clickgui.util.Alignment
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.VAlignment
import catgirlroutes.ui.misc.elements.MiscElement

fun MiscElement.calculateTextPosition(
    hPadding: Double = this.style.textPadding,
    vPadding: Double = this.style.vTextPadding,
    scale: Double = 1.0,
    h: Double = this.height,
    w: Double = this.width
): Pair<Double, Double> {
    return Pair(
        when (this.style.alignment) {
            Alignment.LEFT -> x + hPadding
            Alignment.CENTRE -> x + w / 2
            Alignment.RIGHT -> x + w - hPadding
        },
        when (this.style.vAlignment) {
            VAlignment.TOP -> y + vPadding
            VAlignment.CENTRE -> y + h / 2
            VAlignment.BOTTOM -> y + h - vPadding - FontUtil.getFontHeight(scale)
        }
    )
}

inline fun <T : MiscElement> T.update(crossinline block: T.() -> Unit): T {
    this.updates.apply {
        this@update.block()
    }
    return this
}