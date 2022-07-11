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

package dev.demeng.pluginbase.event.functional.single;

import com.google.common.base.Preconditions;
import dev.demeng.pluginbase.delegate.Delegates;
import dev.demeng.pluginbase.event.SingleSubscription;
import dev.demeng.pluginbase.event.functional.ExpiryTestStage;
import dev.demeng.pluginbase.event.functional.SubscriptionBuilder;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

/**
 * Functional builder for {@link SingleSubscription}s.
 *
 * @param <T> the event type
 */
public interface SingleSubscriptionBuilder<T extends Event> extends SubscriptionBuilder<T> {

  /**
   * Makes a HandlerBuilder for a given event
   *
   * @param eventClass the class of the event
   * @param <T>        the event type
   * @return a {@link SingleSubscriptionBuilder} to construct the event handler
   * @throws NullPointerException if eventClass is null
   */
  @NotNull
  static <T extends Event> SingleSubscriptionBuilder<T> newBuilder(
      @NotNull final Class<T> eventClass) {
    return newBuilder(eventClass, EventPriority.NORMAL);
  }

  /**
   * Makes a HandlerBuilder for a given event
   *
   * @param eventClass the class of the event
   * @param priority   the priority to listen at
   * @param <T>        the event type
   * @return a {@link SingleSubscriptionBuilder} to construct the event handler
   * @throws NullPointerException if eventClass or priority is null
   */
  @NotNull
  static <T extends Event> SingleSubscriptionBuilder<T> newBuilder(
      @NotNull final Class<T> eventClass,
      @NotNull final EventPriority priority) {
    Objects.requireNonNull(eventClass, "eventClass");
    Objects.requireNonNull(priority, "priority");
    return new SingleSubscriptionBuilderImpl<>(eventClass, priority);
  }

  // override return type - we return SingleSubscriptionBuilder, not SubscriptionBuilder

  @NotNull
  @Override
  default SingleSubscriptionBuilder<T> expireIf(@NotNull final Predicate<T> predicate) {
    return expireIf(Delegates.predicateToBiPredicateSecond(predicate), ExpiryTestStage.PRE,
        ExpiryTestStage.POST_HANDLE);
  }

  @NotNull
  @Override
  default SingleSubscriptionBuilder<T> expireAfter(final long duration,
      @NotNull final TimeUnit unit) {
    Objects.requireNonNull(unit, "unit");
    Preconditions.checkArgument(duration >= 1, "duration < 1");
    final long expiry = Math.addExact(System.currentTimeMillis(), unit.toMillis(duration));
    return expireIf((handler, event) -> System.currentTimeMillis() > expiry, ExpiryTestStage.PRE);
  }

  @NotNull
  @Override
  default SingleSubscriptionBuilder<T> expireAfter(final long maxCalls) {
    Preconditions.checkArgument(maxCalls >= 1, "maxCalls < 1");
    return expireIf((handler, event) -> handler.getCallCounter() >= maxCalls, ExpiryTestStage.PRE,
        ExpiryTestStage.POST_HANDLE);
  }

  @NotNull
  @Override
  SingleSubscriptionBuilder<T> filter(@NotNull Predicate<T> predicate);

  /**
   * Add a expiry predicate.
   *
   * @param predicate  the expiry test
   * @param testPoints when to test the expiry predicate
   * @return ths builder instance
   */
  @NotNull
  SingleSubscriptionBuilder<T> expireIf(@NotNull BiPredicate<SingleSubscription<T>, T> predicate,
      @NotNull ExpiryTestStage... testPoints);

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
  SingleSubscriptionBuilder<T> exceptionConsumer(
      @NotNull BiConsumer<? super T, Throwable> consumer);

  /**
   * Sets that the handler should accept subclasses of the event type.
   *
   * @return the builder instance
   */
  @NotNull
  SingleSubscriptionBuilder<T> handleSubclasses();

  /**
   * Return the handler list builder to append handlers for the event.
   *
   * @return the handler list
   */
  @NotNull
  SingleHandlerList<T> handlers();

  /**
   * Builds and registers the Handler.
   *
   * @param handler the consumer responsible for handling the event.
   * @return a registered {@link SingleSubscription} instance.
   * @throws NullPointerException if the handler is null
   */
  @NotNull
  default SingleSubscription<T> handler(@NotNull final Consumer<? super T> handler) {
    return handlers().consumer(handler).register();
  }

  /**
   * Builds and registers the Handler.
   *
   * @param handler the bi-consumer responsible for handling the event.
   * @return a registered {@link SingleSubscription} instance.
   * @throws NullPointerException if the handler is null
   */
  @NotNull
  default SingleSubscription<T> biHandler(
      @NotNull final BiConsumer<SingleSubscription<T>, ? super T> handler) {
    return handlers().biConsumer(handler).register();
  }

}
