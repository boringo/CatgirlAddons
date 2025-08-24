package catgirlroutes.utils.customtriggers.actions

import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.customtriggers.TypeName

@TypeName("CommandAction")
class CommandAction(val command: String, val commandType: CommandType) : TriggerAction() {

    enum class CommandType {
        AUTO, CLIENT, SERVER
    }

    override fun execute() {
        when (commandType) {
            CommandType.AUTO -> ChatUtils.commandAny(command)
            CommandType.CLIENT -> ChatUtils.command(command)
            CommandType.SERVER -> ChatUtils.command(command, false)
        }
    }
}