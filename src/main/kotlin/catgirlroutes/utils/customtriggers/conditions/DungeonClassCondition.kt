package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.utils.customtriggers.TypeName
import catgirlroutes.utils.dungeon.DungeonUtils

@TypeName("DungeonsClazzCondition")
class DungeonClassCondition(val name: String) : TriggerCondition() {
    override fun check(): Boolean {
        return DungeonUtils.currentDungeonPlayer.clazz.name == name
    }
}