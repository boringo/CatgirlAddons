package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.MathHelper.*
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

object Animations: Module(
   "Animations",
    Category.PLAYER
){

    private val customSize by NumberSetting("Size", 0.0, -1.5, 1.5, 0.05)
    private val scaleSwing by BooleanSetting("Scale swing")

    private val customX by NumberSetting("X", 0.0, -1.5, 1.5, 0.05)
    private val customY by NumberSetting("Y", 0.0, -1.5, 1.5, 0.05)
    private val customZ by NumberSetting("Z", 0.0, -1.5, 1.5, 0.05)

    private val customYaw by NumberSetting("Yaw", 0.0, -180.0, 180.0, 1.0)
    private val customPitch by NumberSetting("Pitch", 0.0, -180.0, 180.0, 1.0)
    private val customRoll by NumberSetting("Roll", 0.0, -180.0, 180.0, 1.0)

    val customSpeed by NumberSetting("Speed", 0.0, -2.0, 1.0, 0.05)

    val ignoreHaste by BooleanSetting("Ignore haste")

    private val drinkingMode by SelectorSetting("Drinking mode", "None", arrayListOf("None", "Rotationless", "Fixed"))

    fun itemTransforHook(equipProgress: Float, swingProgress: Float): Boolean {
        if (!this.enabled) return false
        val newSize = (0.4f * exp(customSize))
        val newX = (0.56f * (1 + customX))
        val newY = (-0.52f * (1 - customY))
        val newZ = (-0.71999997f * (1 + customZ))
        GlStateManager.translate(newX, newY, newZ)
        GlStateManager.translate(0.0f, equipProgress * -0.6f, 0.0f)

        //Rotation
        GlStateManager.rotate(customPitch.toFloat(), 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(customYaw.toFloat(), 0.0f, 1f, 0f)
        GlStateManager.rotate(customRoll.toFloat(), 0f, 0f, 1f)

        GlStateManager.rotate(45f, 0.0f, 1f, 0f)

        val f = sin(swingProgress * swingProgress * Math.PI.toFloat())
        val f1 = sin(sqrt_float(swingProgress) * Math.PI.toFloat())
        GlStateManager.rotate(f * -20.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(f1 * -20.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.rotate(f1 * -80.0f, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(newSize, newSize, newSize)
        return true
    }

    fun scaledSwing(swingProgress: Float): Boolean {
        if (!scaleSwing || !this.enabled) return false
        val scale = exp(customSize)
        val f = -0.4f * sin(sqrt_float(swingProgress) * Math.PI.toFloat()) * scale
        val f1 = 0.2f * sin(sqrt_float(swingProgress) * Math.PI.toFloat() * 2.0f) * scale
        val f2 = -0.2f * sin(swingProgress * Math.PI.toFloat()) * scale
        GlStateManager.translate(f, f1, f2)
        return true
    }

    fun rotationlessDrink(clientPlayer: AbstractClientPlayer, partialTicks: Float): Boolean {
        if (drinkingMode.index != 1 || !this.enabled) return false
        val f: Float = clientPlayer.itemInUseCount.toFloat() - partialTicks + 1.0f
        val f1: Float = f / mc.thePlayer.heldItem.maxItemUseDuration.toFloat()
        var f2 = abs(cos(f / 4.0f * 3.1415927f) * 0.1f)
        if (f1 >= 0.8f) {
            f2 = 0.0f
        }
        GlStateManager.translate(0.0f, f2, 0.0f)
        return true
    }

    fun scaledDrinking(clientPlayer: AbstractClientPlayer, partialTicks: Float, itemToRender: ItemStack): Boolean {
        if (drinkingMode.index != 2 || !this.enabled) return false
        val f: Float = clientPlayer.itemInUseCount.toFloat() - partialTicks + 1.0f
        val f1: Float = f / itemToRender.maxItemUseDuration.toFloat()
        var f2 = abs(cos(f / 4.0f * Math.PI.toFloat()) * 0.1f)

        if (f1 >= 0.8f) {
            f2 = 0.0f
        }

        val newX = (0.56f * (1 + customX)).toFloat()
        val newY = (-0.52f * (1 - customY)).toFloat()
        val newZ = (-0.71999997f * (1 + customZ)).toFloat()
        GlStateManager.translate(-0.56f, 0.52f, 0.71999997f)
        GlStateManager.translate(newX, newY, newZ)

        GlStateManager.translate(0.0f, f2, 0.0f)
        val f3 = 1.0f - f1.toDouble().pow(27.0).toFloat()
        GlStateManager.translate(f3 * 0.6f, f3 * -0.5f, f3 * 0.0f)
        GlStateManager.rotate(f3 * 90.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(f3 * 10.0f, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(f3 * 30.0f, 0.0f, 0.0f, 1.0f)

        GlStateManager.translate(0.56f, -0.52f, -0.71999997f)
        GlStateManager.translate(-newX, -newY, -newZ)
        return true
    }
}