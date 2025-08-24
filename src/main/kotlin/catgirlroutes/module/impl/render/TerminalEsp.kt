package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.utils.noControlCodes
import catgirlroutes.utils.dungeon.DungeonUtils
import catgirlroutes.utils.dungeon.M7Phases
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object TerminalEsp : Module (
    "Terminal ESP",
    Category.RENDER,
    "Shows undone terminals."
){
    private val color by ColorSetting("Terminal ESP color", Color(0,0,255), collapsible = false, description = "Color for the Terminal ESP")

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        mc.theWorld.loadedEntityList
            .filterIsInstance<EntityArmorStand>()
            .filter { DungeonUtils.termInactiveTitles.contains(it.name.noControlCodes) }
            .forEach {
                drawBoxByEntity(it, color, it.width.toDouble(), it.height.toDouble(), 0f)
            }
    }
}