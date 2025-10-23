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

package dev.demeng.pluginbase.event.functional.single;

import dev.demeng.pluginbase.delegate.Delegates;
import dev.demeng.pluginbase.event.SingleSubscription;
import dev.demeng.pluginbase.event.functional.FunctionalHandlerList;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public interface SingleHandlerList<T extends Event>
    extends FunctionalHandlerList<T, SingleSubscription<T>> {

  @NotNull
  @Override
  default SingleHandlerList<T> consumer(@NotNull final Consumer<? super T> handler) {
    Objects.requireNonNull(handler, "handler");
    return biConsumer(Delegates.consumerToBiConsumerSecond(handler));
  }

  @NotNull
  @Override
  SingleHandlerList<T> biConsumer(@NotNull BiConsumer<SingleSubscription<T>, ? super T> handler);
}
