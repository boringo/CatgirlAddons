package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.customtriggers.TypeName
import catgirlroutes.utils.distanceToPlayer

@TypeName("PositionCondition")
class PositionCondition(
    val x: Double,
    val y: Double,
    val z: Double,
    val radius: Double
) : TriggerCondition() { // TODO x2 y2 z2
    override fun check(): Boolean {

        if (mc.thePlayer != null) {
            val dist = distanceToPlayer(x, y, z)
            return dist <= radius * radius
        }

        return false
    }
}
