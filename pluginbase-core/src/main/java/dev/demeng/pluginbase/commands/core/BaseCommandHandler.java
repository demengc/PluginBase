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

import static dev.demeng.pluginbase.commands.util.Preconditions.coerceIn;
import static dev.demeng.pluginbase.commands.util.Preconditions.notEmpty;
import static dev.demeng.pluginbase.commands.util.Preconditions.notNull;
import static dev.demeng.pluginbase.commands.util.Primitives.getType;
import static dev.demeng.pluginbase.commands.util.Strings.splitBySpace;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.CommandHandlerVisitor;
import dev.demeng.pluginbase.commands.annotation.Dependency;
import dev.demeng.pluginbase.commands.annotation.Range;
import dev.demeng.pluginbase.commands.annotation.dynamic.AnnotationReplacer;
import dev.demeng.pluginbase.commands.autocomplete.AutoCompleter;
import dev.demeng.pluginbase.commands.command.ArgumentParser;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandCategory;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.CommandPermission;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.core.reflect.MethodCallerFactory;
import dev.demeng.pluginbase.commands.exception.ArgumentParseException;
import dev.demeng.pluginbase.commands.exception.CommandExceptionHandler;
import dev.demeng.pluginbase.commands.exception.DefaultExceptionHandler;
import dev.demeng.pluginbase.commands.exception.InvalidBooleanException;
import dev.demeng.pluginbase.commands.exception.InvalidUrlException;
import dev.demeng.pluginbase.commands.exception.InvalidUuidException;
import dev.demeng.pluginbase.commands.exception.NumberNotInRangeException;
import dev.demeng.pluginbase.commands.exception.ThrowableFromCommand;
import dev.demeng.pluginbase.commands.help.CommandHelp;
import dev.demeng.pluginbase.commands.help.CommandHelpWriter;
import dev.demeng.pluginbase.commands.orphan.OrphanCommand;
import dev.demeng.pluginbase.commands.orphan.OrphanRegistry;
import dev.demeng.pluginbase.commands.orphan.Orphans;
import dev.demeng.pluginbase.commands.process.CommandCondition;
import dev.demeng.pluginbase.commands.process.ContextResolver;
import dev.demeng.pluginbase.commands.process.ContextResolverFactory;
import dev.demeng.pluginbase.commands.process.ParameterResolver;
import dev.demeng.pluginbase.commands.process.ParameterResolver.ParameterResolverContext;
import dev.demeng.pluginbase.commands.process.ParameterValidator;
import dev.demeng.pluginbase.commands.process.PermissionReader;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import dev.demeng.pluginbase.commands.process.SenderResolver;
import dev.demeng.pluginbase.commands.process.ValueResolver;
import dev.demeng.pluginbase.commands.process.ValueResolver.ValueResolverContext;
import dev.demeng.pluginbase.commands.process.ValueResolverFactory;
import dev.demeng.pluginbase.commands.util.ClassMap;
import dev.demeng.pluginbase.commands.util.Primitives;
import dev.demeng.pluginbase.commands.util.StackTraceSanitizer;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public abstract class BaseCommandHandler implements CommandHandler {

  protected final Map<CommandPath, CommandExecutable> executables = new HashMap<>();
  protected final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();
  private final BaseCommandDispatcher dispatcher = new BaseCommandDispatcher(this);

  final LinkedList<ResolverFactory> factories = new LinkedList<>();
  final BaseAutoCompleter autoCompleter = new BaseAutoCompleter(this);
  final ClassMap<List<ParameterValidator<Object>>> validators = new ClassMap<>();
  final ClassMap<ResponseHandler<?>> responseHandlers = new ClassMap<>();
  final ClassMap<Supplier<?>> dependencies = new ClassMap<>();
  final List<SenderResolver> senderResolvers = new ArrayList<>();
  private final Set<PermissionReader> permissionReaders = new HashSet<>();
  final Map<Class<?>, Set<AnnotationReplacer<?>>> annotationReplacers = new ClassMap<>();
  private MethodCallerFactory methodCallerFactory = MethodCallerFactory.defaultFactory();
  private ArgumentParser argumentParser = ArgumentParser.QUOTES;
  private final WrappedExceptionHandler exceptionHandler = new WrappedExceptionHandler(
      DefaultExceptionHandler.INSTANCE);
  private StackTraceSanitizer sanitizer = StackTraceSanitizer.defaultSanitizer();
  String flagPrefix = "-";
  String switchPrefix = "-";
  CommandHelpWriter<?> helpWriter;
  boolean failOnExtra = false;
  final List<CommandCondition> conditions = new ArrayList<>();

  public BaseCommandHandler() {
    registerContextResolverFactory(new SenderContextResolverFactory(senderResolvers));
    registerContextResolverFactory(DependencyResolverFactory.INSTANCE);
    registerValueResolver(int.class, ValueResolverContext::popInt);
    registerValueResolver(double.class, ValueResolverContext::popDouble);
    registerValueResolver(short.class, ValueResolverContext::popShort);
    registerValueResolver(byte.class, ValueResolverContext::popByte);
    registerValueResolver(long.class, ValueResolverContext::popLong);
    registerValueResolver(float.class, ValueResolverContext::popFloat);
    registerValueResolver(boolean.class, bool());
    registerValueResolver(String.class, ValueResolverContext::popForParameter);
    registerValueResolver(UUID.class, context -> {
      final String value = context.pop();
      try {
        return UUID.fromString(value);
      } catch (final Throwable t) {
        throw new InvalidUuidException(context.parameter(), value);
      }
    });
    registerValueResolver(URL.class, context -> {
      final String value = context.pop();
      try {
        return new URL(value);
      } catch (final MalformedURLException e) {
        throw new InvalidUrlException(context.parameter(), value);
      }
    });
    registerValueResolver(URI.class, context -> {
      final String value = context.pop();
      try {
        return new URI(value);
      } catch (final URISyntaxException e) {
        throw new InvalidUrlException(context.parameter(), value);
      }
    });
    registerContextResolver(CommandHandler.class, context -> this);
    registerContextResolver(ExecutableCommand.class,
        context -> context.parameter().getDeclaringCommand());
    registerContextResolver(CommandActor.class, ParameterResolverContext::actor);
    registerContextResolver((Class) CommandHelp.class, new BaseCommandHelp.Resolver(this));
    setExceptionHandler(DefaultExceptionHandler.INSTANCE);
    registerCondition(CooldownCondition.INSTANCE);
    registerParameterValidator(Number.class, (value, parameter, actor) -> {
      final Range range = parameter.getAnnotation(Range.class);
      if (range != null) {
        if (value.doubleValue() > range.max() || value.doubleValue() < range.min()) {
          throw new NumberNotInRangeException(actor, parameter, value, range.min(),
              range.max());
        }
      }
    });
    registerCondition((actor, command, arguments) -> command.checkPermission(actor));
  }

  @Override
  public @NotNull CommandHandler register(@NotNull final Object... commands) {
    for (final Object command : commands) {
      notNull(command, "Command");
      if (command instanceof OrphanCommand) {
        throw new IllegalArgumentException("You cannot register an OrphanCommand directly! " +
            "You must wrap it using Orphans.path(...).handler(OrphanCommand)");
      }
      if (command instanceof Orphans) {
        throw new IllegalArgumentException(
            "You forgot to call .handler(OrphanCommand) in your Orphans.path(...)!");
      }
      if (command instanceof OrphanRegistry) {
        setDependencies(((OrphanRegistry) command).getHandler());
        CommandParser.parse(this, ((OrphanRegistry) command));
      } else {
        setDependencies(command);
        CommandParser.parse(this, command);
      }
    }
    for (final BaseCommandCategory category : categories.values()) {
      final CommandPath categoryPath = category.getPath().getCategoryPath();
      category.parent(categoryPath == null ? null : categories.get(categoryPath));
      findPermission(category.defaultAction);
    }
    for (final CommandExecutable executable : executables.values()) {
      findPermission(executable);
    }
    return this;
  }

  private void findPermission(@Nullable final CommandExecutable executable) {
    if (executable == null) {
      return;
    }
    if (!executable.permissionSet) {
      for (final PermissionReader reader : permissionReaders) {
        final CommandPermission p = reader.getPermission(executable);
        if (p != null) {
          executable.permissionSet = true;
          executable.setPermission(p);
          return;
        }
      }
    }
  }

  public Set<PermissionReader> getPermissionReaders() {
    return permissionReaders;
  }

  @Override
  public @NotNull CommandHandler setMethodCallerFactory(
      @NotNull final MethodCallerFactory factory) {
    notNull(factory, "method caller factory");
    methodCallerFactory = factory;
    return this;
  }

  @Override
  public @NotNull ArgumentParser getArgumentParser() {
    return argumentParser;
  }

  @Override
  public ArgumentStack parseArgumentsForCompletion(final String... arguments)
      throws ArgumentParseException {
    final String args = String.join(" ", arguments);
    if (args.isEmpty()) {
      return ArgumentStack.copy(EMPTY_TEXT);
    }
    return argumentParser.parse(args);
  }

  @Override
  public ArgumentStack parseArguments(final String... arguments) throws ArgumentParseException {
    final String args = String.join(" ", arguments);
    if (args.isEmpty()) {
      return ArgumentStack.empty();
    }
    return argumentParser.parse(args);
  }

  /**
   * A collection that is returned for empty auto-completions.
   */
  private static final Collection<String> EMPTY_TEXT = Collections.singletonList("");

  @Override
  public BaseCommandHandler setArgumentParser(@NotNull final ArgumentParser argumentParser) {
    notNull(argumentParser, "argument parser");
    this.argumentParser = argumentParser;
    return this;
  }

  @Override
  public @NotNull CommandHandler setExceptionHandler(
      @NotNull final CommandExceptionHandler handler) {
    notNull(handler, "command exception handler");
    exceptionHandler.handler = handler;
    return this;
  }

  @Override
  public @NotNull <T extends Throwable> CommandHandler registerExceptionHandler(
      @NotNull final Class<T> exceptionType,
      @NotNull final BiConsumer<CommandActor, T> handler) {
    notNull(exceptionType, "exception type");
    notNull(handler, "exception handler");
    exceptionHandler.exceptionsHandlers.add(exceptionType,
        (BiConsumer<CommandActor, Throwable>) handler);
    return this;
  }

  @Override
  public @NotNull CommandHandler setSwitchPrefix(@NotNull final String prefix) {
    notNull(prefix, "prefix");
    notEmpty(prefix, "prefix cannot be empty!");
    switchPrefix = prefix;
    return this;
  }

  @Override
  public @NotNull CommandHandler setFlagPrefix(@NotNull final String prefix) {
    notNull(prefix, "prefix");
    notEmpty(prefix, "prefix cannot be empty!");
    flagPrefix = prefix;
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler setHelpWriter(@NotNull final CommandHelpWriter<T> helpWriter) {
    notNull(helpWriter, "command help writer");
    this.helpWriter = helpWriter;
    return this;
  }

  @Override
  public @NotNull CommandHandler disableStackTraceSanitizing() {
    sanitizer = StackTraceSanitizer.empty();
    return this;
  }

  @Override
  public @NotNull CommandHandler failOnTooManyArguments() {
    failOnExtra = true;
    return this;
  }

  @Override
  public @NotNull CommandHandler registerSenderResolver(@NotNull final SenderResolver resolver) {
    notNull(resolver, "resolver");
    senderResolvers.add(resolver);
    return this;
  }

  @Override
  public @NotNull CommandHandler registerPermissionReader(@NotNull final PermissionReader reader) {
    notNull(reader, "permission reader");
    permissionReaders.add(reader);
    return this;
  }

  @Override
  public <T> @NotNull CommandHandler registerValueResolver(@NotNull final Class<T> type,
      @NotNull final ValueResolver<T> resolver) {
    notNull(type, "type");
    notNull(resolver, "resolver");
    if (type.isPrimitive()) {
      registerValueResolver(Primitives.wrap(type), resolver);
    }
    factories.add(new ResolverFactory(ValueResolverFactory.forType(type, resolver)));
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler registerValueResolver(final int priority,
      @NotNull final Class<T> type,
      @NotNull final ValueResolver<T> resolver) {
    notNull(type, "type");
    notNull(resolver, "resolver");
    if (type.isPrimitive()) {
      registerValueResolver(priority, Primitives.wrap(type), resolver);
    }
    factories.add(coerceIn(priority, 0, factories.size()),
        new ResolverFactory(ValueResolverFactory.forType(type, resolver)));
    return this;
  }

  @Override
  public <T> @NotNull CommandHandler registerContextResolver(@NotNull final Class<T> type,
      @NotNull final ContextResolver<T> resolver) {
    notNull(type, "type");
    notNull(resolver, "resolver");
    if (type.isPrimitive()) {
      registerContextResolver(Primitives.wrap(type), resolver);
    }
    factories.add(new ResolverFactory(ContextResolverFactory.forType(type, resolver)));
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler registerContextResolver(final int priority,
      @NotNull final Class<T> type,
      @NotNull final ContextResolver<T> resolver) {
    notNull(type, "type");
    notNull(resolver, "resolver");
    if (type.isPrimitive()) {
      registerContextResolver(Primitives.wrap(type), resolver);
    }
    factories.add(coerceIn(priority, 0, factories.size()),
        new ResolverFactory(ContextResolverFactory.forType(type, resolver)));
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler registerContextValue(@NotNull final Class<T> type,
      final T value) {
    return registerContextResolver(type, ContextResolver.of(value));
  }

  @Override
  public @NotNull <T> CommandHandler registerContextValue(final int priority,
      @NotNull final Class<T> type,
      @NotNull final T value) {
    return registerContextResolver(priority, type, ContextResolver.of(value));
  }

  @Override
  public @NotNull CommandHandler registerValueResolverFactory(
      @NotNull final ValueResolverFactory factory) {
    notNull(factory, "value resolver factory");
    factories.add(new ResolverFactory(factory));
    return this;
  }

  @Override
  public @NotNull CommandHandler registerValueResolverFactory(final int priority,
      @NotNull final ValueResolverFactory factory) {
    notNull(factory, "value resolver factory");
    factories.add(coerceIn(priority, 0, factories.size()), new ResolverFactory(factory));
    return this;
  }

  @Override
  public @NotNull CommandHandler registerContextResolverFactory(
      @NotNull final ContextResolverFactory factory) {
    notNull(factory, "context resolver factory");
    factories.add(new ResolverFactory(factory));
    return this;
  }

  @Override
  public @NotNull CommandHandler registerContextResolverFactory(final int priority,
      @NotNull final ContextResolverFactory factory) {
    notNull(factory, "context resolver factory");
    factories.add(coerceIn(priority, 0, factories.size()), new ResolverFactory(factory));
    return this;
  }

  @Override
  public @NotNull CommandHandler registerCondition(@NotNull final CommandCondition condition) {
    notNull(condition, "condition");
    conditions.add(condition);
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler registerDependency(@NotNull final Class<T> type,
      @NotNull final Supplier<T> supplier) {
    notNull(type, "type");
    notNull(supplier, "supplier");
    dependencies.add(type, supplier);
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler registerDependency(@NotNull final Class<T> type,
      final T value) {
    notNull(type, "type");
    dependencies.add(type, () -> value);
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler registerParameterValidator(@NotNull final Class<T> type,
      @NotNull final ParameterValidator<T> validator) {
    notNull(type, "type");
    notNull(validator, "validator");
    validators.computeIfAbsent(Primitives.wrap(type), t -> new ArrayList<>())
        .add((ParameterValidator<Object>) validator);
    return this;
  }

  @Override
  public @NotNull <T> CommandHandler registerResponseHandler(@NotNull final Class<T> responseType,
      @NotNull final ResponseHandler<T> handler) {
    notNull(responseType, "response type");
    notNull(handler, "response handler");
    responseHandlers.add(responseType, handler);
    return this;
  }

  @Override
  public @NotNull <T extends Annotation> CommandHandler registerAnnotationReplacer(
      @NotNull final Class<T> annotationType, @NotNull final AnnotationReplacer<T> replacer) {
    notNull(annotationType, "annotation type");
    notNull(replacer, "annotation replacer");
    annotationReplacers.computeIfAbsent(annotationType, e -> new HashSet<>()).add(replacer);
    return this;
  }

  @Override
  public @NotNull CommandHandler accept(@NotNull final CommandHandlerVisitor visitor) {
    notNull(visitor, "command handler visitor cannot be null!");
    visitor.visit(this);
    return this;
  }

  @SuppressWarnings("rawtypes")
  public @Nullable <T extends Annotation> List<Annotation> replaceAnnotation(
      final AnnotatedElement element, final T ann) {
    final Set<AnnotationReplacer<?>> replacers = annotationReplacers.get(ann.annotationType());
    if (replacers == null || replacers.isEmpty()) {
      return null;
    }
    final List<Annotation> annotations = new ArrayList<>();
    for (final AnnotationReplacer replacer : replacers) {
      final Collection<Annotation> replaced = replacer.replaceAnnotations(element, ann);
      if (replaced == null || replaced.isEmpty()) {
        continue;
      }
      annotations.addAll(replaced);
    }
    if (annotations.isEmpty()) {
      return null;
    }
    return annotations;
  }

  @Override
  public @NotNull AutoCompleter getAutoCompleter() {
    return autoCompleter;
  }

  @Override
  public ExecutableCommand getCommand(@NotNull final CommandPath path) {
    return executables.get(path);
  }

  @Override
  public CommandCategory getCategory(@NotNull final CommandPath path) {
    return categories.get(path);
  }

  @Override
  public @UnmodifiableView @NotNull Map<CommandPath, ExecutableCommand> getCommands() {
    return Collections.unmodifiableMap(executables);
  }

  @Override
  public @UnmodifiableView @NotNull Map<CommandPath, CommandCategory> getCategories() {
    return Collections.unmodifiableMap(categories);
  }

  public <T> ParameterResolver<T> getResolver(final CommandParameter parameter) {
    final ParameterResolver<T> cached = null;

    for (final ResolverFactory factory : factories) {
      final Resolver resolver = factory.create(parameter);
      if (resolver == null) {
        continue;
      }
      return (ParameterResolver<T>) resolver;
    }
    if (parameter.getType().isEnum()) {
      return (ParameterResolver<T>) new Resolver(null,
          EnumResolverFactory.INSTANCE.create(parameter));
    }
    return null;
  }

  @Override
  public @NotNull CommandExceptionHandler getExceptionHandler() {
    return exceptionHandler;
  }

  @Override
  public @NotNull MethodCallerFactory getMethodCallerFactory() {
    return methodCallerFactory;
  }

  @Override
  public <T> CommandHelpWriter<T> getHelpWriter() {
    return (CommandHelpWriter<T>) helpWriter;
  }

  private void unregister(final CommandPath path, final CommandExecutable command) {
    final BaseCommandCategory parent = command.parent;
    if (parent != null) {
      parent.commands.remove(path);
      if (parent.isEmpty()) {
        categories.remove(parent.path);
      }
    }
  }

  private void unregister(final CommandPath path, final BaseCommandCategory category) {
    final BaseCommandCategory parent = category.parent;
    if (parent != null) {
      parent.commands.remove(path);
      if (parent.isEmpty()) {
        categories.remove(parent.path);
      }
    }
  }

  @Override
  public boolean unregister(@NotNull final CommandPath path) {
    boolean modified = false;
    for (final Iterator<Entry<CommandPath, CommandExecutable>> iterator = executables.entrySet()
        .iterator(); iterator.hasNext(); ) {
      final Entry<CommandPath, CommandExecutable> entry = iterator.next();
      if (entry.getKey().isChildOf(path)) {
        modified = true;
        iterator.remove();
        unregister(path, entry.getValue());
      }
    }
    for (final Iterator<Entry<CommandPath, BaseCommandCategory>> iterator = categories.entrySet()
        .iterator(); iterator.hasNext(); ) {
      final Entry<CommandPath, BaseCommandCategory> entry = iterator.next();
      if (entry.getKey().isChildOf(path)) {
        modified = true;
        iterator.remove();
        unregister(path, entry.getValue());
      }
    }
    return modified;
  }

  @Override
  public boolean unregister(@NotNull final String commandPath) {
    return unregister(CommandPath.get(splitBySpace(commandPath)));
  }

  @Override
  public void unregisterAllCommands() {
    // it's important that we don't just do a blind executables.clear()
    // or categories.clear(), since some platforms register commands
    // in their own way (such as Bukkit).
    getRootPaths().forEach(this::unregister);
  }

  @Override
  public @NotNull Set<CommandPath> getRootPaths() {
    final Set<CommandPath> paths = new HashSet<>();
    for (final CommandPath path : categories.keySet()) {
      if (path.isRoot()) {
        paths.add(path);
      }
    }
    for (final CommandPath path : executables.keySet()) {
      if (path.isRoot()) {
        paths.add(path);
      }
    }
    return paths;
  }

  @Override
  public @NotNull String getSwitchPrefix() {
    return switchPrefix;
  }

  @Override
  public @NotNull String getFlagPrefix() {
    return flagPrefix;
  }

  @Override
  public <T> @NotNull Optional<@Nullable T> dispatch(@NotNull final CommandActor actor,
      @NotNull final ArgumentStack arguments) {
    return (Optional<T>) Optional.ofNullable(dispatcher.eval(actor, arguments));
  }

  @Override
  public <T> @NotNull Optional<@Nullable T> dispatch(@NotNull final CommandActor actor,
      @NotNull final String commandInput) {
    try {
      return dispatch(actor, parseArguments(commandInput));
    } catch (final Throwable t) {
      getExceptionHandler().handleException(t, actor);
      return Optional.empty();
    }
  }

  @Override
  public <T> Supplier<T> getDependency(@NotNull final Class<T> dependencyType) {
    return (Supplier<T>) dependencies.getFlexible(dependencyType);
  }

  @Override
  public <T> Supplier<T> getDependency(@NotNull final Class<T> dependencyType,
      final Supplier<T> def) {
    return (Supplier<T>) dependencies.getFlexibleOrDefault(dependencyType, def);
  }

  protected void setDependencies(final Object ob) {
    for (final Field field : getType(ob).getDeclaredFields()) {
      if (!field.isAnnotationPresent(Dependency.class)) {
        continue;
      }
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      final Supplier<?> dependency = dependencies.getFlexible(field.getType());
      if (dependency == null) {
        throw new IllegalStateException(
            "Unable to find correct dependency for type " + field.getType());
      }
      try {
        field.set(ob, dependency.get());
      } catch (final IllegalAccessException e) {
        throw new IllegalStateException(
            "Unable to inject dependency value into field " + field.getName(), e);
      }
    }
  }

  private ValueResolver<Boolean> bool() {
    return context -> {
      final String v = context.pop();
      switch (v.toLowerCase()) {
        case "true":
        case "yes":
        case "ye":
        case "y":
        case "yeah":
        case "ofcourse":
        case "mhm":
          return true;
        case "false":
        case "no":
        case "n":
          return false;
        default:
          throw new InvalidBooleanException(context.parameter(), v);
      }
    };
  }

  private class WrappedExceptionHandler implements CommandExceptionHandler {

    private final ClassMap<BiConsumer<CommandActor, Throwable>> exceptionsHandlers = new ClassMap<>();
    private @NotNull CommandExceptionHandler handler;

    public WrappedExceptionHandler(@NotNull final CommandExceptionHandler handler) {
      this.handler = handler;
    }

    @Override
    public void handleException(@NotNull Throwable throwable, @NotNull final CommandActor actor) {
      final Throwable cause = throwable.getCause();
      if (cause != null && (cause.getClass().isAnnotationPresent(ThrowableFromCommand.class) ||
          exceptionsHandlers.getFlexible(cause.getClass()) != null)
      ) {
        throwable = cause;
      }
      @Nullable final BiConsumer<CommandActor, Throwable> registered = exceptionsHandlers.getFlexible(
          throwable.getClass());
      sanitizer.sanitize(throwable);
      if (registered != null) {
        registered.accept(actor, throwable);
        return;
      }
      handler.handleException(throwable, actor);
    }
  }

}
