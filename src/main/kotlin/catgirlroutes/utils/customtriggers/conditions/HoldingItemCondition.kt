package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.utils.PlayerUtils.heldItem
import catgirlroutes.utils.PlayerUtils.isHolding
import catgirlroutes.utils.customtriggers.TypeName
import catgirlroutes.utils.noControlCodes

@TypeName("ItemCondition")
class HoldingItemCondition(
    val itemName: String? = null,
    val skyblockId: String? = null
) : TriggerCondition() {
    override fun check(): Boolean {
        return itemName?.let { heldItem?.displayName.noControlCodes.contains(it) } ?: false || isHolding(skyblockId)
    }
}