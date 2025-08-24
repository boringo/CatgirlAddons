package catgirlroutes.commands

import catgirlroutes.commands.impl.*
import catgirlroutes.module.impl.render.BarRender.barSetter
import catgirlroutes.module.impl.render.CustomHighlight.highlightCommands
import catgirlroutes.module.impl.render.Waypoints.waypointCommands
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.rotation.rotationDebug
import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.nodes.Executable
import com.github.stivais.commodore.nodes.LiteralNode
import com.github.stivais.commodore.utils.findCorrespondingNode
import com.github.stivais.commodore.utils.getArgumentsRequired
import com.github.stivais.commodore.utils.getRootNode


object CommandRegistry {

    private val commands: ArrayList<Commodore> = arrayListOf(
        catgirlAddonsCommands, devCommands,
        pearlClip, lavaClip, blockClip, aura, inventoryButtons,
        autoP3Commands, autoRoutesCommands, rotationDebug,

        barSetter, waypointCommands, highlightCommands // todo move these
    )

    fun add(vararg commands: Commodore) {
        commands.forEach { commodore ->
            CommandRegistry.commands.add(commodore)
        }
    }

    fun register() {
        commands.forEach { commodore ->
            commodore.register { problem, cause ->
                val builder = StringBuilder()

                builder.append("§c$problem\n\n")
                builder.append("  Did you mean to run:\n\n")
                buildTreeString(cause, builder)

                findCorrespondingNode(getRootNode(cause), "help")?.let {
                    builder.append("\n  §7Run /${getArgumentsRequired(it).joinToString(" ")} for more help.")
                }
                modMessage(builder.toString())
            }
        }
    }

    private fun buildTreeString(from: LiteralNode, builder: StringBuilder) {
        for (node in from.children) {
            when (node) {
                is LiteralNode -> buildTreeString(node, builder)
                is Executable -> {
                    builder.append("  /${getArgumentsRequired(from).joinToString(" ")}")
                    for (parser in node.parsers) {
                        builder.append(" <${parser.name()}${if (parser.optional()) "?" else ""}>")
                    }
                    builder.append("\n")
                }
            }
        }
    }
}