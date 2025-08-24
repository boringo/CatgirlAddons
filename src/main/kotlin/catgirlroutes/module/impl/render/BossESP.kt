package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.RenderEntityModelEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.utils.dungeon.DungeonUtils
import catgirlroutes.utils.dungeon.M7Phases
import catgirlroutes.utils.render.OutlineUtils
import catgirlroutes.utils.render.WorldRenderUtils.draw2DBoxByEntity
import catgirlroutes.utils.render.WorldRenderUtils.drawEntityBox
import net.minecraft.entity.boss.EntityWither
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object BossESP : Module( // todo add tracer, using packets for detection is prob better
    "Boss ESP",
    Category.RENDER,
    "ESP for the Withers."
) {
    private val style by SelectorSetting("Style","Box", arrayListOf("Box", "Outline", "2D"), "Esp render style to be used.")
    private val lineWidth by NumberSetting("Line width", 4.0, 1.0, 8.0, 1.0)
    private val boxOffset by NumberSetting("Box size offset", 0.0, -1.0, 1.0, 0.05, "Change box size offset.").withDependency { style.selected == "Box" }
    private val color by ColorSetting("Boss ESP color", Color(0,0,255), description = "Color for the Boss ESP")
    private val espFill by BooleanSetting("Esp fill").withDependency { style.selected == "Box"}
    private val fillColour by ColorSetting("Boss ESP fill colour", Color(0,0,255), description = "Fill colour for the Boss ESP").withDependency { espFill && style.selected == "Box" }

    private var wither: EntityWither? = null;

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() == M7Phases.Unknown) return
        mc.theWorld.loadedEntityList
            .filterIsInstance<EntityWither>()
            .filter { !it.isInvisible && it.renderSizeModifier == 1f && it.invulTime != 800 }
            .forEach {
                wither = it
                when (style.selected) {
                    "Box" -> drawEntityBox(it, color, fillColour, true, espFill, event.partialTicks, lineWidth.toFloat(), boxOffset.toFloat())
                    "2D" -> draw2DBoxByEntity(it, color, event.partialTicks, lineWidth.toFloat(), true)
                }
            }
    }

    @SubscribeEvent
    fun onRenderEntityModel (event: RenderEntityModelEvent) {
        if (style.selected == "Outline" && event.entity == wither) {
            OutlineUtils.outlineESP(event, lineWidth.toFloat(), color, true)
        }
    }
}