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

import static dev.demeng.pluginbase.commands.util.Preconditions.notNull;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.annotation.Default;
import dev.demeng.pluginbase.commands.command.CommandCategory;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.CommandPermission;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.core.reflect.MethodCaller.BoundMethodCaller;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

class CommandExecutable implements ExecutableCommand {

  // lazily populated by CommandParses.
  CommandHandler handler;
  boolean permissionSet = false;
  int id;
  CommandPath path;
  String name, usage, description;
  Method method;
  AnnotationReader reader;
  boolean secret;
  BoundMethodCaller methodCaller;
  BaseCommandCategory parent;
  @SuppressWarnings("rawtypes") ResponseHandler responseHandler = CommandParser.VOID_HANDLER;
  private CommandPermission permission = CommandPermission.ALWAYS_TRUE;
  @Unmodifiable List<CommandParameter> parameters;
  @Unmodifiable Map<Integer, CommandParameter> resolveableParameters;

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @Range(from = 0, to = Long.MAX_VALUE) int getId() {
    return id;
  }

  @Override
  public @NotNull String getUsage() {
    return usage;
  }

  @Override
  public @Nullable String getDescription() {
    return description;
  }

  @Override
  public @NotNull CommandPath getPath() {
    return path;
  }

  @Override
  public @Nullable CommandCategory getParent() {
    return parent;
  }

  @Override
  public @NotNull @Unmodifiable List<CommandParameter> getParameters() {
    return parameters;
  }

  @Override
  public @NotNull @Unmodifiable Map<Integer, CommandParameter> getValueParameters() {
    return resolveableParameters;
  }

  @Override
  public @NotNull CommandPermission getPermission() {
    return permission;
  }

  @Override
  public @NotNull CommandHandler getCommandHandler() {
    return handler;
  }

  @Override
  public @NotNull <T> ResponseHandler<T> getResponseHandler() {
    return responseHandler;
  }

  @Override
  public boolean isSecret() {
    return secret;
  }

  @Override
  public <A extends Annotation> A getAnnotation(@NotNull final Class<A> annotation) {
    return reader.get(annotation);
  }

  @Override
  public boolean hasAnnotation(@NotNull final Class<? extends Annotation> annotation) {
    return reader.contains(annotation);
  }

  public void parent(final BaseCommandCategory cat) {
    parent = cat;
    if (hasAnnotation(Default.class) && cat != null) {
      cat.defaultAction = this;
    } else {
      if (cat != null) {
        cat.commands.put(path, this);
      }
    }
  }

  public void setPermission(@NotNull final CommandPermission permission) {
    notNull(permission, "permission");
    this.permission = permission;
  }

  @Override
  public String toString() {
    return "ExecutableCommand{" +
        "path=" + path +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public int compareTo(@NotNull final ExecutableCommand o) {
    return path.compareTo(o.getPath());
  }
}
