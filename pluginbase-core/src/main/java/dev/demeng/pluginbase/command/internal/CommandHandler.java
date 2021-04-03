/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.command.internal;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.command.CommandBase;
import dev.demeng.pluginbase.command.annotations.Aliases;
import dev.demeng.pluginbase.command.annotations.CompleteFor;
import dev.demeng.pluginbase.command.annotations.Completion;
import dev.demeng.pluginbase.command.annotations.Default;
import dev.demeng.pluginbase.command.annotations.Description;
import dev.demeng.pluginbase.command.annotations.Optional;
import dev.demeng.pluginbase.command.annotations.Permission;
import dev.demeng.pluginbase.command.annotations.SubCommand;
import dev.demeng.pluginbase.command.annotations.Usage;
import dev.demeng.pluginbase.command.exceptions.CustomCommandException;
import dev.demeng.pluginbase.command.handlers.ArgumentHandler;
import dev.demeng.pluginbase.command.handlers.CompletionHandler;
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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The handler for all the custom commands.
 */
public final class CommandHandler extends Command {

  private static final String DEFAULT_NAME = "pb-default";

  private final Map<String, CommandData> commands = new HashMap<>();

  private final ArgumentHandler argumentHandler;
  private final CompletionHandler completionHandler;

  /**
   * Initializes a handler for the command. For internal use. To properly register your commands,
   * use {@link dev.demeng.pluginbase.command.CommandManager#register(CommandBase)}.
   *
   * @param commandName       The name of the command
   * @param command           The command base
   * @param aliases           The command aliases
   * @param argumentHandler   The argument handler
   * @param completionHandler The completion handler
   */
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

  /**
   * Registers a new sub-command to the base command. For internal use only. To properly register
   * your commands, use {@link dev.demeng.pluginbase.command.CommandManager#register(CommandBase)}.
   *
   * @param command The command base to have its sub-commands registered
   */
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

      checkParameters(method, data);
      checkDefaultAnnotation(method, data);
      checkDescriptionAnnotation(method, data);
      checkUsageAnnotation(method, data);
      checkPermissionAnnotation(method, data);
      checkOptionalAnnotation(method, data);
      checkParametersCompletionAnnnotation(method, data);

      if (!data.isDef() && method.isAnnotationPresent(SubCommand.class)) {
        final String name = method.getAnnotation(SubCommand.class).value().toLowerCase();
        data.setName(name);
        commands.put(name, data);
      }

      if (data.isDef()) {
        data.setName(DEFAULT_NAME);
        commands.put(DEFAULT_NAME, data);
      }

      checkCompletionMethodAnnotation(command, data);
      checkAliasesAnnotation(method, data);
    }
  }

  /**
   * Overrides the default command execution method and instead use it to map all of our arguments,
   * check the requirements, etc. before actually invoking the execution method and executing the
   * command. You probably shouldn't be calling this method yourself.
   *
   * @param sender    The command sender
   * @param label     The command label
   * @param arguments The command arguments
   * @return Always true
   */
  @Override
  public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] arguments) {

    final BaseSettings settings = BaseLoader.getPlugin().getBaseSettings();
    CommandData data = getDefaultSubCommand();

    if (arguments.length == 0 || arguments[0].isEmpty()) {

      if (data == null) {
        ChatUtils.tell(sender, settings.incorrectUsage());
        return true;
      }

      if (checkSender(sender, data)) {
        runCommand(data, sender, arguments);
      }

      return true;
    }

    final String firstArg = arguments[0].toLowerCase();

    if (((data != null && data.getArguments().isEmpty())
        && (!commands.containsKey(firstArg) || getName().equalsIgnoreCase(firstArg))) || (
        data == null && !commands.containsKey(firstArg))) {
      ChatUtils.tell(sender, settings.incorrectUsage());
      return true;
    }

    if (commands.containsKey(firstArg)) {
      data = commands.get(firstArg);
    }

    if (data == null) {
      throw new CustomCommandException(String.format("Command data is null for '%s'", firstArg));
    }

    if (checkSender(sender, data)) {
      runCommand(data, sender, arguments);
    }

    return true;
  }

  private void runCommand(
      CommandData data, CommandSender sender, String[] arguments) {

    try {
      final Method method = data.getMethod();
      final List<String> argumentsList = new LinkedList<>(Arrays.asList(arguments));

      if (!data.isDef() && !argumentsList.isEmpty()) {
        argumentsList.remove(0);
      }

      if (data.getArguments().isEmpty() && argumentsList.isEmpty()) {
        method.invoke(data.getCommandBase(), sender);
        return;
      }

      if (data.getArguments().size() == 1
          && String[].class.isAssignableFrom(data.getArguments().get(0))) {
        method.invoke(data.getCommandBase(), sender, arguments);
        return;
      }

      if (data.getArguments().size() != argumentsList.size() && !data
          .isOptionalArgument()) {

        final BaseSettings settings = BaseLoader.getPlugin().getBaseSettings();

        if (!data.isDef() && data.getArguments().isEmpty()) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return;
        }

        if (!String[]
            .class.isAssignableFrom(
            data.getArguments().get(data.getArguments().size() - 1))) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return;
        }
      }

      runCommandWithParameters(data, sender, argumentsList);

    } catch (Exception ex) {
      if (sender instanceof Player) {
        Common.error(ex, "Failed to execute command.", false, (Player) sender);
      } else {
        Common.error(ex, "Failed to execute command.", false);
      }
    }
  }

  private void runCommandWithParameters(CommandData data, CommandSender sender,
      List<String> argumentsList)
      throws InvocationTargetException, IllegalAccessException {

    final BaseSettings settings = BaseLoader.getPlugin().getBaseSettings();

    final List<Object> invokeParams = new ArrayList<>();
    invokeParams.add(sender);

    for (int index = 0; index < data.getArguments().size(); index++) {
      final Class<?> parameter = data.getArguments().get(index);

      if (data.isOptionalArgument()) {
        if (argumentsList.size() > data.getArguments().size()) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return;
        }

        if (argumentsList.size() < data.getArguments().size() - 1) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return;
        }

        if (argumentsList.size() < data.getArguments().size()) {
          argumentsList.add(null);
        }
      }

      if (data.getArguments().size() > argumentsList.size()) {
        ChatUtils.tell(sender, settings.incorrectUsage());
        return;
      }

      Object argument = argumentsList.get(index);

      if (parameter.equals(String[].class)) {
        String[] args = new String[argumentsList.size() - index];

        for (int n = 0; n < args.length; n++) {
          args[n] = argumentsList.get(index + n);
        }

        argument = args;
      }

      invokeParams.add(argumentHandler.getTypeResult(
          parameter, argument, data, data.getArgumentNames().get(index)));
    }

    data.getMethod().invoke(data.getCommandBase(), invokeParams.toArray());
    data.getCommandBase().clearArguments();
  }

  @Override
  @NotNull
  public List<String> tabComplete(
      @NotNull CommandSender sender, @NotNull String alias, String[] args)
      throws IllegalArgumentException {

    if (args.length == 1) {
      List<String> commandNames = new ArrayList<>();

      final CommandData data = getDefaultSubCommand();

      if (data != null) {
        final List<String> result = invokeCompletionMethod(data.getCompletionMethod(),
            data.getCommandBase(), DEFAULT_NAME, sender, args);

        if (result != null) {
          return result;
        }
      }

      final List<String> subCmd = new ArrayList<>(commands.keySet());
      subCmd.remove(DEFAULT_NAME);

      for (Map.Entry<String, CommandData> entry : commands.entrySet()) {
        if (!Common.hasPermission(sender, entry.getValue().getPermission())) {
          subCmd.remove(entry.getKey());
        }
      }

      if (data != null && data.getCompletions().size() != 0) {
        String id = data.getCompletions().get(1);
        Object inputClazz = data.getArguments().get(0);

        if (id.contains(":")) {
          String[] values = id.split(":");
          id = values[0];
          inputClazz = values[1];
        }

        subCmd.addAll(completionHandler.getTypeResult(id, inputClazz));
      }

      if (!args[0].equals("")) {
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
      final List<String> result = invokeCompletionMethod(completionMethod, data.getCommandBase(),
          subCommandArg, sender, args);

      if (result != null) {
        return result;
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

    if (!current.equals("")) {
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

  private void checkParameters(Method method, CommandData data) {

    for (int i = 1; i < method.getParameterTypes().length; i++) {
      final Class<?> clazz = method.getParameterTypes()[i];

      if (clazz.equals(String[].class) && method.getParameterTypes().length - 1 != i) {
        throw new CustomCommandException(
            String.format(
                "'String[] args' must be the last parameter for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      System.out.println(argumentHandler == null);

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

  private void checkDefaultAnnotation(Method method, CommandData data) {
    data.setDef(method.isAnnotationPresent(Default.class));
  }

  private void checkDescriptionAnnotation(Method method, CommandData data) {
    if (method.isAnnotationPresent(Description.class)) {
      data.setDescription(method.getAnnotation(Description.class).value());
    }
  }

  private void checkUsageAnnotation(Method method, CommandData data) {
    if (method.isAnnotationPresent(Usage.class)) {
      data.setUsage(method.getAnnotation(Usage.class).value());
    }
  }

  private void checkPermissionAnnotation(Method method, CommandData data) {
    if (method.isAnnotationPresent(Permission.class)) {
      data.setPermission(method.getAnnotation(Permission.class).value());
    }
  }

  private void checkOptionalAnnotation(Method method, CommandData data) {

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
        data.setOptionalArgument(true);
      }
    }
  }

  private void checkParametersCompletionAnnnotation(Method method, CommandData data) {

    for (int i = 0; i < method.getParameters().length; i++) {
      final Parameter parameter = method.getParameters()[i];

      if (!parameter.isAnnotationPresent(Completion.class)) {
        continue;
      }

      if (i == 0) {
        throw new CustomCommandException(
            String.format(
                "Illegal @Completion annotation for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      final String value = parameter.getAnnotation(Completion.class).value();

      if (!value.startsWith("#")) {
        throw new CustomCommandException(
            String.format(
                "Completion ID must start with '#' for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      if (completionHandler.isRegistered(value)) {
        throw new CustomCommandException(
            String.format(
                "Unregistered completion ID '%s' for method %s in class %s",
                value, method.getName(), method.getClass().getName()));
      }

      data.getCompletions().put(i, value);
    }
  }

  private void checkCompletionMethodAnnotation(CommandBase command, CommandData data) {

    for (Method method : command.getClass().getDeclaredMethods()) {
      if (!method.isAnnotationPresent(CompleteFor.class) || !(method
          .getGenericReturnType() instanceof ParameterizedType)) {
        continue;
      }

      final ParameterizedType returnType =
          (ParameterizedType) method.getGenericReturnType();

      if (method.getParameterTypes().length != 2) {
        throw new CustomCommandException(
            String.format(
                "2 parameters, 'args, sender` required for method %s in class %s",
                method.getName(), method.getClass().getName()));
      }

      if (isCompletionMethod(method, returnType, data)) {
        data.setCompletionMethod(method);
      }
    }
  }

  private boolean isCompletionMethod(Method method, ParameterizedType returnType,
      CommandData data) {
    return returnType.getRawType() == List.class
        && returnType.getActualTypeArguments().length == 1
        && returnType.getActualTypeArguments()[0] == String.class
        && CommandSender.class.isAssignableFrom(method.getParameterTypes()[1])
        && method.getAnnotation(CompleteFor.class).value().equalsIgnoreCase(data.getName());
  }

  private void checkAliasesAnnotation(Method method, CommandData data) {

    if (!method.isAnnotationPresent(Aliases.class)) {
      return;
    }

    for (String alias : method.getAnnotation(Aliases.class).value()) {
      final CommandData clone = data.copy();

      clone.setName(alias.toLowerCase());
      clone.setDef(false);
      commands.put(alias.toLowerCase(), clone);
    }
  }

  private boolean checkSender(CommandSender sender, CommandData data) {

    final BaseSettings settings = BaseLoader.getPlugin().getBaseSettings();

    if (!(CommandSender.class.equals(data.getSenderClass())
        || ConsoleCommandSender.class.equals(data.getSenderClass()))
        && !(sender instanceof Player)) {
      ChatUtils.tell(sender, settings.notPlayer());
      return false;
    }

    if (!Common.hasPermission(sender, data.getPermission())) {
      ChatUtils.tell(sender, settings.insufficientPermission());
      return false;
    }

    return true;
  }

  private List<String> invokeCompletionMethod(Method method, CommandBase base, String arg,
      CommandSender sender,
      String[] args) {

    if (method == null) {
      return null;
    }

    try {
      final List<String> argsList = new LinkedList<>(Arrays.asList(args));
      argsList.remove(arg);

      //noinspection unchecked
      return (List<String>) method.invoke(base, argsList, sender);

    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new CustomCommandException(String
          .format("Could not invoke completion method for method %s in class %s",
              method.getName(), method.getClass().getName()), ex);
    }
  }
}
