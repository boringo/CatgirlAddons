package catgirlroutes.utils.autop3.actions

import catgirlroutes.utils.autop3.IStupid
import catgirlroutes.utils.autop3.Ring

sealed class RingAction : IStupid {
    abstract fun execute(ring: Ring)
    open fun onTick(ring: Ring) {  }
}