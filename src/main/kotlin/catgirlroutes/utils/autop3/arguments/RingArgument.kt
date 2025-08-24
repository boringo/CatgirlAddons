package catgirlroutes.utils.autop3.arguments

import catgirlroutes.utils.autop3.IStupid
import catgirlroutes.utils.autop3.Ring

sealed class RingArgument : IStupid {
    abstract fun check(ring: Ring): Boolean
}