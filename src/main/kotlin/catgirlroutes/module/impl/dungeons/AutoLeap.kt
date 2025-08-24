package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.ChatPacket
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.render.ClickGui
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.*
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.Party
import catgirlroutes.utils.dungeon.DungeonClass
import catgirlroutes.utils.dungeon.DungeonUtils.getP3Section
import catgirlroutes.utils.dungeon.DungeonUtils.inBoss
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import catgirlroutes.utils.dungeon.LeapUtils.leap
import catgirlroutes.utils.dungeon.P3Sections
import catgirlroutes.utils.getSafe
import catgirlroutes.utils.noControlCodes
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoLeap : Module(
    "Auto Leap",
    Category.DUNGEON,
    tag = TagType.WHIP
) {
    private val fastLeap by BooleanSetting("Fast leap", "Leaps to a set player on infinileap left click.")
    private val autoLeap by BooleanSetting("Auto leap", "Automatically leaps to a set player.")

    private var teamList by ListSetting("Teammates", mutableListOf("None"))
    private val clazzArray = arrayListOf("Mage", "Berserk", "Archer", "Tank", "Healer", "None")

    private val clazzOrder by OrderSetting("Class order", mapOf("S1" to "Archer", "S2" to "Healer", "S3" to "Mage", "S4" to "Mage", "Clear" to "None"), 2, clazzArray).withDependency { !isFlopper && leapMode.selected == "Class" }
    private val namesOrder by OrderSetting("Name order", mapOf("S1" to "None", "S2" to "None", "S3" to "None", "S4" to "None", "Clear" to "None"), 2, teamList) {
        updateTeammates()
        options = teamList
    }.withDependency { !isFlopper && leapMode.selected == "Name" }
    private val slotOrder by OrderSetting("Leap slot order", mapOf("S1" to "1", "S2" to "2", "S3" to "3", "S4" to "4", "Clear" to "None"), 2, listOf("1", "2", "3", "4", "None")).withDependency { leapMode.selected == "Leap slot" }

    private var leapMode by SelectorSetting("Leap mode", "Name", arrayListOf("Name", "Class", "Leap slot"), "Leap mode for the module.")

    private var clearLeap by SelectorSetting("Clear leap", "None", ArrayList(teamList), "Player name to leap to during clear.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s1leap by SelectorSetting("S1 leap", "None", ArrayList(teamList), "Player name to leap to when in S1.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s2leap by SelectorSetting("S2 leap", "None", ArrayList(teamList), "Player name to leap to when in S2.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s3leap by SelectorSetting("S3 leap", "None", ArrayList(teamList), "Player name to leap to when in S3.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s4leap by SelectorSetting("S4 leap", "None", ArrayList(teamList), "Player name to leap to when in S4.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var update by ActionSetting("Update", "Updates teammates names") { updateTeammates() }.withDependency { isFlopper && leapMode.selected == "Name" }

    private var clearLeapClass by SelectorSetting("Clear leap", "None", clazzArray, "Player class to leap to during clear.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s1leapClass by SelectorSetting("S1 leap", "Archer", clazzArray, "Player name to leap to when in S1.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s2leapClass by SelectorSetting("S2 leap", "Healer", clazzArray, "Player name to leap to when in S2.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s3leapClass by SelectorSetting("S3 leap", "Mage", clazzArray, "Player name to leap to when in S3.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s4leapClass by SelectorSetting("S4 leap", "Mage", clazzArray, "Player name to leap to when in S4.").withDependency { isFlopper && leapMode.selected == "Class" }

    init {
        listOf(clearLeap, s1leap, s2leap, s3leap, s4leap).forEach { it.options = teamList }
    }

    private val classMapping = mapOf(
        "Mage" to DungeonClass.Mage,
        "Berserk" to DungeonClass.Berserk,
        "Archer" to DungeonClass.Archer,
        "Tank" to DungeonClass.Tank,
        "Healer" to DungeonClass.Healer,
        "None" to DungeonClass.Unknown
    )

    private val isFlopper: Boolean get() = ClickGui.clickGui.selected == "Flopper"

    private fun updateTeammates() {
//        if (dungeonTeammatesNoSelf.isEmpty()) return

        val teammates = Party.members.take(4) + "None" // dungeonTeammatesNoSelf.map { it.name } + "None"
        teamList = teammates.toMutableList()
        listOf(clearLeap, s1leap, s2leap, s3leap, s4leap).forEach { it.options = teammates }
    }

    /*
    s1 min (89, 100, 48) max (113, 160, 122)
    s2 min (19, 100, 121) max (91, 160, 145)
    s3 min (-6, 100, 50) max (19, 160, 123)
    s4 min (17, 100, 27) max (90, 160, 50)
     */

    @SubscribeEvent
    fun onMouse(event: MouseEvent) {
        if (!event.buttonstate || event.button != 0 || !inDungeons || !fastLeap) return
        if (mc.thePlayer?.heldItem?.displayName?.noControlCodes != "Infinileap") return
        handleLeap()
    }

    @SubscribeEvent
    fun onChat(event: ChatPacket) {
        if (!inDungeons || !inBoss || !autoLeap) return
        val message = event.message
        if ("(7/7)" in message || "(8/8)" in message) {
            handleLeap()
        }
    }

    private val leapName get() = listOf(s1leap, s2leap, s3leap, s4leap, clearLeap)
    private val leapClazz get() = listOf(s1leapClass, s2leapClass, s3leapClass, s4leapClass, clearLeapClass)

    private fun getLeapName(key: String): String? {
        val i = namesOrder.keys.indexOf(key)
        return if (isFlopper) leapName[i].selected else namesOrder[key]
    }

    private fun getLeapClass(key: String): DungeonClass? {
        val i = clazzOrder.keys.indexOf(key)
        return if (isFlopper) classMapping[leapClazz[i].selected] else classMapping[clazzOrder[key]]
    }

    private fun getLeapSlotName(key: String): String? {
        val slotValue = slotOrder[key] ?: return null
        if (slotValue == "None") return null

        return slotValue.toIntOrNull()
            ?.minus(1)
            ?.let { LeapOrganiser.leapOrder.values.getSafe(it) }
            ?.takeIf { it.isNotEmpty() }
    }

    private fun handleLeap() {
        val section = getP3Section()
        val key = when {
            section in setOf(P3Sections.S1, P3Sections.S2, P3Sections.S3, P3Sections.S4) -> section.displayName
            !inBoss -> "Clear"
            else -> return
        }

        val name = getLeapName(key)
        val clazz = getLeapClass(key) ?: DungeonClass.Unknown
        val slotName = getLeapSlotName(key)

        debugMessage("leaping from $key $name $clazz $slotName")

        when (leapMode.selected) {
            "Name" -> leap(name ?: return)
            "Class" -> leap(clazz)
            "Leap slot" -> leap(slotName ?: return)
        }
    }
}