package dev.demeng.pluginbase.command.handlers;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.command.CommandBase;
import dev.demeng.pluginbase.command.annotations.Optional;
import dev.demeng.pluginbase.command.annotations.*;
import dev.demeng.pluginbase.command.exceptions.CustomCommandException;
import dev.demeng.pluginbase.command.objects.CommandData;
import dev.demeng.pluginbase.plugin.BaseLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

/** The handler for all the custom commands. For internal use. */
public final class CommandHandler extends Command {

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
      checkAlias(method, data);

      if (!data.isDef() && method.isAnnotationPresent(SubCommand.class)) {
        final String name = method.getAnnotation(SubCommand.class).value().toLowerCase();
        data.setName(name);
        commands.put(name, data);
      }

      if (data.isDef()) {
        data.setName("pb-default");
        commands.put("pb-default", data);
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

      return executeCommand(data, sender, arguments, settings);
    }

    final String argCommand = arguments[0].toLowerCase();

    if ((data != null && data.getArguments().size() == 0)
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

    return executeCommand(data, sender, arguments, settings);
  }

  private boolean executeCommand(
      CommandData subCommand, CommandSender sender, String[] arguments, BaseSettings settings) {
    try {

      final Method method = subCommand.getMethod();

      final List<String> argumentsList = new LinkedList<>(Arrays.asList(arguments));
      if (!subCommand.isDef() && argumentsList.size() > 0) argumentsList.remove(0);

      if (subCommand.getArguments().size() == 0 && argumentsList.size() == 0) {
        method.invoke(subCommand.getCommandBase(), sender);
        return true;
      }

      if (subCommand.getArguments().size() == 1
          && String[].class.isAssignableFrom(subCommand.getArguments().get(0))) {
        method.invoke(subCommand.getCommandBase(), sender, arguments);
        return true;
      }

      if (subCommand.getArguments().size() != argumentsList.size() && !subCommand.hasOptional()) {
        if (!subCommand.isDef() && subCommand.getArguments().size() == 0) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return true;
        }

        if (!String[]
            .class.isAssignableFrom(
                subCommand.getArguments().get(subCommand.getArguments().size() - 1))) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return true;
        }
      }

      final List<Object> invokeParams = new ArrayList<>();
      invokeParams.add(sender);

      for (int i = 0; i < subCommand.getArguments().size(); i++) {
        final Class<?> parameter = subCommand.getArguments().get(i);

        if (subCommand.hasOptional()) {
          if (argumentsList.size() > subCommand.getArguments().size()) {
            ChatUtils.tell(sender, settings.incorrectUsage());
            return true;
          }

          if (argumentsList.size() < subCommand.getArguments().size() - 1) {
            ChatUtils.tell(sender, settings.incorrectUsage());
            return true;
          }

          if (argumentsList.size() < subCommand.getArguments().size()) {
            argumentsList.add(null);
          }
        }

        if (subCommand.getArguments().size() > argumentsList.size()) {
          ChatUtils.tell(sender, settings.incorrectUsage());
          return true;
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

      return true;

    } catch (Throwable ex) {
      if (sender instanceof Player) {
        Common.error(ex, "Failed to execute command.", false, (Player) sender);
      } else {
        Common.error(ex, "Failed to execute command.", false);
      }
    }

    return true;
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
            argsList.remove("pb-default");
            //noinspection unchecked
            return (List<String>) completionMethod.invoke(data.getCommandBase(), argsList, sender);
          } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }

      final List<String> subCmd = new ArrayList<>(commands.keySet());
      subCmd.remove("pb-default");

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
          if (!commandName.toLowerCase().startsWith(args[0].toLowerCase())) continue;
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
        if (!completion.toLowerCase().contains(current.toLowerCase())) continue;
        completionList.add(completion);
      }
    } else {
      completionList = new ArrayList<>(completionHandler.getTypeResult(id, inputClazz));
    }

    Collections.sort(completionList);
    return completionList;
  }

  private CommandData getDefaultSubCommand() {
    return commands.get("pb-default");
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
      if (!method.isAnnotationPresent(CompleteFor.class)) {
        continue;
      }

      if (!(method.getGenericReturnType() instanceof ParameterizedType)) {
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

      if (parametrizedReturnType.getRawType() != List.class) {
        continue;
      }

      if (parametrizedReturnType.getActualTypeArguments().length != 1) {
        continue;
      }

      if (parametrizedReturnType.getActualTypeArguments()[0] != String.class) {
        continue;
      }

      if (!CommandSender.class.isAssignableFrom(method.getParameterTypes()[1])) {
        continue;
      }

      final String subCommandName = method.getAnnotation(CompleteFor.class).value();

      if (!subCommandName.equalsIgnoreCase(data.getName())) {
        continue;
      }

      data.setCompletionMethod(method);
    }
  }

  private void checkAlias(Method method, CommandData data) {

    if (!method.isAnnotationPresent(Alias.class)) {
      return;
    }

    for (String alias : method.getAnnotation(Alias.class).value()) {
      //noinspection UnnecessaryLocalVariable
      final CommandData aliasCD = data;
      data.setName(alias.toLowerCase());
      if (aliasCD.isDef()) aliasCD.setDef(false);
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
