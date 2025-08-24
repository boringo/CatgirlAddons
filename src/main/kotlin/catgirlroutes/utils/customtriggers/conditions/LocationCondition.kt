package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.utils.Island
import catgirlroutes.utils.LocationManager
import catgirlroutes.utils.customtriggers.TypeName
import catgirlroutes.utils.dungeon.DungeonUtils

@TypeName("LocationCondition")
class LocationCondition(
    val location: Island,
    val floorNumber: Int? = null,
    val inBoss: Boolean? = null
) : TriggerCondition() {
    override fun check(): Boolean {
        if (LocationManager.currentArea != location) return false
        if (floorNumber != null && DungeonUtils.floorNumber != floorNumber) return false
        if (inBoss != null && DungeonUtils.inBoss != inBoss) return false
        return true
    }
}