package dev.josantonius.minecraft.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CustomCommandExecutor(private val plugin: Main) : CommandExecutor {

    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<String>
    ): Boolean {

        if (command.name.lowercase() == "command" && args.isNotEmpty()) {
            if (!sender.hasPermission("command.admin")) {
                sender.sendMessage("error.command.permission")
                return true
            }
            if (args[0].lowercase() == "reload") {
                plugin.reload(sender)
                return true
            }
        }
        return false
    }
}
