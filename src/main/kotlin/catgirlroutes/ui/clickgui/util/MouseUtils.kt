package catgirlroutes.ui.clickgui.util

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.render.HUDRenderUtils.scale
import catgirlroutes.utils.render.HUDRenderUtils.sr
import org.lwjgl.input.Mouse

object MouseUtils {
    val mouseButton get() = Mouse.getEventButton()
    val mx get() = Mouse.getX() / sr.scaleFactor
    val my get() = (mc.displayHeight - Mouse.getY()) / sr.scaleFactor

    val mouseX get() = (Mouse.getX() / (scale * sr.scaleFactor)).toInt()
    val mouseY get() = (((mc.displayHeight - Mouse.getY()) / sr.scaleFactor) / scale).toInt()
}