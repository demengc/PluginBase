/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package dev.demeng.pluginbase.commands.core;

import static dev.demeng.pluginbase.commands.util.Collections.listOf;
import static dev.demeng.pluginbase.commands.util.Strings.getName;
import static dev.demeng.pluginbase.commands.util.Strings.splitBySpace;
import static java.util.Collections.addAll;
import static java.util.stream.Collectors.toMap;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.annotation.Command;
import dev.demeng.pluginbase.commands.annotation.Default;
import dev.demeng.pluginbase.commands.annotation.Description;
import dev.demeng.pluginbase.commands.annotation.Flag;
import dev.demeng.pluginbase.commands.annotation.SecretCommand;
import dev.demeng.pluginbase.commands.annotation.Single;
import dev.demeng.pluginbase.commands.annotation.Subcommand;
import dev.demeng.pluginbase.commands.annotation.Switch;
import dev.demeng.pluginbase.commands.annotation.Usage;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.CommandPermission;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.core.reflect.MethodCaller.BoundMethodCaller;
import dev.demeng.pluginbase.commands.orphan.OrphanCommand;
import dev.demeng.pluginbase.commands.orphan.OrphanRegistry;
import dev.demeng.pluginbase.commands.process.ParameterResolver;
import dev.demeng.pluginbase.commands.process.ParameterValidator;
import dev.demeng.pluginbase.commands.process.PermissionReader;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import dev.demeng.pluginbase.commands.util.Preconditions;
import dev.demeng.pluginbase.commands.util.Primitives;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

final class CommandParser {

  static final ResponseHandler<?> VOID_HANDLER = (response, actor, command) -> {
  };
  private static final AtomicInteger COMMAND_ID = new AtomicInteger();

  private CommandParser() {
  }

  public static void parse(@NotNull final BaseCommandHandler handler,
      @NotNull final OrphanRegistry orphan) {
    final OrphanCommand instance = orphan.getHandler();
    final Class<?> type = instance.getClass();

    // we pass the type of the orphan handler, but pass the object as the orphan registry
    parse(handler, type, orphan);
  }

  public static void parse(@NotNull final BaseCommandHandler handler,
      @NotNull final Object boundTarget) {
    final Class<?> type =
        boundTarget instanceof Class ? (Class<?>) boundTarget : boundTarget.getClass();
    parse(handler, type, boundTarget);
  }

  @SneakyThrows
  public static void parse(@NotNull final BaseCommandHandler handler,
      @NotNull final Class<?> container,
      @NotNull final Object boundTarget) {
    final Map<CommandPath, BaseCommandCategory> categories = handler.categories;
    final Map<CommandPath, CommandExecutable> subactions = new HashMap<>();
    for (final Method method : getAllMethods(container)) {
      final AnnotationReader reader = AnnotationReader.create(handler, method);
      Object invokeTarget = boundTarget;
      if (reader.shouldDismiss()) {
        continue;
      }
      if (boundTarget instanceof OrphanRegistry) {
        insertCommandPath((OrphanRegistry) boundTarget, reader);
        invokeTarget = ((OrphanRegistry) invokeTarget).getHandler();
      }
      reader.distributeAnnotations();
      reader.replaceAnnotations(handler);
      final List<CommandPath> paths = getCommandPath(container, method, reader);
      final BoundMethodCaller caller = handler.getMethodCallerFactory().createFor(method)
          .bindTo(invokeTarget);
      final int id = COMMAND_ID.getAndIncrement();
      final boolean isDefault = reader.contains(Default.class);
      paths.forEach(path -> {
        for (final BaseCommandCategory category : getCategories(handler, isDefault, path)) {
          categories.putIfAbsent(category.path, category);
        }
        final CommandExecutable executable = new CommandExecutable();
        if (!isDefault) {
          categories.remove(path); // prevent duplication.
        }
        executable.name = path.getLast();
        executable.id = id;
        executable.handler = handler;
        executable.description = reader.get(Description.class, Description::value);
        executable.path = path;
        executable.method = method;
        executable.reader = reader;
        executable.secret = reader.contains(SecretCommand.class);
        executable.methodCaller = caller;
        if (isDefault) {
          executable.parent(categories.get(path));
        } else {
          executable.parent(categories.get(path.getCategoryPath()));
        }
        executable.responseHandler = getResponseHandler(handler, method.getGenericReturnType());
        executable.parameters = getParameters(handler, method, executable);
        executable.resolveableParameters = executable.parameters.stream()
            .filter(c -> c.getCommandIndex() != -1)
            .collect(toMap(CommandParameter::getCommandIndex, c -> c));
        executable.usage = reader.get(Usage.class, Usage::value, () -> generateUsage(executable));
        if (reader.contains(Default.class)) {
          subactions.put(path, executable);
        } else {
          putOrError(handler.executables, path, executable,
              "A command with path '" + path.toRealString() + "' already exists!");
        }
      });
    }

    subactions.forEach((path, subaction) -> {
      final BaseCommandCategory cat = categories.get(path);
      if (cat != null) { // should never be null but let's just do that
        cat.defaultAction = subaction;
      }
    });
  }

  private static void insertCommandPath(final OrphanRegistry boundTarget,
      final AnnotationReader reader) {
    final List<CommandPath> paths = boundTarget.getParentPaths();
    final String[] pathsArray = paths.stream().map(CommandPath::toRealString)
        .toArray(String[]::new);
    reader.add(new Command() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Command.class;
      }

      @Override
      public String[] value() {
        return pathsArray;
      }
    });
  }

  private static Set<Method> getAllMethods(final Class<?> c) {
    final Set<Method> methods = new HashSet<>();
    Class<?> current = c;
    while (current != null && current != Object.class) {
      addAll(methods, current.getDeclaredMethods());
      current = current.getSuperclass();
    }
    return methods;
  }

  private static String generateUsage(@NotNull final ExecutableCommand command) {
    final StringJoiner joiner = new StringJoiner(" ");
    final CommandHandler handler = command.getCommandHandler();
    for (final CommandParameter parameter : command.getValueParameters().values()) {
      if (!parameter.getResolver().mutatesArguments()) {
        continue;
      }
      if (parameter.isSwitch()) {
        joiner.add("[" + handler.getSwitchPrefix() + parameter.getSwitchName() + "]");
      } else if (parameter.isFlag()) {
        joiner.add("[" + handler.getFlagPrefix() + parameter.getFlagName() + " <value>]");
      } else {
        if (parameter.isOptional()) {
          joiner.add("[" + parameter.getName() + "]");
        } else {
          joiner.add("<" + parameter.getName() + ">");
        }
      }
    }
    return joiner.toString();
  }

  @SuppressWarnings("rawtypes")
  private static ResponseHandler<?> getResponseHandler(final BaseCommandHandler handler,
      final Type genericType) {
    final Class<?> rawType = Primitives.getRawType(genericType);
    if (CompletionStage.class.isAssignableFrom(rawType)) {
      final ResponseHandler delegateHandler = getResponseHandler(handler,
          getInsideGeneric(genericType));
      return new CompletionStageResponseHandler(handler, delegateHandler);
    }
    if (Optional.class.isAssignableFrom(rawType)) {
      final ResponseHandler delegateHandler = getResponseHandler(handler,
          getInsideGeneric(genericType));
      return new OptionalResponseHandler(delegateHandler);
    }
    if (Supplier.class.isAssignableFrom(rawType)) {
      final ResponseHandler delegateHandler = getResponseHandler(handler,
          getInsideGeneric(genericType));
      return new SupplierResponseHandler(delegateHandler);
    }
    return handler.responseHandlers.getFlexibleOrDefault(rawType, VOID_HANDLER);
  }

  private static Type getInsideGeneric(final Type genericType) {
    try {
      return ((ParameterizedType) genericType).getActualTypeArguments()[0];
    } catch (final ClassCastException e) {
      return Object.class;
    }
  }

  private static Set<BaseCommandCategory> getCategories(final CommandHandler handler,
      final boolean respectDefault, @NotNull final CommandPath path) {
    if (path.size() == 1 && !respectDefault) {
      return Collections.emptySet();
    }
    final String parent = path.getParent();
    final Set<BaseCommandCategory> categories = new HashSet<>();

    final BaseCommandCategory root = new BaseCommandCategory();
    root.handler = handler;
    root.path = CommandPath.get(parent);
    root.name = parent;
    categories.add(root);

    final List<String> pathList = new ArrayList<>();
    pathList.add(parent);

    for (final String subcommand : path.getSubcommandPath()) {
      pathList.add(subcommand);
      final BaseCommandCategory cat = new BaseCommandCategory();
      cat.handler = handler;
      cat.path = CommandPath.get(pathList);
      cat.name = cat.path.getName();
      categories.add(cat);
    }

    return categories;
  }

  private static List<CommandParameter> getParameters(@NotNull final BaseCommandHandler handler,
      @NotNull final Method method,
      @NotNull final CommandExecutable parent) {
    final List<CommandParameter> parameters = new ArrayList<>();
    final Parameter[] methodParameters = method.getParameters();
    int cIndex = 0;
    for (int i = 0; i < methodParameters.length; i++) {
      final Parameter parameter = methodParameters[i];
      final AnnotationReader paramAnns = AnnotationReader.create(handler, parameter);
      final List<ParameterValidator<Object>> validators = new ArrayList<>(
          handler.validators.getFlexibleOrDefault(parameter.getType(), Collections.emptyList())
      );

      final BaseCommandParameter param = new BaseCommandParameter(
          getName(parameter),
          paramAnns.get(Description.class, Description::value),
          i,
          paramAnns.get(Default.class, Default::value),
          i == methodParameters.length - 1 && !paramAnns.contains(Single.class),
          paramAnns.contains(dev.demeng.pluginbase.commands.annotation.Optional.class)
              || paramAnns.contains(Default.class),
          parent,
          parameter,
          paramAnns.get(Switch.class),
          paramAnns.get(Flag.class),
          Collections.unmodifiableList(validators)
      );

      for (final PermissionReader reader : handler.getPermissionReaders()) {
        final CommandPermission permission = reader.getPermission(param);
        if (permission != null) {
          param.permission = permission;
          break;
        }
      }

      if (param.getType().isPrimitive() && param.isOptional() && param.getDefaultValue() == null
          && !param.isSwitch()) {
        throw new IllegalStateException(
            "Optional parameter " + parameter + " at " + method + " cannot be a prmitive!");
      }
      if (param.isSwitch()) {
        if (Primitives.wrap(param.getType()) != Boolean.class) {
          throw new IllegalStateException(
              "Switch parameter " + parameter + " at " + method + " must be of boolean type!");
        }
      }

      final ParameterResolver<?> resolver = handler.getResolver(param);

      if (resolver == null) {
        throw new IllegalStateException(
            "Unable to find a resolver for parameter type " + parameter.getType());
      }
      param.resolver = resolver;
      if (resolver.mutatesArguments()) {
        param.cindex = cIndex++;
      }
      param.suggestionProvider = handler.autoCompleter.getProvider(param);
      parameters.add(param);
    }
    return Collections.unmodifiableList(parameters);
  }

  private static List<CommandPath> getCommandPath(@NotNull final Class<?> container,
      @NotNull final Method method,
      @NotNull final AnnotationReader reader) {
    final List<CommandPath> paths = new ArrayList<>();

    final List<String> commands = new ArrayList<>();
    final List<String> subcommands = new ArrayList<>();
    final Command commandAnnotation = reader.get(Command.class, "Method " + method.getName()
        + " does not have a parent command! You might have forgotten one of the following:\n" +
        "- @Command on the method or class\n" +
        "- implement OrphanCommand");
    Preconditions.notEmpty(commandAnnotation.value(), "@Command#value() cannot be an empty array!");
    addAll(commands, commandAnnotation.value());

    final List<String> parentSubcommandAliases = new ArrayList<>();

    for (final Class<?> topClass : getTopClasses(container)) {
      final Subcommand ps = topClass.getAnnotation(Subcommand.class);
      if (ps != null) {
        addAll(parentSubcommandAliases, ps.value());
      }
    }

    final Subcommand subcommandAnnotation = reader.get(Subcommand.class);
    if (subcommandAnnotation != null) {
      addAll(subcommands, subcommandAnnotation.value());
    }

    for (final String command : commands) {
      if (!subcommands.isEmpty()) {
        for (final String subcommand : subcommands) {
          final List<String> path = new ArrayList<>(splitBySpace(command));
          parentSubcommandAliases.forEach(
              subcommandAlias -> path.addAll(splitBySpace(subcommandAlias)));
          path.addAll(splitBySpace(subcommand));
          paths.add(CommandPath.get(path));
        }
      } else {
        paths.add(CommandPath.get(splitBySpace(command)));
      }
    }
    return paths;
  }

  private static List<Class<?>> getTopClasses(Class<?> c) {
    final List<Class<?>> classes = listOf(c);
    final Class<?> enclosingClass = c.getEnclosingClass();
    while (c.getEnclosingClass() != null) {
      classes.add(c = enclosingClass);
    }
    Collections.reverse(classes);
    return classes;
  }

  private static <K, V> void putOrError(final Map<K, V> map, final K key, final V value,
      final String err) {
    if (map.containsKey(key)) {
      throw new IllegalStateException(err);
    }
    map.put(key, value);
  }

}
