package catgirlroutes.utils.autop3

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.impl.dungeons.AutoP3.legsOffset
import catgirlroutes.utils.PlayerUtils.posX
import catgirlroutes.utils.PlayerUtils.posY
import catgirlroutes.utils.PlayerUtils.posZ
import catgirlroutes.utils.autop3.actions.RingAction
import catgirlroutes.utils.autop3.arguments.RingArgument
import kotlinx.coroutines.delay
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3

class Ring(
    val action: RingAction,
    var position: Vec3,
    var yaw: Float,
    var pitch: Float,
    val arguments: List<RingArgument>? = null,
    var length: Float = 1.0f,
    var width: Float = 1.0f,
    var height: Float = 1.0f,
    var delay: Int? = null,
) {
    fun boundingBox(): AxisAlignedBB {
        val halfWidth = width / 2.0
        val halfLength = length / 2.0

        val minX = position.xCoord - halfWidth
        val maxX = position.xCoord + halfWidth

        val minY = position.yCoord
        val maxY = position.yCoord + height

        val minZ = position.zCoord - halfLength
        val maxZ = position.zCoord + halfLength

        return AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).expand(0.0, -0.000001, 0.0)
    }

    fun inside(): Boolean {
        val feet = AxisAlignedBB(posX - 0.2, posY, posZ - 0.2, posX + 0.3, posY + 0.00001 + legsOffset, posZ)
        val ringBB: AxisAlignedBB = boundingBox()
        val a = Vec3(posX, posY, posZ)
        val b = Vec3(mc.thePlayer.prevPosX, mc.thePlayer.prevPosY, mc.thePlayer.prevPosZ)

        val intercept: MovingObjectPosition? = ringBB.calculateIntercept(a, b)
        val intersects = ringBB.intersectsWith(feet)

        return intercept != null || intersects
    }

    fun checkArgs(): Boolean {
        return this.arguments?.all { it.check(this) } ?: true
    }

    suspend fun execute() {
        delay?.let { delay(it.toLong()) }
        action.execute(this)
    }
}