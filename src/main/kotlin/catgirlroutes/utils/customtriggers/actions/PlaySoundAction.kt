package catgirlroutes.utils.customtriggers.actions

import catgirlroutes.utils.PlayerUtils
import catgirlroutes.utils.customtriggers.TypeName

@TypeName("SoundAction")
class PlaySoundAction(
    val sound: String,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f
) : TriggerAction() {
    override fun execute() {
        PlayerUtils.playLoudSound(sound, volume, pitch)
    }
}