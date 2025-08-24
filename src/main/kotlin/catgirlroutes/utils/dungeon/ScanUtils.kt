package catgirlroutes.utils.dungeon

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.utils.Island
import catgirlroutes.utils.LocationManager
import catgirlroutes.utils.dungeon.DungeonUtils.inBoss
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import catgirlroutes.utils.*
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.dungeon.tiles.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.io.FileNotFoundException

object ScanUtils {
    private const val ROOM_SIZE_SHIFT = 5  // Since ROOM_SIZE = 32 (2^5)
    private const val START = -185

    private var lastRoomPos: Vec2 = Vec2(0, 0)
    private val roomList: Set<RoomData> = loadRoomData()
    var currentRoom: Room? = null
        private set
    var passedRooms: MutableSet<Room> = mutableSetOf()
        private set

    private fun loadRoomData(): Set<RoomData> {
        return try {
            GsonBuilder()
                .registerTypeAdapter(RoomData::class.java, RoomDataDeserializer())
                .create().fromJson(
                    (ScanUtils::class.java.getResourceAsStream("/rooms.json") ?: throw FileNotFoundException()).bufferedReader(),
                    object : TypeToken<Set<RoomData>>() {}.type
                )
        } catch (e: Exception) {
            handleRoomDataError(e)
            setOf()
        }
    }

    private fun handleRoomDataError(e: Exception) {
        when (e) {
            is JsonSyntaxException -> println("Error parsing room data.")
            is JsonIOException -> println("Error reading room data.")
            is FileNotFoundException -> println("Room data not found, something went wrong! Please report this!")
            else -> {
                println("Unknown error while reading room data.")
//                logger.error("Error reading room data", e)
                println(e.message)
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return

        if ((!inDungeons && !LocationManager.currentArea.isArea(Island.SinglePlayer)) || inBoss) {
            currentRoom?.let { RoomEnterEvent(null).postAndCatch() }
            return
        } // If not in dungeon or in boss room, return and register current room as null

        val roomCenter = getRoomCenter(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
        if (roomCenter == lastRoomPos && LocationManager.currentArea.isArea(Island.SinglePlayer)) return // extra SinglePlayer caching for invalid placed rooms
        lastRoomPos = roomCenter

        passedRooms.find { previousRoom -> previousRoom.roomComponents.any { it.vec2 == roomCenter } }?.let { room ->
            if (currentRoom?.roomComponents?.none { it.vec2 == roomCenter } == true) RoomEnterEvent(room).postAndCatch()
            return
        } // If room is in passedRooms, post RoomEnterEvent and return only posts Event if room is not in currentFullRoom

        scanRoom(roomCenter)?.let { room -> if (room.rotation != Rotations.NONE) RoomEnterEvent(room).postAndCatch() }
    }

    private fun updateRotation(room: Room) {
        val roomHeight = getTopLayerOfRoom(room.roomComponents.first().vec2)
        if (room.data.name == "Fairy") {
            room.clayPos = room.roomComponents.firstOrNull()?.let { BlockPos(it.x - 15, roomHeight, it.z - 15) } ?: return
            room.rotation = Rotations.SOUTH
            return
        }
        room.rotation = Rotations.entries.dropLast(1).find { rotation ->
            room.roomComponents.any { component ->
                BlockPos(component.x + rotation.x, roomHeight, component.z + rotation.z).let { blockPos ->
                    getBlockIdAt(blockPos) == 159 && (room.roomComponents.size == 1 || EnumFacing.HORIZONTALS.all { facing ->
                        getBlockIdAt(blockPos.add(facing.frontOffsetX, 0, facing.frontOffsetZ)).equalsOneOf(159, 0)
                    }).also { isCorrectClay -> if (isCorrectClay) room.clayPos = blockPos }
                }
            }
        } ?: Rotations.NONE
    }

    private fun scanRoom(vec2: Vec2): Room? =
        getCore(vec2).let { core -> getRoomData(core)?.let { Room(data = it, roomComponents = findRoomComponentsRecursively(vec2, it.cores)) }?.apply { updateRotation(this) } }

    private fun findRoomComponentsRecursively(vec2: Vec2, cores: List<Int>, visited: MutableSet<Vec2> = mutableSetOf(), tiles: MutableSet<RoomComponent> = mutableSetOf()): MutableSet<RoomComponent> {
        if (vec2 in visited) return tiles else visited.add(vec2)
        tiles.add(RoomComponent(vec2.x, vec2.z, getCore(vec2).takeIf { it in cores } ?: return tiles))
        EnumFacing.HORIZONTALS.forEach { facing ->
            findRoomComponentsRecursively(Vec2(vec2.x + (facing.frontOffsetX shl ROOM_SIZE_SHIFT), vec2.z + (facing.frontOffsetZ shl ROOM_SIZE_SHIFT)), cores, visited, tiles)
        }
        return tiles
    }

    private fun getRoomData(hash: Int): RoomData? =
        roomList.find { hash in it.cores }

    fun getRoomCenter(posX: Int, posZ: Int): Vec2 {
        val roomX = (posX - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        val roomZ = (posZ - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        return Vec2((roomX shl ROOM_SIZE_SHIFT) + START, (roomZ shl ROOM_SIZE_SHIFT) + START)
    }

    fun getCore(vec2: Vec2): Int {
        val sb = StringBuilder(150)
        val chunk = mc.theWorld?.getChunkFromChunkCoords(vec2.x shr 4, vec2.z shr 4) ?: return 0
        val height = chunk.getHeightValue(vec2.x and 15, vec2.z and 15).coerceIn(11..140)
        sb.append(CharArray(140 - height) { '0' })
        var bedrock = 0
        for (y in height downTo 12) {
            val id = Block.getIdFromBlock(chunk.getBlock(BlockPos(vec2.x, y, vec2.z)))
            if (id == 0 && bedrock >= 2 && y < 69) {
                sb.append(CharArray(y - 11) { '0' })
                break
            }

            if (id == 7) bedrock++
            else {
                bedrock = 0
                if (id.equalsOneOf(5, 54, 146)) continue
            }
            sb.append(id)
        }
        return sb.toString().hashCode()
    }

    private fun getTopLayerOfRoom(vec2: Vec2): Int {
        val chunk = mc.theWorld?.getChunkFromChunkCoords(vec2.x shr 4, vec2.z shr 4) ?: return 0
        val height = chunk.getHeightValue(vec2.x and 15, vec2.z and 15) - 1
        return if (chunk.getBlock(vec2.x, height, vec2.z) == Blocks.gold_block) height - 1 else height
    }

    @SubscribeEvent
    fun enterDungeonRoom(event: RoomEnterEvent) {
        currentRoom = event.room
        if (passedRooms.none { it.data.name == currentRoom?.data?.name }) passedRooms.add(currentRoom ?: return)
        devMessage("${event.room?.data?.name} - ${event.room?.rotation} || clay: ${event.room?.clayPos}")
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        passedRooms.clear()
        currentRoom = null
        lastRoomPos = Vec2(0, 0)
    }
}