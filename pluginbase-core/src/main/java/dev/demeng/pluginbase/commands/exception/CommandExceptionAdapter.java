/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
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
package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.core.reflect.MethodCaller.BoundMethodCaller;
import dev.demeng.pluginbase.commands.core.reflect.MethodCallerFactory;
import dev.demeng.pluginbase.commands.util.ClassMap;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link CommandExceptionHandler} that inlines all exceptions into individual,
 * overridable methods. This greatly simplifies the process of handling exceptions.
 * <p>
 * This class loosely uses reflections to find the appropriate handler method. To handle custom
 * exceptions, extend this class and define a method that meets the following criteria:
 * <ol>
 *     <li>Method is public</li>
 *     <li>Method has 2 parameters, one is a CommandActor (or a subclass of it), and the
 *     other is your exception. The name of the method, and the order of parameters does
 *     not matter.</li>
 * </ol>
 * <p>
 * An example:
 * <pre>
 * {@code
 * public void onCustomException(CommandActor actor, CustomException e) {
 *     actor.error("Caught you!");
 * }
 * }
 * </pre>
 * If you have methods that meet the above criteria and want the reflection handler
 * to ignore them, annotate them with {@link Ignore}.
 */
public abstract class CommandExceptionAdapter implements CommandExceptionHandler {

  /**
   * An annotation to automatically ignore any method that may otherwise be a handler method.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Ignore {

  }

  @Ignore
  public void onUnhandledException(@NotNull final CommandActor actor,
      @NotNull final Throwable throwable) {
  }

  public void missingArgument(@NotNull final CommandActor actor,
      @NotNull final MissingArgumentException exception) {
  }

  public void invalidEnumValue(@NotNull final CommandActor actor,
      @NotNull final EnumNotFoundException exception) {
  }

  public void invalidUUID(@NotNull final CommandActor actor,
      @NotNull final InvalidUUIDException exception) {
  }

  public void invalidNumber(@NotNull final CommandActor actor,
      @NotNull final InvalidNumberException exception) {
  }

  public void invalidURL(@NotNull final CommandActor actor,
      @NotNull final InvalidURLException exception) {
  }

  public void invalidBoolean(@NotNull final CommandActor actor,
      @NotNull final InvalidBooleanException exception) {
  }

  public void numberNotInRange(@NotNull final CommandActor actor,
      @NotNull final NumberNotInRangeException exception) {
  }

  public void noPermission(@NotNull final CommandActor actor,
      @NotNull final NoPermissionException exception) {
  }

  public void argumentParse(@NotNull final CommandActor actor,
      @NotNull final ArgumentParseException exception) {
  }

  public void commandInvocation(@NotNull final CommandActor actor,
      @NotNull final CommandInvocationException exception) {
  }

  public void tooManyArguments(@NotNull final CommandActor actor,
      @NotNull final TooManyArgumentsException exception) {
  }

  public void invalidCommand(@NotNull final CommandActor actor,
      @NotNull final InvalidCommandException exception) {
  }

  public void invalidSubcommand(@NotNull final CommandActor actor,
      @NotNull final InvalidSubcommandException exception) {
  }

  public void noSubcommandSpecified(@NotNull final CommandActor actor,
      @NotNull final NoSubcommandSpecifiedException exception) {
  }

  public void cooldown(@NotNull final CommandActor actor,
      @NotNull final CooldownException exception) {
  }

  public void invalidHelpPage(@NotNull final CommandActor actor,
      @NotNull final InvalidHelpPageException exception) {
  }

  public void sendableException(@NotNull final CommandActor actor,
      @NotNull final SendableException exception) {
  }

  private static final List<Method> IGNORED_METHODS = new ArrayList<>();

  static {
    for (final Method method : CommandExceptionAdapter.class.getDeclaredMethods()) {
      if (method.getParameterCount() != 2) {
        continue;
      }
      if (method.isAnnotationPresent(Ignore.class)) {
        IGNORED_METHODS.add(method);
      }
    }
  }

  @Override
  @Ignore
  public void handleException(@NotNull final Throwable throwable,
      @NotNull final CommandActor actor) {
    final MethodExceptionHandler handler = handlers.getFlexibleOrDefault(throwable.getClass(),
        unknownHandler);
    if (handler == unknownHandler && throwable instanceof SelfHandledException) {
      ((SelfHandledException) throwable).handle(actor);
    } else {
      handler.handle(actor, throwable);
    }
  }

  public CommandExceptionAdapter() {
    for (final Method m : getClass().getMethods()) {
      register(m);
    }
  }

  private final ClassMap<MethodExceptionHandler> handlers = new ClassMap<>();
  private final MethodExceptionHandler unknownHandler = this::onUnhandledException;

  @SneakyThrows
  private void register(@NotNull final Method method) {
    if (!CommandExceptionAdapter.class.isAssignableFrom(method.getDeclaringClass())) {
      return;
    }
    if (method.getParameterCount() != 2) {
      return;
    }
    if (method.isAnnotationPresent(Ignore.class)) {
      return;
    }
    for (final Method ignoredMethod : IGNORED_METHODS) {
      if (method.getName().equals(ignoredMethod.getName()) && Arrays.equals(
          method.getParameterTypes(), ignoredMethod.getParameterTypes())) {
        return;
      }
    }
    final Parameter[] parameters = method.getParameters();
    final Class<?> firstType = parameters[0].getType();
    final Class<?> secondType = parameters[1].getType();
    final Class<?> exceptionType;
    final MethodExceptionHandler handler;
    if (CommandActor.class.isAssignableFrom(firstType) && Throwable.class.isAssignableFrom(
        secondType)) {
      exceptionType = secondType;
      final BoundMethodCaller caller = MethodCallerFactory.defaultFactory().createFor(method)
          .bindTo(this);
      handler = caller::call;
    } else if (Throwable.class.isAssignableFrom(firstType) && CommandActor.class.isAssignableFrom(
        secondType)) {
      exceptionType = firstType;
      final BoundMethodCaller caller = MethodCallerFactory.defaultFactory().createFor(method)
          .bindTo(this);
      handler = (actor, throwable) -> caller.call(throwable, actor);
    } else {
      return;
    }
    handlers.add(exceptionType, handler);
  }

  private interface MethodExceptionHandler {

    void handle(@NotNull CommandActor actor, @NotNull Throwable throwable);


  }

}
