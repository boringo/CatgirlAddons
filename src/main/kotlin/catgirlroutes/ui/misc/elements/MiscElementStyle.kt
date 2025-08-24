package catgirlroutes.ui.misc.elements

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.Alignment
import catgirlroutes.ui.clickgui.util.VAlignment
import catgirlroutes.utils.render.Radii
import java.awt.Color

data class MiscElementStyle(
    var value: String = "",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 80.0,
    var height: Double = 20.0,
    private var _radii: Radii = Radii(3.0),
    var thickness: Double = 1.0,
    var textColour: Color = Color(ColorUtil.textcolor),
    var textShadow: Boolean = false,
    var colour: Color = ColorUtil.bgColor.darker(),
    var hoverColour: Color = colour,
    var outlineColour: Color = ColorUtil.outlineColor,
    var outlineHoverColour: Color = ColorUtil.clickGUIColor,
    var alignment: Alignment = Alignment.CENTRE,
    var vAlignment: VAlignment = VAlignment.CENTRE,
    var textPadding: Double = 5.0,
    var vTextPadding: Double = 0.0
) {
    fun apply(block: MiscElementStyle.() -> Unit): MiscElementStyle {
        this.block()
        return this
    }

    var radius: Double
        get() = _radii.topLeft
        set(value) {
            _radii = Radii(value)
        }

    var radii: Radii
        get() = _radii
        set(value) {
            _radii = value
        }
}