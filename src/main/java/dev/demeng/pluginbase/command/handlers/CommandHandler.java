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

package dev.demeng.pluginbase.command.handlers;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.command.CommandBase;
import dev.demeng.pluginbase.command.annotations.Aliases;
import dev.demeng.pluginbase.command.annotations.CompleteFor;
import dev.demeng.pluginbase.command.annotations.Completion;
import dev.demeng.pluginbase.command.annotations.Default;
import dev.demeng.pluginbase.command.annotations.Optional;
import dev.demeng.pluginbase.command.annotations.Permission;
import dev.demeng.pluginbase.command.annotations.SubCommand;
import dev.demeng.pluginbase.command.exceptions.CustomCommandException;
import dev.demeng.pluginbase.command.objects.CommandData;
import dev.demeng.pluginbase.plugin.BaseLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The handler for all the custom commands. For internal use.
 */
public final class CommandHandler extends Command {

  private static final String DEFAULT_NAME = "pb-default";

  private final Map<String, CommandData> commands = new HashMap<>();

  private final ArgumentHandler argumentHandler;
  private final CompletionHandler completionHandler;

  public CommandHandler(
      String commandName,
      CommandBase command,
      List<String> aliases,
      ArgumentHandler argumentHandler,
      CompletionHandler completionHandler) {
    super(commandName);

    addSubCommands(command);
    setAliases(aliases);

    this.argumentHandler = argumentHandler;
    this.completionHandler = completionHandler;
  }

  public void addSubCommands(CommandBase command) {

    for (Method method : command.getClass().getDeclaredMethods()) {

      final CommandData data = new CommandData(command);

      if ((!method.isAnnotationPresent(Default.class)
          && !method.isAnnotationPresent(SubCommand.class))
          || !Modifier.isPublic(method.getModifiers())) {
        continue;
      }

      if (method.getParameterCount() == 0) {
        throw new CustomCommandException(
            String.format(
                "No method parameters for %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      if (!CommandSender.class.isAssignableFrom(method.getParameterTypes()[0])) {
        throw new CustomCommandException(
            String.format(
                "First parameter for method %s in class %s must be instance of CommandSender",
                method.getName(), method.getClass().getName()));
      }

      data.setMethod(method);
      data.setSenderClass(method.getParameterTypes()[0]);

      checkRegisteredParams(method, data);
      checkDefault(method, data);
      checkPermission(method, data);
      checkOptionalParam(method, data);
      checkParamCompletion(method, data);
      checkAliases(method, data);

      if (!data.isDef() && method.isAnnotationPresent(SubCommand.class)) {
        final String name = method.getAnnotation(SubCommand.class).value().toLowerCase();
        data.setName(name);
        commands.put(name, data);
      }

      if (data.isDef()) {
        data.setName(DEFAULT_NAME);
        commands.put(DEFAULT_NAME, data);
      }

      checkCompletionMethod(command, data);
    }
  }

  @Override
  public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] arguments) {

    final BaseSettings settings = BaseLoader.getPlugin().getBaseSettings();
    CommandData data = getDefaultSubCommand();

    if (arguments.length == 0 || arguments[0].isEmpty()) {

      if (data == null) {
        ChatUtils.tell(sender, settings.incorrectUsage());
        return true;
      }

      if (!Common.hasPermission(sender, data.getPermission())) {
        ChatUtils.tell(sender, settings.insufficientPermission());
        return true;
      }

      if (!(CommandSender.class.equals(data.getSenderClass())
          || ConsoleCommandSender.class.equals(data.getSenderClass()))
          && !(sender instanceof Player)) {
        ChatUtils.tell(sender, settings.notPlayer());
        return true;
      }

      executeCommand(data, sender, arguments, settings);
      return true;
    }

    final String argCommand = arguments[0].toLowerCase();

    if ((data != null && data.getArguments().isEmpty())
        && (!commands.containsKey(argCommand) || getName().equalsIgnoreCase(argCommand))) {
      ChatUtils.tell(sender, settings.incorrectUsage());
      return true;
    }

    if (data == null && !commands.containsKey(argCommand)) {
      ChatUtils.tell(sender, settings.incorrectUsage());
      return true;
    }

    if (commands.containsKey(argCommand)) {
      data = commands.get(argCommand);
    }

    Objects.requireNonNull(data);

    if (!Common.hasPermission(sender, data.getPermission())) {
      ChatUtils.tell(sender, settings.insufficientPermission());
      return true;
    }

    if (!(CommandSender.class.equals(data.getSenderClass())
        || ConsoleCommandSender.class.equals(data.getSenderClass()))
        && !(sender instanceof Player)) {
      ChatUtils.tell(sender, settings.notPlayer());
      return true;
    }

    executeCommand(data, sender, arguments, settings);
    return true;
  }

  private void executeCommand(
      CommandData subCommand, CommandSender sender, String[] arguments, BaseSettings settings) {
    try {

      final Method method = subCommand.getMethod();

      final List<String> argumentsList = new LinkedList<>(Arrays.asList(arguments));
      if (!subCommand.isDef() && !argumentsList.isEmpty()) {
        argumentsList.remove(0);
      }

      if (subCommand.getArguments().isEmpty() && argumentsList.isEmpty()) {
        method.invoke(subCommand.getCommandBase(), sender);
        return;
      }

      if (subCommand.getArguments().size() == 1
          && String[].class.isAssignableFrom(subCommand.getArguments().get(0))) {
        method.invoke(subCommand.getCommandBase(), sender, arguments);
        return;
      }

      if (subCommand.getArguments().size() != argumentsList.size() && !subCommand.hasOptional()) {
        if (!subCommand.isDef() && subCommand.getArguments().isEmpty()) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return;
        }

        if (!String[]
            .class.isAssignableFrom(
            subCommand.getArguments().get(subCommand.getArguments().size() - 1))) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return;
        }
      }

      final List<Object> invokeParams = new ArrayList<>();
      invokeParams.add(sender);

      for (int i = 0; i < subCommand.getArguments().size(); i++) {
        final Class<?> parameter = subCommand.getArguments().get(i);

        if (subCommand.hasOptional()) {
          if (argumentsList.size() > subCommand.getArguments().size()) {
            ChatUtils.tell(sender, settings.incorrectUsage());
            return;
          }

          if (argumentsList.size() < subCommand.getArguments().size() - 1) {
            ChatUtils.tell(sender, settings.incorrectUsage());
            return;
          }

          if (argumentsList.size() < subCommand.getArguments().size()) {
            argumentsList.add(null);
          }
        }

        if (subCommand.getArguments().size() > argumentsList.size()) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return;
        }

        Object argument = argumentsList.get(i);

        if (parameter.equals(String[].class)) {
          String[] args = new String[argumentsList.size() - i];

          for (int j = 0; j < args.length; j++) {
            args[j] = argumentsList.get(i + j);
          }

          argument = args;
        }

        final Object result =
            argumentHandler.getTypeResult(
                parameter, argument, subCommand, subCommand.getArgumentNames().get(i));
        invokeParams.add(result);
      }

      method.invoke(subCommand.getCommandBase(), invokeParams.toArray());

      subCommand.getCommandBase().clearArguments();
      return;

    } catch (Exception ex) {
      if (sender instanceof Player) {
        Common.error(ex, "Failed to execute command.", false, (Player) sender);
      } else {
        Common.error(ex, "Failed to execute command.", false);
      }
    }

    return;
  }

  @Override
  public @NotNull List<String> tabComplete(
      @NotNull CommandSender sender, @NotNull String alias, String[] args)
      throws IllegalArgumentException {

    if (args.length == 1) {
      List<String> commandNames = new ArrayList<>();

      final CommandData data = getDefaultSubCommand();

      if (data != null) {
        final Method completionMethod = data.getCompletionMethod();

        if (completionMethod != null) {
          try {
            List<String> argsList = new LinkedList<>(Arrays.asList(args));
            argsList.remove(DEFAULT_NAME);
            //noinspection unchecked
            return (List<String>) completionMethod.invoke(data.getCommandBase(), argsList, sender);
          } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }

      final List<String> subCmd = new ArrayList<>(commands.keySet());
      subCmd.remove(DEFAULT_NAME);

      for (String subCmdName : commands.keySet()) {
        final CommandData subCmdData = commands.get(subCmdName);
        if (!Common.hasPermission(sender, subCmdData.getPermission())) {
          subCmd.remove(subCmdName);
        }
      }

      if (data != null && data.getCompletions().size() != 0) {
        String id = data.getCompletions().get(1);
        Object inputClss = data.getArguments().get(0);

        if (id.contains(":")) {
          String[] values = id.split(":");
          id = values[0];
          inputClss = values[1];
        }

        subCmd.addAll(completionHandler.getTypeResult(id, inputClss));
      }

      if (!"".equals(args[0])) {
        for (String commandName : subCmd) {
          if (!commandName.toLowerCase().startsWith(args[0].toLowerCase())) {
            continue;
          }
          commandNames.add(commandName);
        }
      } else {
        commandNames = subCmd;
      }

      Collections.sort(commandNames);

      if (commandNames.isEmpty()) {
        return Collections.singletonList("");
      }

      return commandNames;
    }

    final String subCommandArg = args[0];

    if (!commands.containsKey(subCommandArg)) {
      return Collections.singletonList("");
    }

    final CommandData data = commands.get(subCommandArg);

    if (!Common.hasPermission(sender, data.getPermission())) {
      return Collections.singletonList("");
    }

    final Method completionMethod = data.getCompletionMethod();

    if (completionMethod != null) {
      try {
        List<String> argsList = new LinkedList<>(Arrays.asList(args));
        argsList.remove(subCommandArg);
        //noinspection unchecked
        return (List<String>) completionMethod.invoke(data.getCommandBase(), argsList, sender);
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    if (!data.getCompletions().containsKey(args.length - 1)) {
      return Collections.singletonList("");
    }

    String id = data.getCompletions().get(args.length - 1);

    List<String> completionList = new ArrayList<>();
    Object inputClazz = data.getArguments().get(args.length - 2);

    if (id.contains(":")) {
      String[] values = id.split(":");
      id = values[0];
      inputClazz = values[1];
    }

    final String current = args[args.length - 1];

    if (!"".equals(current)) {
      for (String completion : completionHandler.getTypeResult(id, inputClazz)) {
        if (!completion.toLowerCase().contains(current.toLowerCase())) {
          continue;
        }
        completionList.add(completion);
      }
    } else {
      completionList = new ArrayList<>(completionHandler.getTypeResult(id, inputClazz));
    }

    Collections.sort(completionList);
    return completionList;
  }

  private CommandData getDefaultSubCommand() {
    return commands.get(DEFAULT_NAME);
  }

  private void checkDefault(Method method, CommandData data) {
    if (!method.isAnnotationPresent(Default.class)) {
      return;
    }

    data.setDef(true);
  }

  private void checkRegisteredParams(Method method, CommandData data) {

    for (int i = 1; i < method.getParameterTypes().length; i++) {
      final Class<?> clazz = method.getParameterTypes()[i];

      if (clazz.equals(String[].class) && i != method.getParameterTypes().length - 1) {
        throw new CustomCommandException(
            String.format(
                "'String[] args' must be the last parameter for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      if (!argumentHandler.isRegisteredType(clazz)) {
        throw new CustomCommandException(
            String.format(
                "Invalid parameter types for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      data.getArguments().add(clazz);
      data.getArgumentNames().add(method.getParameters()[i].getName());
    }
  }

  private void checkPermission(Method method, CommandData data) {
    if (!method.isAnnotationPresent(Permission.class)) {
      return;
    }

    data.setPermission(method.getAnnotation(Permission.class).value());
  }

  private void checkParamCompletion(Method method, CommandData data) {

    for (int i = 0; i < method.getParameters().length; i++) {
      final Parameter parameter = method.getParameters()[i];

      if (i == 0 && parameter.isAnnotationPresent(Completion.class)) {
        throw new CustomCommandException(
            String.format(
                "Illegal @Completion annotation for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      final String[] values;
      if (parameter.isAnnotationPresent(Completion.class)) {
        values = parameter.getAnnotation(Completion.class).value();
      } else {
        continue;
      }

      if (values.length != 1) {
        throw new CustomCommandException(
            String.format(
                "Parameter completion can only have one value for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      if (!values[0].startsWith("#")) {
        throw new CustomCommandException(
            String.format(
                "Completion ID must start with '#' for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      if (completionHandler.isNotRegistered(values[0])) {
        throw new CustomCommandException(
            String.format(
                "Unregistered completion ID '%s' for method %s in class %s",
                values[0], method.getName(), method.getClass().getName()));
      }

      data.getCompletions().put(i, values[0]);
    }
  }

  private void checkCompletionMethod(CommandBase command, CommandData data) {

    for (final Method method : command.getClass().getDeclaredMethods()) {
      if (!method.isAnnotationPresent(CompleteFor.class) || !(method
          .getGenericReturnType() instanceof ParameterizedType)) {
        continue;
      }

      final ParameterizedType parametrizedReturnType =
          (ParameterizedType) method.getGenericReturnType();

      if (method.getParameterTypes().length != 2) {
        throw new CustomCommandException(
            String.format(
                "2 parameters, 'args, sender` required for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      final String subCommandName = method.getAnnotation(CompleteFor.class).value();

      if (parametrizedReturnType.getRawType() != List.class
          || parametrizedReturnType.getActualTypeArguments().length != 1
          || parametrizedReturnType.getActualTypeArguments()[0] != String.class
          || !CommandSender.class.isAssignableFrom(method.getParameterTypes()[1])
          || !subCommandName.equalsIgnoreCase(data.getName())) {
        continue;
      }

      data.setCompletionMethod(method);
    }
  }

  private void checkAliases(Method method, CommandData data) {

    if (!method.isAnnotationPresent(Aliases.class)) {
      return;
    }

    for (String alias : method.getAnnotation(Aliases.class).value()) {
      data.setName(alias.toLowerCase());
      if (data.isDef()) {
        data.setDef(false);
      }
      commands.put(alias.toLowerCase(), data);
    }
  }

  private void checkOptionalParam(Method method, CommandData data) {

    // Checks for completion on the parameters.
    for (int i = 0; i < method.getParameters().length; i++) {
      final Parameter parameter = method.getParameters()[i];

      if (i != method.getParameters().length - 1
          && parameter.isAnnotationPresent(
          dev.demeng.pluginbase.command.annotations.Optional.class)) {
        throw new CustomCommandException(
            String.format(
                "Optional arguments must be the last parameter for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      if (parameter.isAnnotationPresent(Optional.class)) {
        data.setOptional(true);
      }
    }
  }
}
