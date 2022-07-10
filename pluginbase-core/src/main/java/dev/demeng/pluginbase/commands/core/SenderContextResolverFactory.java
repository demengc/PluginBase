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

import dev.demeng.pluginbase.commands.annotation.NotSender;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ContextResolver;
import dev.demeng.pluginbase.commands.process.ContextResolverFactory;
import dev.demeng.pluginbase.commands.process.SenderResolver;
import dev.demeng.pluginbase.commands.util.Preconditions;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SenderContextResolverFactory implements ContextResolverFactory {

  private static final SenderResolver SELF = new SenderResolver() {

    @Override
    public boolean isCustomType(final Class<?> type) {
      return CommandActor.class.isAssignableFrom(type);
    }

    @Override
    public @NotNull Object getSender(@NotNull final Class<?> customSenderType,
        @NotNull final CommandActor actor,
        @NotNull final ExecutableCommand command) {
      return actor;
    }
  };

  private final List<SenderResolver> resolvers;

  public SenderContextResolverFactory(final List<SenderResolver> resolvers) {
    this.resolvers = resolvers;
    resolvers.add(SELF);
  }

  @Override
  public @Nullable ContextResolver<?> create(@NotNull final CommandParameter parameter) {
    if (parameter.getMethodIndex() != 0) {
      return null;
    }
    if (parameter.isOptional() || parameter.hasAnnotation(NotSender.class)) {
      return null;
    }
    for (final SenderResolver resolver : resolvers) {
      if (resolver.isCustomType(parameter.getType())) {
        return context -> Preconditions.notNull(
            resolver.getSender(parameter.getType(), context.actor(), context.command()),
            "SenderResolver#getSender() must not return null!");
      }
    }
    return null;
  }
}
