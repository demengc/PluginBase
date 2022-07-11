/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

import com.google.common.reflect.TypeToken;
import dev.demeng.pluginbase.event.MergedSubscription;
import dev.demeng.pluginbase.event.functional.ExpiryTestStage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

class MergedSubscriptionBuilderImpl<T> implements MergedSubscriptionBuilder<T> {

  final TypeToken<T> handledClass;
  final Map<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> mappings = new HashMap<>();

  BiConsumer<? super Event, Throwable> exceptionConsumer = DEFAULT_EXCEPTION_CONSUMER;

  final List<Predicate<T>> filters = new ArrayList<>();
  final List<BiPredicate<MergedSubscription<T>, T>> preExpiryTests = new ArrayList<>(0);
  final List<BiPredicate<MergedSubscription<T>, T>> midExpiryTests = new ArrayList<>(0);
  final List<BiPredicate<MergedSubscription<T>, T>> postExpiryTests = new ArrayList<>(0);

  MergedSubscriptionBuilderImpl(final TypeToken<T> handledClass) {
    this.handledClass = handledClass;
  }

  @NotNull
  @Override
  public <E extends Event> MergedSubscriptionBuilder<T> bindEvent(
      @NotNull final Class<E> eventClass,
      @NotNull final Function<E, T> function) {
    return bindEvent(eventClass, EventPriority.NORMAL, function);
  }

  @NotNull
  @Override
  public <E extends Event> MergedSubscriptionBuilder<T> bindEvent(
      @NotNull final Class<E> eventClass,
      @NotNull final EventPriority priority, @NotNull final Function<E, T> function) {
    Objects.requireNonNull(eventClass, "eventClass");
    Objects.requireNonNull(priority, "priority");
    Objects.requireNonNull(function, "function");

    this.mappings.put(eventClass, new MergedHandlerMapping<>(priority, function));
    return this;
  }

  @NotNull
  @Override
  public MergedSubscriptionBuilder<T> expireIf(
      @NotNull final BiPredicate<MergedSubscription<T>, T> predicate,
      @NotNull final ExpiryTestStage... testPoints) {
    Objects.requireNonNull(testPoints, "testPoints");
    Objects.requireNonNull(predicate, "predicate");
    for (final ExpiryTestStage testPoint : testPoints) {
      switch (testPoint) {
        case PRE:
          this.preExpiryTests.add(predicate);
          break;
        case POST_FILTER:
          this.midExpiryTests.add(predicate);
          break;
        case POST_HANDLE:
          this.postExpiryTests.add(predicate);
          break;
        default:
          throw new IllegalArgumentException("Unknown ExpiryTestPoint: " + testPoint);
      }
    }
    return this;
  }

  @NotNull
  @Override
  public MergedSubscriptionBuilder<T> filter(@NotNull final Predicate<T> predicate) {
    Objects.requireNonNull(predicate, "predicate");
    this.filters.add(predicate);
    return this;
  }

  @NotNull
  @Override
  public MergedSubscriptionBuilder<T> exceptionConsumer(
      @NotNull final BiConsumer<Event, Throwable> exceptionConsumer) {
    Objects.requireNonNull(exceptionConsumer, "exceptionConsumer");
    this.exceptionConsumer = exceptionConsumer;
    return this;
  }

  @NotNull
  @Override
  public MergedHandlerList<T> handlers() {
    if (this.mappings.isEmpty()) {
      throw new IllegalStateException("No mappings were created");
    }

    return new MergedHandlerListImpl<>(this);
  }

}
