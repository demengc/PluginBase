/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

package dev.demeng.pluginbase.event.functional.merged;

import dev.demeng.pluginbase.event.MergedSubscription;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

class MergedHandlerListImpl<T> implements MergedHandlerList<T> {

  private final MergedSubscriptionBuilderImpl<T> builder;
  private final List<BiConsumer<MergedSubscription<T>, ? super T>> handlers = new ArrayList<>(1);

  MergedHandlerListImpl(@NotNull final MergedSubscriptionBuilderImpl<T> builder) {
    this.builder = builder;
  }

  @NotNull
  @Override
  public MergedHandlerList<T> biConsumer(
      @NotNull final BiConsumer<MergedSubscription<T>, ? super T> handler) {
    Objects.requireNonNull(handler, "handler");
    this.handlers.add(handler);
    return this;
  }

  @NotNull
  @Override
  public MergedSubscription<T> register() {
    if (this.handlers.isEmpty()) {
      throw new IllegalStateException("No handlers have been registered");
    }

    final BaseMergedEventListener<T> listener = new BaseMergedEventListener<>(this.builder,
        this.handlers);
    listener.register(BaseManager.getPlugin());
    return listener;
  }
}
