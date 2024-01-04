/*
 * MIT License
 *
 * Copyright (c) 2024 Demeng Chen
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) lucko/helper contributors
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

package dev.demeng.pluginbase.delegate;

import dev.demeng.pluginbase.exceptions.BaseException;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A collection of utility methods for delegating Java 8 functions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Delegates {

  public static <T> Consumer<T> runnableToConsumer(final Runnable runnable) {
    return new RunnableToConsumer<>(runnable);
  }

  public static Supplier<Void> runnableToSupplier(final Runnable runnable) {
    return new RunnableToSupplier<>(runnable);
  }

  public static <T> Supplier<T> callableToSupplier(final Callable<T> callable) {
    return new CallableToSupplier<>(callable);
  }

  public static <T, U> BiConsumer<T, U> consumerToBiConsumerFirst(final Consumer<T> consumer) {
    return new ConsumerToBiConsumerFirst<>(consumer);
  }

  public static <T, U> BiConsumer<T, U> consumerToBiConsumerSecond(final Consumer<U> consumer) {
    return new ConsumerToBiConsumerSecond<>(consumer);
  }

  public static <T, U> BiPredicate<T, U> predicateToBiPredicateFirst(final Predicate<T> predicate) {
    return new PredicateToBiPredicateFirst<>(predicate);
  }

  public static <T, U> BiPredicate<T, U> predicateToBiPredicateSecond(
      final Predicate<U> predicate) {
    return new PredicateToBiPredicateSecond<>(predicate);
  }

  public static <T, U> Function<T, U> consumerToFunction(final Consumer<T> consumer) {
    return new ConsumerToFunction<>(consumer);
  }

  public static <T, U> Function<T, U> runnableToFunction(final Runnable runnable) {
    return new RunnableToFunction<>(runnable);
  }

  private abstract static class AbstractDelegate<T> implements Delegate<T> {

    final T delegate;

    AbstractDelegate(final T delegate) {
      this.delegate = delegate;
    }

    @Override
    public T getDelegate() {
      return delegate;
    }
  }

  private static final class RunnableToConsumer<T> extends AbstractDelegate<Runnable> implements
      Consumer<T> {

    RunnableToConsumer(final Runnable delegate) {
      super(delegate);
    }

    @Override
    public void accept(final T t) {
      delegate.run();
    }
  }

  private static final class CallableToSupplier<T> extends AbstractDelegate<Callable<T>> implements
      Supplier<T> {

    CallableToSupplier(final Callable<T> delegate) {
      super(delegate);
    }

    @Override
    public T get() {
      try {
        return delegate.call();
      } catch (final RuntimeException ex) {
        throw ex;
      } catch (final Exception ex) {
        throw new BaseException(ex);
      }
    }
  }

  private static final class RunnableToSupplier<T> extends AbstractDelegate<Runnable> implements
      Supplier<T> {

    RunnableToSupplier(final Runnable delegate) {
      super(delegate);
    }

    @Override
    public T get() {
      delegate.run();
      return null;
    }
  }

  private static final class ConsumerToBiConsumerFirst<T, U> extends
      AbstractDelegate<Consumer<T>> implements BiConsumer<T, U> {

    ConsumerToBiConsumerFirst(final Consumer<T> delegate) {
      super(delegate);
    }

    @Override
    public void accept(final T t, final U u) {
      delegate.accept(t);
    }
  }

  private static final class ConsumerToBiConsumerSecond<T, U> extends
      AbstractDelegate<Consumer<U>> implements BiConsumer<T, U> {

    ConsumerToBiConsumerSecond(final Consumer<U> delegate) {
      super(delegate);
    }

    @Override
    public void accept(final T t, final U u) {
      delegate.accept(u);
    }
  }

  private static final class PredicateToBiPredicateFirst<T, U> extends
      AbstractDelegate<Predicate<T>> implements BiPredicate<T, U> {

    PredicateToBiPredicateFirst(final Predicate<T> delegate) {
      super(delegate);
    }

    @Override
    public boolean test(final T t, final U u) {
      return delegate.test(t);
    }
  }

  private static final class PredicateToBiPredicateSecond<T, U> extends
      AbstractDelegate<Predicate<U>> implements BiPredicate<T, U> {

    PredicateToBiPredicateSecond(final Predicate<U> delegate) {
      super(delegate);
    }

    @Override
    public boolean test(final T t, final U u) {
      return delegate.test(u);
    }
  }

  private static final class ConsumerToFunction<T, R> extends
      AbstractDelegate<Consumer<T>> implements Function<T, R> {

    ConsumerToFunction(final Consumer<T> delegate) {
      super(delegate);
    }

    @Override
    public R apply(final T t) {
      delegate.accept(t);
      return null;
    }
  }

  private static final class RunnableToFunction<T, R> extends AbstractDelegate<Runnable> implements
      Function<T, R> {

    RunnableToFunction(final Runnable delegate) {
      super(delegate);
    }

    @Override
    public R apply(final T t) {
      delegate.run();
      return null;
    }
  }
}
