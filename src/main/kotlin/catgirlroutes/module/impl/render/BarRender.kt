package catgirlroutes.module.impl.render

import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.HudSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.clock.Executor
import catgirlroutes.utils.render.HUDRenderUtils
import com.github.stivais.commodore.Commodore
import java.awt.Color
import kotlin.math.abs

object BarRender : Module(
    name = "Bar Render",
    category = Category.RENDER
) {

    init {
        change()
    }

    var percentage: Double = 50.0
    var targetProgress: Double = 50.0
    val hud by HudSetting{
        size(200.0 * percentage, 20.0)
        render { HUDRenderUtils.renderRect(x + 1.0, y + 1.0, 200.0 * percentage / 100.0, 20.0, Color.PINK)  }
    }

    private fun change() {
        Executor(1) {
            if (targetProgress != percentage) {
                if (abs(targetProgress - percentage) < 0.5) {
                    percentage = targetProgress
                    return@Executor
                }
                percentage += (targetProgress - percentage) * 0.01
            }
        }
    }

    var barSetter = Commodore("bar") { // todo move everything in one place, fuck you turtle
        literal("set").runs {
            num: Double ->
                targetProgress = num
            modMessage("Set Bar to: " + num)
        }
    }
}