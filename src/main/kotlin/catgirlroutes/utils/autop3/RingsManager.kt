package catgirlroutes.utils.autop3

import catgirlroutes.module.impl.dungeons.AutoP3.selectedRoute
import catgirlroutes.module.impl.dungeons.Relics
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.configList
import catgirlroutes.utils.dungeon.DungeonUtils.getF7Phase
import catgirlroutes.utils.dungeon.M7Phases
import catgirlroutes.utils.typeName

object RingsManager {
    private val routes by configList<AutoP3Route>("p3_rings.json")
    val allRoutes get() = routes.toList()

    var currentRoute: List<AutoP3Route> = listOf()
    val SPECIAL_ROUTES = mapOf(
        "RelicsRoute" to { getF7Phase() == M7Phases.P5 && Relics.doCustomBlink }
    )

    var ringEditMode: Boolean = false
    var blinkEditMode: Boolean = false
    var blinkCd = false

//    val actions = listOf(
//        AlignRing,
//        BlinkRing(),
//        BoomRing(Vec3(0.0, 0.0, 0.0)),
//        CommandRing(""),
//        EdgeRing,
//        HClipRing,
//        JumpRing,
//        LavaClipRing(0.0),
//        LookRing,
//        MotionRing,
//        StopRing(),
//        SwapRing(""),
//        UseItemRing(""),
//        WalkRing
//    )

    fun init() {
        loadRoute()
    }

    fun loadRoute(route: String = selectedRoute, msg: Boolean = false) {
        routes.load()
        val inputRoutes = route.split(" ").filter { it.isNotBlank() }

        if (inputRoutes.isEmpty()) {
            return modMessage("Input is empty")
        }

        val matchedRoutes = routes.filter { it.name in inputRoutes }

        if (matchedRoutes.size != inputRoutes.size) {
            val validRoutes = matchedRoutes.map { it.name }.toSet()
            val invalidRoutes = inputRoutes - validRoutes
            return modMessage("Invalid routes found: §7${invalidRoutes.joinToString(", ") { it }}§r. Do §7/p3 create <§bname§7>")
        }

        selectedRoute = route
        val r = routes.filter { it.name in selectedRoute.split(" ") && it.name !in SPECIAL_ROUTES.keys }
        val special = routes.filter { it.name in SPECIAL_ROUTES.keys }.filter { it.name !in r.map { it.name } } // schizo

        currentRoute = r + if (Relics.blinkCustom) special else listOf()

        if (msg) modMessage("Loaded §7$route")
    }

    fun saveRoute() {
        routes.save()
    }

    fun loadSaveRoute() {
        saveRoute()
        loadRoute()
    }

    fun clearAll() {
        routes.clear()
    }

    fun addRing(ring: Ring): Boolean {
        val existingRoute = routes.firstOrNull { it.name == selectedRoute }
        if (existingRoute == null) return false
        existingRoute.rings.add(ring)
        routes.save()
        return true
    }

    fun createRoute(name: String) {
        if (routes.any { it.name == name }) return modMessage("Route §7$name§r already exists")
        val newRoute = AutoP3Route(name, mutableListOf())
        routes.add(newRoute)
        loadRoute(name)
        modMessage("Route §7$name§r created")
    }

    fun Ring.format(): String = buildString {
        append("§7${action.typeName.capitalize()}§r")
        arguments?.let { args ->
            append(" (${args.joinToString(", ") { it.typeName }})")
        }
    }
}