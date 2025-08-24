package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Island
import catgirlroutes.utils.LocationManager.currentArea
import catgirlroutes.utils.configList
import catgirlroutes.utils.render.WorldRenderUtils.blockPosToAABB
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxAtBlock
import com.github.stivais.commodore.Commodore
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER
) {
    val waypointColorInner = ColorSetting("Inner Color", Color.PINK)
    val waypointColorOuter = ColorSetting("Outer Color", Color.PINK)

    init {
        this.addSettings(waypointColorInner, waypointColorOuter)
    }

    override fun onKeyBind() {
        waypoints.add(Waypoint(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, currentArea))
    }

    val waypoints by configList<Waypoint>("waypoints.json")

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        waypoints.forEach { p ->
            if (p.area != currentArea) return@forEach
            drawBoxAtBlock(BlockPos(p.x, p.y, p.z), color = if (intersects().contains(BlockPos(p.x, p.y, p.z))) Color(255, 255, 255, waypointColorInner.value.alpha) else waypointColorInner.value, filled = true)
            drawBoxAtBlock(BlockPos(p.x, p.y, p.z), color = if (intersects().contains(BlockPos(p.x, p.y, p.z))) Color(255, 255, 255, waypointColorOuter.value.alpha) else waypointColorOuter.value)
        }
    }

    @SubscribeEvent
    fun onMouse(event: MouseEvent) {
        if (!event.buttonstate) return
        if (event.button != 0) return

        waypoints.removeAll{ p ->
            intersects().contains(BlockPos(p.x, p.y, p.z))
        }
    }

    private fun intersects() : List<BlockPos> {
        var list = ArrayList<BlockPos>()
        waypoints.forEach {p ->
            val startVec = mc.thePlayer.getPositionEyes(1.0f)
            val box = blockPosToAABB(BlockPos(p.x, p.y, p.z), 1, 1, 1)
            val ray = mc.thePlayer.rayTrace(10.0, 1f).hitVec
            if (box.calculateIntercept(startVec, ray) != null) {
                list.add(BlockPos(p.x, p.y, p.z))
            }
        }
        return list
    }

    val waypointCommands = Commodore("wp") {
        literal("add").runs {
            x: Double, y: Double, z: Double ->
            val area = currentArea
            waypoints.add(Waypoint(x, y, z, area))
            modMessage("Added waypoint at x: ${x}, y: ${y},z: ${z} in the ${area}!")
            waypoints.save()
        }
        literal("undo").runs {
            waypoints.removeLast()
            waypoints.save()
        }
        literal("clear").runs {
            waypoints.clear()
            waypoints.save()
        }
        literal("remove").runs {
            val toRemove = intersects()
            waypoints.removeAll { p ->
                toRemove.contains(BlockPos(p.x, p.y, p.z))
            }
            waypoints.save()
        }
        literal("load").runs { waypoints.load() }
    }

    data class Waypoint (val x: Double, val y: Double, val z: Double, val area: Island)
}