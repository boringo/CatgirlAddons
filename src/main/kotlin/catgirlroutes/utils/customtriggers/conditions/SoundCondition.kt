package catgirlroutes.utils.customtriggers.conditions

import catgirlroutes.utils.customtriggers.TypeName

@TypeName("SoundCondition")
class SoundCondition(
    val soundName: String,
    val volume: Double,
    val pitch: Double,
) : TriggerCondition() {
    override fun check(): Boolean { // handled in sound event
        return false
    }

    fun checkSound(name: String, volume: Float, pitch: Float): Boolean {
        return name == soundName && volume.toDouble() == this.volume && pitch.toDouble() == this.pitch
    }
}