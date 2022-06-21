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

package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.exception.CommandInvocationException;
import dev.demeng.pluginbase.commands.exception.InvalidCommandException;
import dev.demeng.pluginbase.commands.exception.InvalidNumberException;
import dev.demeng.pluginbase.commands.exception.MissingArgumentException;
import dev.demeng.pluginbase.commands.exception.NoSubcommandSpecifiedException;
import dev.demeng.pluginbase.commands.exception.TooManyArgumentsException;
import dev.demeng.pluginbase.commands.process.ContextResolver;
import dev.demeng.pluginbase.commands.process.ParameterResolver;
import dev.demeng.pluginbase.commands.process.ParameterResolver.ParameterResolverContext;
import dev.demeng.pluginbase.commands.process.ParameterValidator;
import dev.demeng.pluginbase.commands.process.ValueResolver.ValueResolverContext;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class BaseCommandDispatcher {

  private final BaseCommandHandler handler;

  public BaseCommandDispatcher(final BaseCommandHandler handler) {
    this.handler = handler;
  }

  public Object eval(@NotNull final CommandActor actor, @NotNull final ArgumentStack arguments) {
    try {
      final MutableCommandPath path = MutableCommandPath.empty();
      final String argument = arguments.getFirst();
      path.add(argument);
      final CommandExecutable executable = handler.executables.get(path);
      if (executable != null) {
        arguments.removeFirst();
        return execute(executable, actor, arguments);
      }

      final BaseCommandCategory category = handler.categories.get(path);
      if (category != null) {
        arguments.removeFirst();
        return searchCategory(actor, category, path, arguments);
      } else {
        throw new InvalidCommandException(path, path.getFirst());
      }
    } catch (final Throwable throwable) {
      handler.getExceptionHandler().handleException(throwable, actor);
    }
    return null;
  }

  private Object searchCategory(final CommandActor actor, final BaseCommandCategory category,
      final MutableCommandPath path, final ArgumentStack arguments) {
    if (!arguments.isEmpty()) {
      path.add(arguments.getFirst());
    }
    final CommandExecutable executable = (CommandExecutable) category.commands.get(path);
    if (executable != null) {
      arguments.removeFirst();
      return execute(executable, actor, arguments);
    }
    category.checkPermission(actor);
    final BaseCommandCategory found = (BaseCommandCategory) category.getCategories().get(path);
    if (found == null) {
      if (category.defaultAction == null) {
        throw new NoSubcommandSpecifiedException(category);
      } else {
        return execute(category.defaultAction, actor, arguments);
      }
    } else {
      arguments.removeFirst();
      return searchCategory(actor, found, path, arguments);
    }
  }

  private Object execute(@NotNull final CommandExecutable executable,
      @NotNull final CommandActor actor,
      @NotNull final ArgumentStack args) {
    final List<String> input = args.asImmutableCopy();
    handler.conditions.forEach(
        condition -> condition.test(actor, executable, args.asImmutableView()));
    final Object[] methodArguments = getMethodArguments(executable, actor, args, input);
    if (!args.isEmpty() && handler.failOnExtra) {
      throw new TooManyArgumentsException(executable, args);
    }
    final Object result;
    try {
      result = executable.methodCaller.call(methodArguments);
    } catch (final Throwable throwable) {
      throw new CommandInvocationException(executable, throwable);
    }
    executable.responseHandler.handleResponse(result, actor, executable);
    return result;
  }

  @SneakyThrows
  private Object[] getMethodArguments(final CommandExecutable executable, final CommandActor actor,
      final ArgumentStack args, final List<String> input) {
    final Object[] values = new Object[executable.parameters.size()];
    for (final CommandParameter parameter : executable.parameters) {
      if (ArgumentStack.class.isAssignableFrom(parameter.getType())) {
        values[parameter.getMethodIndex()] = args;
      } else if (parameter.isSwitch()) {
        handleSwitch(args, values, parameter);
      } else if (parameter.isFlag()) {
        handleFlag(input, actor, args, values, parameter);
      }
    }
    for (final CommandParameter parameter : executable.parameters) {
      if (ArgumentStack.class.isAssignableFrom(parameter.getType())) {
        values[parameter.getMethodIndex()] = args;
        continue;
      }
      if (!parameter.isSwitch() && !parameter.isFlag()) {
        final ParameterResolver<?> resolver = parameter.getResolver();
        if (!resolver.mutatesArguments()) {
          parameter.checkPermission(actor);
          final ContextResolverContext cxt = new ContextResolverContext(input, actor, parameter,
              values);
          final Object value = resolver.resolve(cxt);
          for (final ParameterValidator<Object> v : parameter.getValidators()) {
            v.validate(value, parameter, actor);
          }
          values[parameter.getMethodIndex()] = value;
        } else {
          if (!addDefaultValues(args, parameter, actor, values)) {
            parameter.checkPermission(actor);
            final ValueContextR cxt = new ValueContextR(input, actor, parameter, values, args);
            final Object value = resolver.resolve(cxt);
            for (final ParameterValidator<Object> v : parameter.getValidators()) {
              v.validate(value, parameter, actor);
            }
            values[parameter.getMethodIndex()] = value;
          }
        }
      }
    }
    return values;
  }

  private boolean addDefaultValues(final ArgumentStack args,
      final CommandParameter parameter,
      final CommandActor actor,
      final Object[] values) {
    if (args.isEmpty()) {
      if (parameter.getDefaultValue().isEmpty() && parameter.isOptional()) {
        values[parameter.getMethodIndex()] = null;
        return true;
      } else {
        if (!parameter.getDefaultValue().isEmpty()) {
          args.addAll(parameter.getDefaultValue());
          return false;
        } else {
          throw new MissingArgumentException(parameter);
        }
      }
    }
    return false;
  }

  private void handleSwitch(final ArgumentStack args, final Object[] values,
      final CommandParameter parameter) {
    final boolean provided = args.remove(handler.switchPrefix + parameter.getSwitchName());
    if (!provided) {
      values[parameter.getMethodIndex()] = parameter.getDefaultSwitch();
    } else {
      values[parameter.getMethodIndex()] = true;
    }
  }

  @SneakyThrows
  private void handleFlag(final List<String> input, final CommandActor actor,
      final ArgumentStack args,
      final Object[] values, final CommandParameter parameter) {
    final String lookup = handler.getFlagPrefix() + parameter.getFlagName();
    int index = args.indexOf(lookup);
    final ArgumentStack flagArguments;
    if (index == -1) { // flag isn't specified, use default value or throw an MPE.
      if (parameter.isOptional()) {
        if (!parameter.getDefaultValue().isEmpty()) {
          args.add(lookup);
          args.addAll(parameter.getDefaultValue());
          index = args.indexOf(lookup);
          args.remove(index); // remove the flag prefix + flag name
          flagArguments = handler.parseArguments(
              args.remove(index)); // put the actual value in a separate argument stack
        } else {
          for (final ParameterValidator<Object> v : parameter.getValidators()) {
            v.validate(null, parameter, actor);
          }
          values[parameter.getMethodIndex()] = null;
          return;
        }
      } else {
        throw new MissingArgumentException(parameter);
      }
    } else {
      args.remove(index); // remove the flag prefix + flag name
      if (index >= args.size()) {
        throw new MissingArgumentException(parameter);
      }
      flagArguments = handler.parseArguments(
          args.remove(index)); // put the actual value in a separate argument stack
    }
    final ValueContextR contextR = new ValueContextR(input, actor, parameter, values,
        flagArguments);
    final Object value = parameter.getResolver().resolve(contextR);
    for (final ParameterValidator<Object> v : parameter.getValidators()) {
      v.validate(value, parameter, actor);
    }
    values[parameter.getMethodIndex()] = value;

  }

  @AllArgsConstructor
  private static abstract class ParamResolverContext implements ParameterResolverContext {

    private final List<String> input;
    private final CommandActor actor;
    private final CommandParameter parameter;
    private final Object[] resolved;

    @Override
    public @NotNull @Unmodifiable List<String> input() {
      return input;
    }

    @Override
    public <A extends CommandActor> @NotNull A actor() {
      return (A) actor;
    }

    @Override
    public @NotNull CommandParameter parameter() {
      return parameter;
    }

    @Override
    public @NotNull ExecutableCommand command() {
      return parameter.getDeclaringCommand();
    }

    @Override
    public @NotNull CommandHandler commandHandler() {
      return parameter.getCommandHandler();
    }

    @Override
    public <T> @NotNull T getResolvedParameter(@NotNull final CommandParameter parameter) {
      try {
        return (T) resolved[parameter.getMethodIndex()];
      } catch (final Throwable throwable) {
        throw new IllegalArgumentException("This parameter has not been resolved yet!");
      }
    }

    @Override
    public <T> @NotNull T getResolvedArgument(@NotNull final Class<T> type) {
      for (final Object o : resolved) {
        if (type.isInstance(o)) {
          return (T) o;
        }
      }
      throw new IllegalArgumentException("This parameter has not been resolved yet!");
    }
  }

  private static final class ContextResolverContext extends ParamResolverContext implements
      ContextResolver.ContextResolverContext {

    public ContextResolverContext(final List<String> input, final CommandActor actor,
        final CommandParameter parameter, final Object[] resolved) {
      super(input, actor, parameter, resolved);
    }
  }

  private static final class ValueContextR extends ParamResolverContext implements
      ValueResolverContext {

    private final ArgumentStack argumentStack;

    public ValueContextR(final List<String> input,
        final CommandActor actor,
        final CommandParameter parameter,
        final Object[] resolved,
        final ArgumentStack argumentStack) {
      super(input, actor, parameter, resolved);
      this.argumentStack = argumentStack;
    }

    @Override
    public ArgumentStack arguments() {
      return argumentStack;
    }

    @Override
    public String popForParameter() {
      return argumentStack.popForParameter(parameter());
    }

    @Override
    public String pop() {
      return argumentStack.pop();
    }

    private <T> T num(final Function<String, T> f) {
      final String input = pop();
      try {
        if (input.startsWith("0x")) {
          return (T) Integer.valueOf(input.substring(2), 16);
        }
        return f.apply(input);
      } catch (final NumberFormatException e) {
        throw new InvalidNumberException(parameter(), input);
      }
    }

    @Override
    public int popInt() {
      return num(Integer::parseInt);
    }

    @Override
    public double popDouble() {
      return num(Double::parseDouble);
    }

    @Override
    public byte popByte() {
      return num(Byte::parseByte);
    }

    @Override
    public short popShort() {
      return num(Short::parseShort);
    }

    @Override
    public float popFloat() {
      return num(Float::parseFloat);
    }

    @Override
    public long popLong() {
      return num(Long::parseLong);
    }
  }
}
