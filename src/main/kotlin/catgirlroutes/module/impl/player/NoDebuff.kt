package catgirlroutes.module.impl.player

import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting

object NoDebuff : Module( // todo add more stuff
    "No Debuff",
    Category.PLAYER
) {

    /**
     * @see [MixinEntityPlayerSP]
     */
    val noPush by BooleanSetting("No push", false, "Prevents you from getting pushed out of blocks")
}