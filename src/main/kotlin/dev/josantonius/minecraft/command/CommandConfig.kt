package dev.josantonius.minecraft.command

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class CommandConfig(plugin: JavaPlugin) {
    private val config: FileConfiguration
    private val commands: Map<String, CommandData>

    init {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config
        commands = getCommandsFromConfig()
    }

    data class CommandData(val actions: List<String>, val cooldown: Int?)

    fun getCommands(): Map<String, CommandData> {
        return commands
    }

    fun getCommandActions(command: String): List<String>? {
        return commands[command]?.actions
    }

    fun getCommandCooldown(command: String): Int? {
        return commands[command]?.cooldown
    }

    fun getCommandNames(): Set<String> {
        return commands.keys
    }

    private fun getCommandsFromConfig(): Map<String, CommandData> {
        val commands = config.getConfigurationSection("commands") ?: return emptyMap()
        val commandsMap = mutableMapOf<String, CommandData>()

        for (commandKey in commands.getKeys(false)) {
            val actions = commands.getStringList("$commandKey.actions")
            val cooldown =
                    commands.getInt("$commandKey.cooldown", -1).let { if (it == -1) null else it }
            commandsMap[commandKey] = CommandData(actions, cooldown)
        }

        return commandsMap
    }
}
