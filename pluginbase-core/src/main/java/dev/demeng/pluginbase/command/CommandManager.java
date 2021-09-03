/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2019 Matt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.pluginbase.command;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.command.annotations.Aliases;
import dev.demeng.pluginbase.command.annotations.Command;
import dev.demeng.pluginbase.command.exceptions.CustomCommandException;
import dev.demeng.pluginbase.command.handlers.ArgumentHandler;
import dev.demeng.pluginbase.command.handlers.CompletionHandler;
import dev.demeng.pluginbase.command.internal.CommandHandler;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the custom commands registered using the library.
 */
public final class CommandManager implements Listener {

  private CommandMap commandMap;

  @NotNull private final Map<String, CommandHandler> commands = new HashMap<>();
  @NotNull private Map<String, org.bukkit.command.Command> bukkitCommands = new HashMap<>();

  @Getter private final ArgumentHandler argumentHandler = new ArgumentHandler();
  @Getter private final CompletionHandler completionHandler = new CompletionHandler();

  /**
   * Creates a new command manager for the plugin. This constructor is designed for internal use-
   * you should only have 1 command manager per plugin, and that manager is already created by
   * {@link dev.demeng.pluginbase.plugin.BasePlugin}.
   */
  public CommandManager() {
    updateCommandMap();
  }

  /**
   * Registers a command.
   *
   * @param command The command to register
   */
  public void register(@NotNull final CommandBase command) {

    final Class<?> commandClass = command.getClass();

    final String commandName;

    if (!commandClass.isAnnotationPresent(Command.class)) {
      commandName = command.getCommand();
      if (commandName == null) {
        throw new CustomCommandException(
            command.getClass().getName() + " must be annotated with @Command");
      }

    } else {
      commandName = commandClass.getAnnotation(Command.class).value();
    }

    final List<String> aliases = command.getAliases();

    if (commandClass.isAnnotationPresent(Aliases.class)) {
      aliases.addAll(Arrays.asList(commandClass.getAnnotation(Aliases.class).value()));
    }

    final org.bukkit.command.Command oldCommand = commandMap.getCommand(commandName);

    if (oldCommand instanceof PluginIdentifiableCommand
        && ((PluginIdentifiableCommand) oldCommand).getPlugin() == BaseManager.getPlugin()) {
      bukkitCommands.remove(commandName);
      oldCommand.unregister(commandMap);
    }

    final CommandHandler commandHandler;
    if (commands.containsKey(commandName)) {
      commands.get(commandName).addSubCommands(command);
      return;
    }

    commandHandler =
        new CommandHandler(commandName, command, aliases, argumentHandler, completionHandler);

    commandMap.register(commandName, Common.getName(), commandHandler);
    commands.put(commandName, commandHandler);
  }

  /**
   * Unregisters all commands registered by this plugin.
   */
  public void unregisterAll() {
    commands.values().forEach(command -> command.unregister(commandMap));
  }

  private void updateCommandMap() {

    try {
      final Server server = Bukkit.getServer();
      final Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
      getCommandMap.setAccessible(true);

      this.commandMap = (CommandMap) getCommandMap.invoke(server);

      final Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
      knownCommands.setAccessible(true);

      //noinspection unchecked
      this.bukkitCommands = (Map<String, org.bukkit.command.Command>) knownCommands.get(commandMap);

    } catch (final ReflectiveOperationException ex) {
      Common.error(ex, "Failed to get command map.", false);
    }
  }
}
