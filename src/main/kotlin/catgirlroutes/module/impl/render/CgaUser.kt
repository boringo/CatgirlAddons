package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.CatgirlRoutes.Companion.scope
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.AlwaysActive
import catgirlroutes.module.settings.SettingsCategory
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.CgaUsers
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.sendDataToServer
import kotlinx.coroutines.launch
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@AlwaysActive
@SettingsCategory
object CgaUser : Module(
    "Cga User",
    Category.RENDER,
) {
    private var scaleX by NumberSetting("Scale X", 1.0, -3.0, 3.0, 0.1)
    private var scaleY by NumberSetting("Scale Y", 1.0, -3.0, 3.0, 0.1)
    private var scaleZ by NumberSetting("Scale Z", 1.0, -3.0, 3.0, 0.1)

    private var cape by StringSetting("Cape", "XkdcuPO")

    private val reset by ActionSetting("Reset") {
        modMessage("Resetting User settings")
        scaleX = 1.0; scaleY = 1.0; scaleZ = 1.0
        cape = "XkdcuPO"
    }

    val updateUser = ActionSetting("Update User Data") {
        scope.launch {
            var c = cape.takeIf { it.isNotEmpty() } ?: "XkdcuPO"
            // https://regex101.com/r/dZ971v/2
            c = Regex("(https://imgur\\.com/)?([a-zA-Z0-9]+)(?:\\.png)?").find(c)?.groups?.get(2)?.value ?: c // fuck regex it dosen't work
            val jsonString = """
                {
                    "name": "${mc.thePlayer.name}",
                    "uuid": "${mc.session.playerID}",
                    "dimensions": {
                        "x": ${scaleX},
                        "y": ${scaleY},
                        "z": ${scaleZ}
                    },
                    "cape": "$c"
                }
                """
            modMessage(sendDataToServer(jsonString))
            CgaUsers.updateUsers()
        }
    }

    override fun onEnable() {
        super.onEnable()
        toggle()
    }

    fun scaleHook(entityLivingBaseIn: AbstractClientPlayer) {
        if (!CgaUsers.users.containsKey(entityLivingBaseIn.name)) return

        val user = CgaUsers.users[entityLivingBaseIn.name] ?: return
        GlStateManager.scale(user.xScale, user.yScale, user.zScale)
        if (user.yScale < 0) GlStateManager.translate(0f, user.yScale * -2, 0f)
    }

    fun capeHook(cir: CallbackInfoReturnable<ResourceLocation>, playerInfo: NetworkPlayerInfo) {
        if (!CgaUsers.users.containsKey(playerInfo.gameProfile.name)) return

        val user = CgaUsers.users[playerInfo.gameProfile.name] ?: return
        cir.returnValue = user.cape
    }
}