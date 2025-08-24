package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.rotation.RotationUtils.getYawAndPitch
import net.minecraft.block.Block
import net.minecraft.client.settings.GameSettings.isKeyDown
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object MovementUtils { // todo cleanup, add motion

    private val FORWARD = mc.gameSettings.keyBindForward
    private val BACK = mc.gameSettings.keyBindBack
    private val RIGHT = mc.gameSettings.keyBindRight
    private val LEFT = mc.gameSettings.keyBindLeft

    private val JUMP = mc.gameSettings.keyBindJump
    private val SNEAK = mc.gameSettings.keyBindSneak
    private val SPRINT = mc.gameSettings.keyBindSprint

    fun setKey(key: String, down: Boolean) {
        when(key) {
            "w" -> KeyBinding.setKeyBindState(FORWARD.keyCode, down)
            "a" -> KeyBinding.setKeyBindState(LEFT.keyCode, down)
            "s" -> KeyBinding.setKeyBindState(BACK.keyCode, down)
            "d" -> KeyBinding.setKeyBindState(RIGHT.keyCode, down)
            "jump" -> KeyBinding.setKeyBindState(JUMP.keyCode, down)
            "shift" -> KeyBinding.setKeyBindState(SNEAK.keyCode, down)
            "sprint" -> KeyBinding.setKeyBindState(SPRINT.keyCode, down)
        }
    }

    fun movementKeysDown(): Boolean {
        return FORWARD.isKeyDown || LEFT.isKeyDown || RIGHT.isKeyDown || BACK.isKeyDown || SNEAK.isKeyDown
    }

    fun stopVelo() {
        mc.thePlayer.setVelocity(0.0, mc.thePlayer.motionY, 0.0)
    }

    fun stopMovement() {
        dir = null
        jumping = false
        setKey("w", false)
        setKey("a", false)
        setKey("s", false)
        setKey("d", false)
    }

    fun restartMovement() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, isKeyDown(mc.gameSettings.keyBindForward))
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.keyCode, isKeyDown(mc.gameSettings.keyBindLeft))
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.keyCode, isKeyDown(mc.gameSettings.keyBindBack))
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.keyCode, isKeyDown(mc.gameSettings.keyBindRight))
    }

    fun jump() {
//        scheduleTask(0) {setKey("jump", true)} // why did watr think it was a good idea
//        scheduleTask(2) {setKey("jump", false)}
        if (mc.thePlayer.onGround) mc.thePlayer.jump()
    }

    fun pressKey(keyCode: Int, delay: Int = 1) {
        KeyBinding.setKeyBindState(keyCode, true)
        scheduleTask(delay) { KeyBinding.setKeyBindState(keyCode, false) }
    }

    private var shouldEdge = false

    fun edge() {
        shouldEdge = true
    }

    @SubscribeEvent
    fun checkForEdging(event: RenderWorldLastEvent) {
        if (!shouldEdge) return
        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)
        val blockID = Block.getIdFromBlock(mc.theWorld.getBlockState(playerPos).block)
        if (blockID == 0) {
            jump()
            shouldEdge = false
        }
    }

    @SubscribeEvent
    fun stupid(event: KeyInputEvent) {
        if (movementKeysDown()) {
            if (dir != null) stopMovement()
            if (targetBlocks.isNotEmpty()) {
                stopMovement()
                targetBlocks.removeFirst()
            }
        }
    }

    var targetBlocks = arrayListOf<Vec3>()

    fun clearBlocks() {
        targetBlocks.clear()
    }

    fun moveToBlock(x: Double, z: Double) {
        targetBlocks.add(0, Vec3(x, 0.0, z)) // y coord doesn't matter anyway
    }

    fun addBlock(x: Double, z: Double) {
        targetBlocks.add(Vec3(x, 0.0, z))
    }

    private var dir: Float? = null
    private var jumping = false
    private var airTicks = 0

    @SubscribeEvent
    fun onLivingJump(event: LivingEvent.LivingJumpEvent) {
        if (event.entity.worldObj.isRemote) {
            jumping = true
            debugMessage("jump living")
        }
    }

    /**
     * Player velocity calculations from sy?
     * @author sapv5678
     */
    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || dir == null) return
        if (mc.thePlayer.onGround) {
            airTicks = 0
        } else {
            ++airTicks
        }

        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava) return

        val sprintMultiplier = 1.3
        var speed = mc.thePlayer.aiMoveSpeed.toDouble()
        if (mc.thePlayer.isSprinting) {
            speed /= sprintMultiplier
        }

        if (airTicks < 1) {
            val rad = dir!! * Math.PI / 180
            var speedMultiplier = 2.806
            if (jumping) {
                debugMessage("we jumping")
                jumping = false
                speedMultiplier += 2
                speedMultiplier *= 1.25
            }
            mc.thePlayer.motionX = -sin(rad) * speed * speedMultiplier
            mc.thePlayer.motionZ = cos(rad) * speed * speedMultiplier
            return
        }

        val movementFactor = if (mc.thePlayer.onGround || (airTicks == 1 && mc.thePlayer.motionY < 0)) {
            speed * sprintMultiplier
        } else {
            0.02 * sprintMultiplier
        }

        val sinYaw = MathHelper.sin((dir!! * Math.PI / 180).toFloat())
        val cosYaw = MathHelper.cos((dir!! * Math.PI / 180).toFloat())
        mc.thePlayer.motionX -= movementFactor * sinYaw
        mc.thePlayer.motionZ += movementFactor * cosYaw
    }

    fun motion(y: Float) {
        dir = y
        airTicks = 0
    }

    private fun simulateMovementKeys(targetYaw: Float, playerYaw: Float) {

        val radTarget = Math.toRadians(targetYaw.toDouble())
        val xTarget = -sin(radTarget)
        val zTarget =  cos(radTarget)

        val radPlayerYaw = Math.toRadians(playerYaw.toDouble())
        val playerForwardX = -sin(radPlayerYaw)
        val playerForwardZ = cos(radPlayerYaw)
        val playerRightX = cos(radPlayerYaw)
        val playerRightZ = sin(radPlayerYaw)

        val forward = xTarget * playerForwardX + zTarget * playerForwardZ
        val strafe = xTarget * playerRightX + zTarget * playerRightZ

        val pressForward = forward > 0.2
        val pressBack = forward < -0.2
        val pressLeft = strafe > 0.2
        val pressRight = strafe < -0.2

        setKey("w", pressForward)
        setKey("s", pressBack)
        setKey("d", pressRight)
        setKey("a", pressLeft)
    }

    private var lastX = 0.0
    private var lastZ = 0.0

    @SubscribeEvent // todo replace with sapv's calc
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (targetBlocks.isEmpty()) return
        val targetBlock = targetBlocks[0]

        if (mc.thePlayer?.onGround == true) {
            airTicks = 0
        } else {
            airTicks += 1
        }

        val(yaw, pitch) = getYawAndPitch(targetBlock.xCoord, targetBlock.yCoord, targetBlock.zCoord)

        val speed = if(!mc.thePlayer.isSneaking) {
            mc.thePlayer.capabilities.walkSpeed
        } else {
            mc.thePlayer.capabilities.walkSpeed * 3 / 10 /// doesnt let me do 0.3???
        }
        val radians = yaw * Math.PI / 180 // todo: MathUtils?
        val x = -sin(radians) * speed * 2.806
        val z = cos(radians) * speed * 2.806

        if (airTicks < 2) {
            mc.thePlayer.motionX = x
            mc.thePlayer.motionZ = z
            lastX = x
            lastZ = z
        } else {
            //assume max acceleration
            mc.thePlayer.motionX = lastX * 0.91 + 0.0512 * speed * -sin(radians)
            mc.thePlayer.motionZ = lastZ * 0.91 + 0.0512 * speed * cos(radians)
            lastX = lastX * 0.91 + 0.2 * -sin(radians)
            lastZ = lastZ * 0.91 + 0.2 * cos(radians)
        }

        if (mc.thePlayer?.onGround == true) {
            if (
                abs(mc.thePlayer.posX - targetBlock.xCoord) <= 1.3 * mc.thePlayer.capabilities.walkSpeed &&
                abs(mc.thePlayer.posZ - targetBlock.zCoord) <= 1.3 * mc.thePlayer.capabilities.walkSpeed
            ) {
                mc.thePlayer.setVelocity(0.0, mc.thePlayer.motionY, 0.0)
                mc.thePlayer.setPosition(targetBlock.xCoord, mc.thePlayer.posY, targetBlock.zCoord)
                targetBlocks.removeFirst()
                return
            }
        }
    }
}

