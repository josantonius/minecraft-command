# Minecraft Command Plugin

[![Release](https://jitpack.io/v/dev.josantonius/minecraft-command.svg)](https://jitpack.io/#dev.josantonius/minecraft-command)
[![License](https://img.shields.io/github/license/josantonius/minecraft-command)](LICENSE)

This plugin allows server administrators to create custom commands with specific actions and cooldowns.

Custom commands can be cancelled by other plugins in the `PlayerCommandPreprocessEvent` event.

## [Watch demo on YouTube](https://www.youtube.com/watch?v=whHH9OC_1qI)

## Requirements

- Java 17 or higher.
- Purpur server 1.19.3 or Bukkit/Spigot/Paper server compatible with the Purpur API version used.

## Installation

1. Download the JAR file: [command-1.0.0-purpur-1.19.3.jar](/build/libs/command-1.0.0-purpur-1.19.3.jar).

1. Place the JAR file in the plugins folder of your Minecraft server.

1. Restart the server to load the plugin.

## Building

To build the plugin yourself, follow these steps:

1. Make sure you have `Java 17` or higher and `Gradle` installed on your system.

1. Clone the plugin repository on your local machine:

    ```bash
    git clone https://github.com/josantonius/minecraft-command.git
    ```

1. Navigate to the directory of the cloned repository:

    ```bash
    cd minecraft-command
    ```

1. Use Gradle to compile the plugin:

    ```bash
    gradle build
    ```

## Commands

- `/command reload` - Reload the plugin

Requires the `command.admin` permission to be used.

## Configuration

The `config.yml` file located in the `plugins/Command` folder contains the plugin configuration.
To define custom commands and their actions, you can modify the commands section in the file.

### Add new custom command to the server

To add a new custom command to the server, you must add a new entry in the `commands` section of the
`plugins/Command/config.yml` file.

```yaml
commands:
  warpshop:
    cooldown: 5
    actions:
      - 'command_player: shop %player%'
      - 'message: Welcome to the shop, %player%!'
```

## Action Types

The config.yml file located in the `plugins/Command` folder contains the plugin configuration.
To define custom commands and their actions, you can modify the commands section in the file.

1. `Command Player`: Executes a command as the player. Use the prefix `command_player:` followed by
the command to execute. You can use `%player%` as a placeholder for the player's name.

    - `command_player: tp %player% 100 64 200`

1. `Command Console`: Executes a command as the console. Use the prefix `command_console`: followed
by the command to execute. You can use `%player%` as a placeholder for the player's name.

    - `command_console: op %player%`

1. `Message`: Sends a message to the player. Use the prefix message: followed by the text to send.
You can use `%player%` as a placeholder for the player's name.

    - `message: Welcome to the shop, %player%!`

      You can use the `messages.yml` file located in the `plugins/Command` folder to define custom
      messages for your commands. This allows you to easily manage and change command messages in a
      centralized location.

      To add a new message to the messages.yml file, simply create a new entry using a key and a
      value. The key will be used to reference the message in the custom command configuration, and
      the value will be the message content.

      Here's an example of how to add a login message to the `messages.yml` file:

      ```yaml
      user:
        login: "Login successful!"
      ```

      To use a message from the `messages.yml` file in a custom command, reference the message key
      in the message: action using the format key.subkey.

      For example, to use the login message in a custom command, you would do the following:

      ```yaml
      commands:
        login:
          actions:
            - 'message: user.login'
      ```

      When the login_command is executed, the player will receive the message "Login successful!" as
      defined in the `messages.yml` file. This allows you to easily update messages without having
      to modify the custom command configurations directly.

## Cooldowns

Cooldowns allow you to limit the frequency at which players can execute custom commands. To add a
cooldown to a custom command, you need to add a cooldown field in the command configuration in the
`config.yml` file located in the `plugins/Command` folder.

### Setting up a cooldown for a custom command

To set up a cooldown for a custom command, simply add the cooldown property to the command in the
`config.yml` file and specify the time in seconds.

Here's an example of how to set up a 60-second cooldown for the warpshop command:

```yaml
commands:
  warpshop:
    cooldown: 60
    actions:
      - 'command_player: tp %player% 100 64 200'
      - 'message: Welcome to the shop, %player%!'
```

### Cancellation of commands on cooldown

If a player moves while waiting for a command's cooldown to expire, the command will automatically
be cancelled, and the player will be notified that the command has been cancelled due to their movement.

## Command Arguments

You can also include command arguments in the actions. To do this, use placeholders in the format
`{1}`, `{2}`, `{3}`, and so on. These placeholders will be replaced by the corresponding arguments
passed by the player when executing the custom command.

Example of a custom command with arguments:

```yaml
commands:
  setwarp:
    actions:
      - 'command_console: setworldspawn %player% {1} {2} {3}'
      - 'message: Warp set at coordinates {1}, {2}, {3}'
```

When a player types `/setwarp 100 64 200`, the command action will be executed as `setworldspawn
playername 100 64 200`, and the message will be "Warp set at coordinates 100, 64, 200".

## Executing Command Exactly as Player Input

To execute the command exactly as the player input, you can use the `*` symbol. This takes the command
and arguments that the player entered, and executes them as they were entered.

```yaml
commands:
  'ps home':
    actions:
      - 'command_player: *'
    cooldown: 5
```

In this example, when a player enters the command ps home `ps-158-1555`, it will be executed exactly
as entered, with ps-158 being treated as an argument to the ps home command. The `*` acts as a
placeholder for any arguments passed by the player.

## Compatibility with Other Plugins

This plugin uses the PlayerCommandPreprocessEvent to handle custom commands. If you want to cancel
a custom command from another plugin, you can do so by calling `event.isCancelled` on the `PlayerCommandPreprocessEvent`.

## TODO

- [ ] Add new feature
- [ ] Create tests
- [ ] Improve documentation

## Changelog

Detailed changes for each release are documented in the
[release notes](https://github.com/josantonius/minecraft-command/releases).

## Contribution

Please make sure to read the [Contributing Guide](.github/CONTRIBUTING.md), before making a pull
request, start a discussion or report a issue.

Thanks to all [contributors](https://github.com/josantonius/minecraft-command/graphs/contributors)! :heart:

## Sponsor

If this project helps you to reduce your development time,
[you can sponsor me](https://github.com/josantonius#sponsor) to support my open source work :blush:

## License

This repository is licensed under the [MIT License](LICENSE).

Copyright © 2023-present, [Josantonius](https://github.com/josantonius#contact)
