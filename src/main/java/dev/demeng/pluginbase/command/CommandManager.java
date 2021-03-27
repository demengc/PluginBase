package dev.demeng.pluginbase.command;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.command.annotations.Alias;
import dev.demeng.pluginbase.command.annotations.Command;
import dev.demeng.pluginbase.command.exceptions.CustomCommandException;
import dev.demeng.pluginbase.command.handlers.ArgumentHandler;
import dev.demeng.pluginbase.command.handlers.CommandHandler;
import dev.demeng.pluginbase.command.handlers.CompletionHandler;
import dev.demeng.pluginbase.plugin.BaseLoader;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Handles the custom commands registered using the library. */
public final class CommandManager implements Listener {

  private CommandMap commandMap;

  private final Map<String, CommandHandler> commands = new HashMap<>();
  private Map<String, org.bukkit.command.Command> bukkitCommands = new HashMap<>();

  private final ArgumentHandler argumentHandler = new ArgumentHandler();
  private final CompletionHandler completionHandler = new CompletionHandler();

  /**
   * Creates a new command manager for the plugin. This constructor is designed for internal use-
   * you should only have 1 command manager per plugin, and that maanger is already created by
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
  public void register(CommandBase command) {

    final Class<?> commandClass = command.getClass();

    String commandName;

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

    if (commandClass.isAnnotationPresent(Alias.class)) {
      aliases.addAll(Arrays.asList(commandClass.getAnnotation(Alias.class).value()));
    }

    org.bukkit.command.Command oldCommand = commandMap.getCommand(commandName);

    if (oldCommand instanceof PluginIdentifiableCommand
        && ((PluginIdentifiableCommand) oldCommand).getPlugin() == BaseLoader.getPlugin()) {
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

  /** Unregisters all commands registered by this plugin. */
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

    } catch (ReflectiveOperationException ex) {
      Common.error(ex, "Failed to get command map.", false);
    }
  }

  /**
   * The command arguments handler.
   *
   * @return The command arguments handler
   */
  public ArgumentHandler getParameterHandler() {
    return argumentHandler;
  }

  /**
   * The tab completion handler.
   *
   * @return The tab completion handler
   */
  public CompletionHandler getCompletionHandler() {
    return completionHandler;
  }
}
