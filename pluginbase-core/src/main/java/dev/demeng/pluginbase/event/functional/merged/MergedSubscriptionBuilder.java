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

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import dev.demeng.pluginbase.delegate.Delegates;
import dev.demeng.pluginbase.event.MergedSubscription;
import dev.demeng.pluginbase.event.functional.ExpiryTestStage;
import dev.demeng.pluginbase.event.functional.SubscriptionBuilder;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

/**
 * Functional builder for {@link MergedSubscription}s.
 *
 * @param <T> the handled type
 */
public interface MergedSubscriptionBuilder<T> extends SubscriptionBuilder<T> {

  /**
   * Makes a MergedHandlerBuilder for a given super type
   *
   * @param handledClass the super type of the event handler
   * @param <T>          the super type class
   * @return a {@link MergedSubscriptionBuilder} to construct the event handler
   */
  @NotNull
  static <T> MergedSubscriptionBuilder<T> newBuilder(@NotNull final Class<T> handledClass) {
    Objects.requireNonNull(handledClass, "handledClass");
    return new MergedSubscriptionBuilderImpl<>(TypeToken.of(handledClass));
  }

  /**
   * Makes a MergedHandlerBuilder for a given super type
   *
   * @param type the super type of the event handler
   * @param <T>  the super type class
   * @return a {@link MergedSubscriptionBuilder} to construct the event handler
   */
  @NotNull
  static <T> MergedSubscriptionBuilder<T> newBuilder(@NotNull final TypeToken<T> type) {
    Objects.requireNonNull(type, "type");
    return new MergedSubscriptionBuilderImpl<>(type);
  }

  /**
   * Makes a MergedHandlerBuilder for a super event class
   *
   * @param superClass   the abstract super event class
   * @param eventClasses the event classes to be bound to
   * @param <S>          the super class type
   * @return a {@link MergedSubscriptionBuilder} to construct the event handler
   */
  @NotNull
  @SafeVarargs
  static <S extends Event> MergedSubscriptionBuilder<S> newBuilder(
      @NotNull final Class<S> superClass,
      @NotNull final Class<? extends S>... eventClasses) {
    return newBuilder(superClass, EventPriority.NORMAL, eventClasses);
  }

  /**
   * Makes a MergedHandlerBuilder for a super event class
   *
   * @param superClass   the abstract super event class
   * @param priority     the priority to listen at
   * @param eventClasses the event classes to be bound to
   * @param <S>          the super class type
   * @return a {@link MergedSubscriptionBuilder} to construct the event handler
   */
  @NotNull
  @SafeVarargs
  static <S extends Event> MergedSubscriptionBuilder<S> newBuilder(
      @NotNull final Class<S> superClass,
      @NotNull final EventPriority priority, @NotNull final Class<? extends S>... eventClasses) {
    Objects.requireNonNull(superClass, "superClass");
    Objects.requireNonNull(eventClasses, "eventClasses");
    Objects.requireNonNull(priority, "priority");
    if (eventClasses.length < 2) {
      throw new IllegalArgumentException("merge method used for only one subclass");
    }

    final MergedSubscriptionBuilderImpl<S> h = new MergedSubscriptionBuilderImpl<>(
        TypeToken.of(superClass));
    for (final Class<? extends S> clazz : eventClasses) {
      h.bindEvent(clazz, priority, e -> e);
    }
    return h;
  }

  // override return type - we return MergedSubscriptionBuilder, not SubscriptionBuilder

  @NotNull
  @Override
  default MergedSubscriptionBuilder<T> expireIf(@NotNull final Predicate<T> predicate) {
    return expireIf(Delegates.predicateToBiPredicateSecond(predicate), ExpiryTestStage.PRE,
        ExpiryTestStage.POST_HANDLE);
  }

  @NotNull
  @Override
  default MergedSubscriptionBuilder<T> expireAfter(final long duration,
      @NotNull final TimeUnit unit) {
    Objects.requireNonNull(unit, "unit");
    Preconditions.checkArgument(duration >= 1, "duration < 1");
    final long expiry = Math.addExact(System.currentTimeMillis(), unit.toMillis(duration));
    return expireIf((handler, event) -> System.currentTimeMillis() > expiry, ExpiryTestStage.PRE);
  }

  @NotNull
  @Override
  default MergedSubscriptionBuilder<T> expireAfter(final long maxCalls) {
    Preconditions.checkArgument(maxCalls >= 1, "maxCalls < 1");
    return expireIf((handler, event) -> handler.getCallCounter() >= maxCalls, ExpiryTestStage.PRE,
        ExpiryTestStage.POST_HANDLE);
  }

  @NotNull
  @Override
  MergedSubscriptionBuilder<T> filter(@NotNull Predicate<T> predicate);

  /**
   * Add a expiry predicate.
   *
   * @param predicate  the expiry test
   * @param testPoints when to test the expiry predicate
   * @return ths builder instance
   */
  @NotNull
  MergedSubscriptionBuilder<T> expireIf(@NotNull BiPredicate<MergedSubscription<T>, T> predicate,
      @NotNull ExpiryTestStage... testPoints);

  /**
   * Binds this handler to an event
   *
   * @param eventClass the event class to bind to
   * @param function   the function to remap the event
   * @param <E>        the event class
   * @return the builder instance
   */
  @NotNull <E extends Event> MergedSubscriptionBuilder<T> bindEvent(@NotNull Class<E> eventClass,
      @NotNull Function<E, T> function);

  /**
   * Binds this handler to an event
   *
   * @param eventClass the event class to bind to
   * @param priority   the priority to listen at
   * @param function   the function to remap the event
   * @param <E>        the event class
   * @return the builder instance
   */
  @NotNull <E extends Event> MergedSubscriptionBuilder<T> bindEvent(@NotNull Class<E> eventClass,
      @NotNull EventPriority priority, @NotNull Function<E, T> function);

  /**
   * Sets the exception consumer for the handler.
   *
   * <p> If an exception is thrown in the handler, it is passed to this consumer to be swallowed.
   *
   * @param consumer the consumer
   * @return the builder instance
   * @throws NullPointerException if the consumer is null
   */
  @NotNull
  MergedSubscriptionBuilder<T> exceptionConsumer(@NotNull BiConsumer<Event, Throwable> consumer);

  /**
   * Return the handler list builder to append handlers for the event.
   *
   * @return the handler list
   */
  @NotNull
  MergedHandlerList<T> handlers();

  /**
   * Builds and registers the Handler.
   *
   * @param handler the consumer responsible for handling the event.
   * @return a registered {@link MergedSubscription} instance.
   * @throws NullPointerException  if the handler is null
   * @throws IllegalStateException if no events have been bound to
   */
  @NotNull
  default MergedSubscription<T> handler(@NotNull final Consumer<? super T> handler) {
    return handlers().consumer(handler).register();
  }

  /**
   * Builds and registers the Handler.
   *
   * @param handler the bi-consumer responsible for handling the event.
   * @return a registered {@link MergedSubscription} instance.
   * @throws NullPointerException  if the handler is null
   * @throws IllegalStateException if no events have been bound to
   */
  @NotNull
  default MergedSubscription<T> biHandler(
      @NotNull final BiConsumer<MergedSubscription<T>, ? super T> handler) {
    return handlers().biConsumer(handler).register();
  }

}
