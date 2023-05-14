package dev.josantonius.minecraft.command

import dev.josantonius.minecraft.messaging.Message
import java.io.File
import java.lang.reflect.Field
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(), Listener {
    lateinit var configuration: CommandConfig
    lateinit var message: Message
    private val userCommands = mutableMapOf<UUID, Pair<String, Array<String>>>()
    private val userTasks = mutableMapOf<UUID, Int>()

    override fun onEnable() {
        load()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        if (event.isCancelled) {
            return
        }

        val input = event.message.trim().split(" ")
        val commandData = configuration.getCommands()

        val foundCommandEntry =
                commandData
                        .entries
                        .mapNotNull { (commandName, _) ->
                            if (event.message.matches(Regex("/$commandName($|\\s).*")))
                                    commandName.length to commandName
                            else null
                        }
                        .maxByOrNull { it.first }
                        ?.let { (length, commandName) -> commandName to commandData[commandName] }

        if (foundCommandEntry != null) {
            val (commandName, command) = foundCommandEntry
            val player = event.player
            val cooldown =
                    if (player.hasPermission("command.admin")) 0 else (command?.cooldown ?: 0)
            val args = input.subList(commandName.split(" ").size, input.size).toTypedArray()
            event.isCancelled = true

            val existingTaskId = userTasks[player.uniqueId]
            if (existingTaskId != null) {
                Bukkit.getScheduler().cancelTask(existingTaskId)
            }

            setUserCommand(player, commandName, args)
            val taskId = scheduleUserTask(player, commandName, cooldown)
            updateUserTask(player, taskId)

            if (cooldown > 0) {
                sendMessage(player, "command.cooldown", cooldown.toString())
            }
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val uuid = player.uniqueId

        if (userCommands.containsKey(uuid) && userTasks.containsKey(uuid)) {
            val commandName = userCommands[uuid]?.first
            val commandData = configuration.getCommands()[commandName]
            val isExcluded =
                    configuration.excludeOpsFromCooldowns() && player.hasPermission("command.admin")
            val cooldown = if (isExcluded) 0 else (commandData?.cooldown ?: 0)

            if (cooldown > 0) {
                sendMessage(player, "error.player.moved")
                Bukkit.getScheduler().cancelTask(userTasks[uuid]!!)
                removeUserCommand(player)
                removeUserTask(player)
            }
        }
    }

    fun load() {
        val messagesFile = File(dataFolder, "messages.yml")
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false)
        }

        message = Message(messagesFile, this)
        message.setConsolePrefix("<dark_blue>[<blue>Command<dark_blue>] <white>")
        message.setChatPrefix("<dark_blue>[<blue>Command<dark_blue>] <white>")
        configuration = CommandConfig(this)
        HandlerList.getHandlerLists().forEach { handlerList ->
            handlerList.unregister(this as Listener)
        }
        val customCommandExecutor = CustomCommandExecutor(this)
        server.pluginManager.registerEvents(this, this)
        getCommand("command")?.setTabCompleter(CommandTabCompleter())
        getCommand("command")?.setExecutor(customCommandExecutor)
        registerCustomCommands(configuration.getCommandNames(), customCommandExecutor)
    }

    fun reload(sender: CommandSender) {
        load()
        sendMessage(sender, "plugin.reloaded")
    }

    fun sendMessage(sender: CommandSender, key: String, vararg params: String) {
        if (sender is Player) {
            message.sendToPlayer(sender, key, *params)
        } else {
            message.sendToConsole(key, *params)
        }
    }

    private fun setUserCommand(player: Player, commandName: String, args: Array<String>) {
        userCommands[player.uniqueId] = commandName to args
    }

    private fun removeUserCommand(player: Player) {
        userCommands.remove(player.uniqueId)
    }

    private fun updateUserTask(player: Player, taskId: Int) {
        userTasks[player.uniqueId] = taskId
    }

    private fun removeUserTask(player: Player) {
        userTasks.remove(player.uniqueId)
    }

    private fun scheduleUserTask(player: Player, commandName: String, cooldown: Int?): Int {
        return Bukkit.getScheduler()
                .runTaskLater(
                        this,
                        Runnable {
                            val uuid = player.uniqueId
                            if (userCommands.containsKey(uuid) && userTasks.containsKey(uuid)) {
                                val (pendingCommandName, args) = userCommands[uuid]!!
                                if (pendingCommandName == commandName) {
                                    val commandData =
                                            configuration.getCommands()[pendingCommandName]
                                    commandData?.actions?.forEach { action ->
                                        executeAction(player, action, args)
                                    }
                                    removeUserCommand(player)
                                    removeUserTask(player)
                                }
                            }
                        },
                        (cooldown ?: 0) * 20L
                )
                .taskId
    }

    private fun executeAction(player: Player, action: String, args: Array<String>) {
        val playerName = player.name
        var processedAction = action.replace("%player%", playerName)

        args.forEachIndexed { index, arg ->
            processedAction = processedAction.replace("{${index + 1}}", arg)
        }

        processedAction = processedAction.replace(Regex("\\{\\d+}"), "")
        if (processedAction.startsWith("command_console:")) {
            val command = processedAction.substring("command_console:".length).trim()
            if (command == "*") {
                val userCommandAndArgs = userCommands[player.uniqueId]
                if (userCommandAndArgs != null) {
                    val (userCommand, userArgs) = userCommandAndArgs
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "$userCommand ${userArgs.joinToString(" ")}"
                    )
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
            }
        } else if (processedAction.startsWith("command_player:")) {
            val command = processedAction.substring("command_player:".length).trim()
            if (command == "*") {
                val userCommandAndArgs = userCommands[player.uniqueId]
                if (userCommandAndArgs != null) {
                    val (userCommand, userArgs) = userCommandAndArgs
                    Bukkit.dispatchCommand(player, "$userCommand ${userArgs.joinToString(" ")}")
                }
            } else {
                Bukkit.dispatchCommand(player, command)
            }
        } else if (processedAction.startsWith("message:")) {
            var message = processedAction.substring("message:".length).trim()
            sendMessage(player, message, *args)
        }
    }

    private fun registerCustomCommands(
            commandNames: Set<String>,
            customCommandExecutor: CustomCommandExecutor
    ) {
        val commandMap: CommandMap = getCommandMap()
        commandNames.forEach { commandName ->
            val customCommand =
                    object : Command(commandName) {
                        override fun execute(
                                sender: CommandSender,
                                commandLabel: String,
                                args: Array<String>
                        ): Boolean {
                            return customCommandExecutor.onCommand(sender, this, commandLabel, args)
                        }
                    }
            commandMap.register(commandName, customCommand)
        }
    }

    private fun getCommandMap(): CommandMap {
        val bukkitCommandMapField: Field = server.javaClass.getDeclaredField("commandMap")
        bukkitCommandMapField.isAccessible = true
        return bukkitCommandMapField.get(server) as CommandMap
    }
}
