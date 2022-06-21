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

import dev.demeng.pluginbase.commands.process.ContextResolver;
import dev.demeng.pluginbase.commands.process.ContextResolver.ContextResolverContext;
import dev.demeng.pluginbase.commands.process.ParameterResolver;
import dev.demeng.pluginbase.commands.process.ValueResolver;
import dev.demeng.pluginbase.commands.process.ValueResolver.ValueResolverContext;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

final class Resolver implements ParameterResolver<Object> {

  private final boolean mutates;

  private final ContextResolver<?> contextResolver;
  private final ValueResolver<?> valueResolver;

  public Resolver(final ContextResolver<?> contextResolver, final ValueResolver<?> valueResolver) {
    this.contextResolver = contextResolver;
    this.valueResolver = valueResolver;
    mutates = valueResolver != null;
  }

  @Override
  public boolean mutatesArguments() {
    return mutates;
  }

  @SneakyThrows
  public Object resolve(@NotNull final ParameterResolverContext context) {
    if (valueResolver != null) {
      return valueResolver.resolve((ValueResolverContext) context);
    }
    return contextResolver.resolve((ContextResolverContext) context);
  }

  public static Resolver wrap(final Object resolver) {
    if (resolver instanceof ValueResolver) {
      return new Resolver(null, (ValueResolver<?>) resolver);
    }
    return new Resolver((ContextResolver<?>) resolver, null);
  }

}
