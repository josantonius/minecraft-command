package dev.josantonius.minecraft.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CommandTabCompleter : TabCompleter {

    override fun onTabComplete(
            sender: CommandSender,
            cmd: Command,
            label: String,
            args: Array<String>
    ): List<String>? {
        if (sender !is Player) {
            return null
        }

        if (cmd.name.equals("command", ignoreCase = true)) {
            when (args.size) {
                1 -> {
                    val subcommands = mutableListOf("reload")
                    return subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
                }
            }
        }
        return emptyList()
    }
}
