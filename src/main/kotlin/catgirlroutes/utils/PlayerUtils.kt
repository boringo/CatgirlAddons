package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import net.minecraft.block.BlockAir
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.lang.reflect.Method

object PlayerUtils {

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ

    private var shouldBypassVolume: Boolean = false
    fun playLoudSound(sound: String?, volume: Float, pitch: Float) {
        shouldBypassVolume = true
        mc.thePlayer?.playSound(sound, volume, pitch)
        shouldBypassVolume = false
    }

    fun getItemSlot(item: String, ignoreCase: Boolean = true): Int? =
        mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it?.unformattedName?.contains(item, ignoreCase) == true }.takeIf { it != -1 }


    var recentlySwapped = false
    fun swapFromName(name: String): SwapState {
        for (i in 0..8) {
            val stack: ItemStack? = mc.thePlayer.inventory.getStackInSlot(i)
            val itemName = stack?.displayName
            if (itemName != null) {
                if (itemName.contains(name, ignoreCase = true)) {
                    if (mc.thePlayer.inventory.currentItem != i) {
                        if (recentlySwapped) {
                            modMessage("yo somethings wrong $itemName")
                            return SwapState.TOO_FAST
                        }
                        recentlySwapped = true
                        mc.thePlayer.inventory.currentItem = i
                        return SwapState.SWAPPED
                    } else {
                        return SwapState.ALREADY_HELD
                    }
                }
            }
        }
        modMessage("$name §cnot found.")
        return SwapState.UNKNOWN
    }

    fun swapFromName(name: String, action: () -> Unit) {
        val state = swapFromName(name)
        when(state) {
            SwapState.SWAPPED -> scheduleTask(1) { action() }
            SwapState.ALREADY_HELD -> action()
            else -> return
        }
    }

    fun swapToSlot(slot: Int): SwapState {
        if (mc.thePlayer.inventory.currentItem != slot) {
            if (recentlySwapped) {
                modMessage("u swapping too faaaast")
                return SwapState.TOO_FAST
            }
            recentlySwapped = true
            mc.thePlayer.inventory.currentItem = slot
            return SwapState.SWAPPED
        } else return SwapState.ALREADY_HELD
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            recentlySwapped = false
        }
    }

    val ItemStack?.skyblockID: String
        get() = this?.extraAttributes?.getString("id") ?: ""

    fun isHolding(vararg id: String?): Boolean =
        heldItem?.skyblockID in id

    val heldItem get() = mc.thePlayer?.heldItem

    fun rightClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
    }

    fun rightClick2() {
        val clickMouse: Method = try {
            Minecraft::class.java.getDeclaredMethod("func_147121_ag")
        } catch (e: NoSuchMethodException) {
            Minecraft::class.java.getDeclaredMethod("rightClickMouse")
        }
        clickMouse.isAccessible = true
        clickMouse.invoke(mc)
    }

    fun leftClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)
    }

    fun leftClick2() {
        val clickMouse: Method = try {
            Minecraft::class.java.getDeclaredMethod("func_147116_af")
        } catch (e: NoSuchMethodException) {
            Minecraft::class.java.getDeclaredMethod("clickMouse")
        }
        clickMouse.isAccessible = true
        clickMouse.invoke(mc)
    }

    fun airClick() {
        PacketUtils.sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
    }

    fun relativeClip(x: Double, y: Double, z: Double, message: Boolean = true) {

        if (message) {
            val text = listOfNotNull(
                if (x != 0.0) "x: $x" else null,
                if (y != 0.0) "y: $y" else null,
                if (z != 0.0) "z: $z" else null
            ).joinToString(" ")

            if (text.isNotEmpty()) modMessage("Clipping $text")
        }

        mc.thePlayer.setPosition(posX + x, posY + y,posZ + z)
    }

    fun findDistanceToAirBlocks(): Double? {
        val world = mc.theWorld

        if (mc.thePlayer == null || world == null) return null

        val startPos = BlockPos(posX, posY, posZ)
        var airCount = 0
        var gapEnd: Int? = null

        for ((y) in (startPos.y downTo 0).withIndex()) {
            val pos = BlockPos(startPos.x, y, startPos.z)
            val block = world.getBlockState(pos).block
            if (block is BlockAir) {
                airCount++
                gapEnd = y
            } else {
                if (airCount >= 2) {
                    return (startPos.y - gapEnd!!).toDouble()
                }
                airCount = 0
            }
        }
        return if (airCount >= 2) (startPos.y - gapEnd!!).toDouble() else null
    }

    fun findDistanceToAirBlocksLegacy(): Double? {
        val world = mc.theWorld

        if (mc.thePlayer == null || world == null) return null

        val startPos = BlockPos(posX, posY, posZ)
        var airCount = 0

        for ((distance, y) in (startPos.y downTo 0).withIndex()) {
            val pos = BlockPos(startPos.x, y, startPos.z)
            val block = world.getBlockState(pos).block

            if (block is BlockAir) {
                airCount++
                if (airCount == 2) return (distance * -1).toDouble() - 1.0
            } else {
                airCount = 0
            }
        }
        return null
    }
}

enum class SwapState{
    SWAPPED, ALREADY_HELD, TOO_FAST, UNKNOWN
}