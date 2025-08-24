package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.RawPacketReceiveEvent
import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.mixins.accessors.IEntityPlayerSPAccessor
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.*
//import catgirlroutes.ui.misc.OrderingGui
import catgirlroutes.utils.*
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.EtherWarpHelper.etherPos
import catgirlroutes.utils.dungeon.tiles.Room
import catgirlroutes.utils.render.WorldRenderUtils
import net.minecraft.block.Block.getIdFromBlock
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.*

object Test : Module(
    "Test",
    Category.MISC,
    "MODULE FOR VARIOUS TESTS. DO NOT USE",
    TagType.HARAM
) {
//    private val notifDropDown by DropdownSetting("Drop down", false)
//    private val notif by ActionSetting("notif", "nofiification") { Notifications.send("Info", "I think this is an info") }.withDependency(notifDropDown)
//    private val notifWarning by ActionSetting("notifWarning", "nofiification") { Notifications.send("Warning (long title test hello 123 herp me hurrep hai)", "I think this is a warning", 5000.0, NotificationType.WARNING) }.withDependency(notifDropDown)
//    private val notifErr by ActionSetting("notifErr", "nofiification") { Notifications.send("Error", "LONG DESCRIPTION TEST every day for the past month I've heard students non-stop joking about P. diddy, Epstein, R. kelly when a student has to go to take a piss he tells his friend group \"one minute bro i gotta pull an R kelly\" they say \"no diddy\" i think like another version of \"no homo\" one student was messing with the computer spamming the windows error sound and said \"this is how Stephen Hawking was moaning in those kids ears at epsteins island\" I've probably heard \"ain't no party like a diddy party\" a thousand times this month alone, im just tired of it all", type = NotificationType.ERROR, icon = "fallingkittens/bread.png") }.withDependency(notifDropDown)
//    private val notifErr2 by ActionSetting("notifErr", "nofiification") { Notifications.send("Error", "LONG DESCRIPTION TEST every day for the past month I've heard students non-stop joking about P. diddy, Epstein, R. kelly when a student has to go to take a piss he tells his friend group \"one minute bro i gotta pull an R kelly\" they say \"no diddy\" i think like another version of \"no homo\" one student was messing with the computer spamming the windows error sound and said \"this is how Stephen Hawking was moaning in those kids ears at epsteins island\" I've probably heard \"ain't no party like a diddy party\" a thousand times this month alone, im just tired of it all", type = NotificationType.ERROR) }.withDependency(notifDropDown)
//    private val notifIcon by ActionSetting("notifIcon", "nofiification") { Notifications.send("Info", "I think this is an info", icon = "Icon.png") }.withDependency(notifDropDown)
//
//    private val dropdownTest by DropdownSetting("Dropdown test")
//    private val stupid1 by BooleanSetting("stupid1").withDependency(dropdownTest)
//    private val stupid2 by BooleanSetting("stupid2").withDependency(dropdownTest) { this.stupid1 }
//    private val stupid3 by ColorSetting("stupid3", Color.WHITE).withDependency(dropdownTest) { this.stupid1 }
//
//    private val colourTest by ColorSetting("Colour", Color.BLUE)
//    private val colourTest2 by ColorSetting("Colour2", Color.BLUE, false)
//
//    private val slider by NumberSetting("Slider", 20.0, 0.0, 100.0, 5.0, unit = "px")
//    private val getHudWidth by ActionSetting("Get hud width") {
////        debugMessage(TestHud.width)
//    }
//
//    private val listTest by ListSetting("Test list", mutableListOf<Stupid>())
//    private val stupid4 by ActionSetting("Stupid4") { this.listTest.add(Stupid("stupid", 1)) }
//    private val stupid5 by ActionSetting("Stupid5") { debugMessage(this.listTest) }
//
//    private val mapTest by MapSetting("Test map", mutableMapOf<Double, Stupid>())
//    private val stupid6 by ActionSetting("Stupid6") { this.mapTest[Math.random()] = Stupid("stupid2", 2) }
//    private val stupid7 by ActionSetting("Stupid7") { debugMessage(this.mapTest) }
//
//    private val hudTest3 by HudSetting {
//        size(100.0, 100.0)
//        render {
//            drawString("this is a test", 0, 0)
//        }
//    }

//    private val orderSettingTest by OrderSetting("Order test 1", mapOf("s1" to "name1", "s2" to "name2", "s3" to "name3", "s4" to "name4", "clear" to "none"), 2, listOf("tom", "tim", "jim", "kim", "bob", "None")) {
//        options = listOf("aboba", "test321")
//    }
//
//    private val orderSettingTest2 by OrderSetting("Order test 2", mapOf("s1" to "name1", "s2" to "name2", "s3" to "name3", "s4" to "name4", "clear" to "none"), 2, listOf("Archer", "Berserk", "Healer", "Mage", "Tank", "None"))
//
//    private val orderSettingTest3 by OrderSetting("Order test 3", mapOf("1" to "name1", "2" to "name2", "3" to "name3", "4" to "name4"), 2)

//    private val stupid by ActionSetting("Stupid") { display = OrderingGui(mapOf("s1" to "name1", "s2" to "name2", "s3" to "name3", "s4" to "name4")) }

    private val stupid by ActionSetting("Stupid") { debugMessage(Party.members) }

    data class Stupid(val stupid1: String, val stupid2: Int)

    @SubscribeEvent
    fun onRawPacket(event: RawPacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        debugMessage(event.packet.windowTitle)
    }

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        if (event.room == null) return;

        val room: Room = event.room;
        ChatUtils.chatMessage("------------");
        ChatUtils.chatMessage("ROOM: " + room.data.name);
        ChatUtils.chatMessage("ROT: " + room.rotation);
        ChatUtils.chatMessage("CLAY: " + room.clayPos);
        ChatUtils.chatMessage("------------");
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (mc.thePlayer?.usingEtherWarp == false) return
        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val positionLook = PositionLook(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), player.lastReportedYaw, player.lastReportedPitch)

        etherPos = EtherWarpHelper.getEtherPos(positionLook)

        val succeeded = etherPos.succeeded && (mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || etherPos.state?.block?.let { invalidBlocks.get(getIdFromBlock(it)) } != true)

        WorldRenderUtils.drawBlock(etherPos.pos ?: return, if (succeeded) Color.GREEN else Color.RED)
    }

    private val invalidBlocks = BitSet().apply {
        setOf(
            Blocks.hopper, Blocks.chest, Blocks.ender_chest, Blocks.furnace, Blocks.crafting_table, Blocks.cauldron,
            Blocks.enchanting_table, Blocks.dispenser, Blocks.dropper, Blocks.brewing_stand, Blocks.trapdoor,
        ).forEach { set(getIdFromBlock(it)) }
    }

//    @RegisterHudElement
//    object TestHud : HudElement(
//        this,
//        width = FontUtil.getStringWidthDouble("Test"),
//        height = fontHeight + 2.0
//    ) {
//        override fun renderHud() {
//            FontUtil.drawStringWithShadow("Test", 0.0, 0.0)
//        }
//
//        override fun updateSize() {
//            this.width = FontUtil.getStringWidth("Test") + slider
//        }
//    }

}
