package catgirlroutes.module.impl.misc

import catgirlroutes.events.impl.ChatPacket
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.ChatUtils.modMessage
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object AutoKick: Module( // todo recode
    "Auto Kick",
    Category.MISC
){

    private val nameInput by StringSetting("Name", "catgirlsave")

    private val floorSelector by SelectorSetting("Floor", "F7", arrayListOf("F7", "M4", "M5", "M6", "M7"))

    private val pbDropdown by DropdownSetting("PBs")
    private val f7Pb by StringSetting("F7 PB", "5:00").withDependency(pbDropdown)
    private val m4Pb by StringSetting("M4 PB", "5:00").withDependency(pbDropdown)
    private val m5Pb by StringSetting("M5 PB", "5:00").withDependency(pbDropdown)
    private val m6Pb by StringSetting("M6 PB", "5:00").withDependency(pbDropdown)
    private val m7Pb by StringSetting("M7 PB", "5:00").withDependency(pbDropdown)

    enum class FloorEnums(
        val floor: String,
        val dungeon: String,
        val pb: () -> String
    ) {
        F7("floor_7", "floors", {f7Pb}),
        M4("floor_4", "master_mode_floors", { m4Pb }),
        M5("floor_5", "master_mode_floors", { m5Pb }),
        M6("floor_6", "master_mode_floors", { m6Pb }),
        M7("floor_7", "master_mode_floors", { m7Pb })
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onKeyBind() {
        GlobalScope.launch(Dispatchers.IO) {
            val secrets = getPB(nameInput)
            modMessage(secrets)
            modMessage(FloorEnums.valueOf(floorSelector.selected).pb())
            modMessage(millisToMmss(FloorEnums.valueOf(floorSelector.selected).pb().toLong()))
        }
    }

    //

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onChat(event: ChatPacket) {
        val match = Regex("^Party Finder > (.*) joined the dungeon group! \\(.* Level .*\\)\$").find(event.message) ?: return
        GlobalScope.launch(Dispatchers.IO) {
            val pb = getPB(nameInput) ?: return@launch
            val reqPb = mmssToMillis(FloorEnums.valueOf(floorSelector.selected).pb()) ?: return@launch
            if (pb >= reqPb) {
                ChatUtils.commandAny("/party chat Autokick: Kicked $match [PB] ${millisToMmss(pb.toLong())} > ${millisToMmss(reqPb)}")
                delay(300.toLong())
                ChatUtils.commandAny("/party kick $match")
            } else modMessage("Autokick: $match meets criteria [PB] ${millisToMmss(pb.toLong())} < ${millisToMmss(reqPb)}")
        }
    }

    private fun mmssToMillis(mmss: String): Long? {
        val regex = Regex("""^(\d+):(\d+)$""")
        val matchResult = regex.matchEntire(mmss) ?: return mmss.toLong()
        val (minutes, seconds) = matchResult.destructured
        return ((minutes.toInt() * 60) + seconds.toInt()) * 1000L
    }

    fun millisToMmss(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }


    private suspend fun getPB(name: String): Int? {
        val response = fetch("https://api.icarusphantom.dev/v1/sbecommands/cata/$name/selected")
        return if (response == null) null else try {
            JsonParser().parse(response).asJsonObject
                .get("data").asJsonObject
                .get("dungeons").asJsonObject
                .get("catacombs").asJsonObject
                .get(FloorEnums.valueOf(floorSelector.selected).dungeon).asJsonObject
                .get(FloorEnums.valueOf(floorSelector.selected).floor).asJsonObject
                .get("fastest_s_plus").asJsonPrimitive
                .asInt
        } catch (e: Exception) {
            modMessage("Failed to get stats for $name")
            null
        }
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun fetch(uri: String): String? {
        HttpClients.createMinimal().use {
            try {
                val httpGet = HttpGet(uri)
                return EntityUtils.toString(it.execute(httpGet).entity)
            }catch (_: Exception) {
                return null
            }
        }
    }
}