package catgirlroutes.ui.misc.customtriggers

import java.awt.Color


// TODO MOVE
data class Theme(
    val name: String,
    val background: Color,
    val panel: Color,
    val hoverPanel: Color,
    val card: Color,
    val hoverCard: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val accent: Color,
    val accentHover: Color,
    val focus: Color,
    val disabled: Color,
    val danger: Color
)


val DarkTheme = Theme(
    name = "Dark",
    background = Color(46, 46, 46, 255),
    panel = Color(58, 58, 58, 255),
    hoverPanel = Color(66, 66, 66, 255),
    card = Color(68, 68, 68, 255),
    hoverCard = Color(78, 78, 78, 255),
    textPrimary = Color(255, 255, 255, 255),
    textSecondary = Color(187, 187, 187, 255),
    border = Color(80, 80, 80, 255),
    accent = Color(107, 203, 119, 255),
    accentHover = Color(144, 224, 149, 255),
    focus = Color(170, 170, 170, 255),
    disabled = Color(102, 102, 102, 255),
    danger = Color(231, 111, 81, 255)
)

val LightTheme = Theme(
    name = "Light",
    background = Color(245, 245, 245, 255),
    panel = Color(255, 255, 255, 255),
    hoverPanel = Color(240, 240, 240, 255),
    card = Color(240, 240, 240, 255),
    hoverCard = Color(230, 230, 230, 255),
    textPrimary = Color(0, 0, 0, 255),
    textSecondary = Color(68, 68, 68, 255),
    border = Color(204, 204, 204, 255),
    accent = Color(107, 203, 119, 255),
    accentHover = Color(144, 224, 149, 255),
    focus = Color(136, 136, 136, 255),
    disabled = Color(170, 170, 170, 255),
    danger = Color(231, 111, 81, 255)
)

