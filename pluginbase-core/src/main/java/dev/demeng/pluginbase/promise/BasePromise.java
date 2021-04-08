/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.promise;

import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.delegate.Delegate;
import dev.demeng.pluginbase.utils.TaskUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link Promise} using the server scheduler.
 *
 * @param <V> the result type
 */
final class BasePromise<V> implements Promise<V> {

  private static final Consumer<Throwable> EXCEPTION_CONSUMER = throwable -> {
    ChatUtils.log(Level.SEVERE, "[SCHEDULER] Exception thrown whilst executing task");
    throwable.printStackTrace();
  };

  @NotNull
  static <U> BasePromise<U> empty() {
    return new BasePromise<>();
  }

  @NotNull
  static <U> BasePromise<U> completed(@Nullable final U value) {
    return new BasePromise<>(value);
  }

  @NotNull
  static <U> BasePromise<U> exceptionally(@NotNull final Throwable t) {
    return new BasePromise<>(t);
  }

  /**
   * If the promise is currently being supplied.
   */
  private final AtomicBoolean supplied = new AtomicBoolean(false);

  /**
   * If the execution of the promise is cancelled.
   */
  private final AtomicBoolean cancelled = new AtomicBoolean(false);

  /**
   * The completable future backing this promise.
   */
  @NotNull
  private final CompletableFuture<V> fut;

  private BasePromise() {
    this.fut = new CompletableFuture<>();
  }

  private BasePromise(@Nullable final V v) {
    this.fut = CompletableFuture.completedFuture(v);
    this.supplied.set(true);
  }

  private BasePromise(@NotNull final Throwable t) {
    this.fut = new CompletableFuture<>();
    fut.completeExceptionally(t);
    this.supplied.set(true);
  }

  private void executeSync(@NotNull final Runnable runnable) {
    if (ThreadContext.forCurrentThread() == ThreadContext.SYNC) {
      runnable.run();
    } else {
      TaskUtils.runSync(task -> runnable.run());
    }
  }

  private void executeAsync(@NotNull final Runnable runnable) {
    TaskUtils.runAsync(task -> runnable.run());
  }

  private void executeDelayedSync(@NotNull final Runnable runnable, final long delayTicks) {
    if (delayTicks <= 0) {
      executeSync(runnable);
    } else {
      TaskUtils.delay(task -> runnable.run(), delayTicks);
    }
  }

  private void executeDelayedAsync(@NotNull final Runnable runnable, final long delayTicks) {
    if (delayTicks <= 0) {
      executeAsync(runnable);
    } else {
      TaskUtils.delayAsync(task -> runnable.run(), delayTicks);
    }
  }

  private boolean complete(final V value) {
    return !cancelled.get() && fut.complete(value);
  }

  private boolean completeExceptionally(@NotNull final Throwable t) {
    return !cancelled.get() && fut.completeExceptionally(t);
  }

  private void markAsSupplied() {
    if (!supplied.compareAndSet(false, true)) {
      throw new IllegalStateException("Promise is already being supplied.");
    }
  }

  /* future methods */

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    cancelled.set(true);
    return fut.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return fut.isCancelled();
  }

  @Override
  public boolean isDone() {
    return fut.isDone();
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    return fut.get();
  }

  @Override
  public V get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return fut.get(timeout, unit);
  }

  @Override
  public V join() {
    return fut.join();
  }

  @Override
  public V getNow(final V valueIfAbsent) {
    return fut.getNow(valueIfAbsent);
  }

  @Override
  public CompletableFuture<V> toCompletableFuture() {
    return fut.thenApply(Function.identity());
  }

  @Override
  public void close() {
    cancel();
  }

  @Override
  public boolean isClosed() {
    return isCancelled();
  }

  /* implementation */

  @NotNull
  @Override
  public Promise<V> supply(@Nullable final V value) {
    markAsSupplied();
    complete(value);
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyException(@NotNull final Throwable exception) {
    markAsSupplied();
    completeExceptionally(exception);
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplySync(@NotNull final Supplier<V> supplier) {
    markAsSupplied();
    executeSync(new SupplyRunnable(supplier));
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyAsync(@NotNull final Supplier<V> supplier) {
    markAsSupplied();
    executeAsync(new SupplyRunnable(supplier));
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyDelayedSync(@NotNull final Supplier<V> supplier, final long delayTicks) {
    markAsSupplied();
    executeDelayedSync(new SupplyRunnable(supplier), delayTicks);
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyDelayedAsync(@NotNull final Supplier<V> supplier, final long delayTicks) {
    markAsSupplied();
    executeDelayedAsync(new SupplyRunnable(supplier), delayTicks);
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyExceptionallySync(@NotNull final Callable<V> callable) {
    markAsSupplied();
    executeSync(new ThrowingSupplyRunnable(callable));
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyExceptionallyAsync(@NotNull final Callable<V> callable) {
    markAsSupplied();
    executeAsync(new ThrowingSupplyRunnable(callable));
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyExceptionallyDelayedSync(@NotNull final Callable<V> callable,
      final long delayTicks) {
    markAsSupplied();
    executeDelayedSync(new ThrowingSupplyRunnable(callable), delayTicks);
    return this;
  }

  @NotNull
  @Override
  public Promise<V> supplyExceptionallyDelayedAsync(@NotNull final Callable<V> callable,
      final long delayTicks) {
    markAsSupplied();
    executeDelayedAsync(new ThrowingSupplyRunnable(callable), delayTicks);
    return this;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenApplySync(@NotNull final Function<? super V, ? extends U> fn) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeSync(new ApplyRunnable<>(promise, fn, value));
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenApplyAsync(@NotNull final Function<? super V, ? extends U> fn) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeAsync(new ApplyRunnable<>(promise, fn, value));
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenApplyDelayedSync(@NotNull final Function<? super V, ? extends U> fn,
      final long delayTicks) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedSync(new ApplyRunnable<>(promise, fn, value), delayTicks);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenApplyDelayedAsync(@NotNull final Function<? super V, ? extends U> fn,
      final long delayTicks) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedAsync(new ApplyRunnable<>(promise, fn, value), delayTicks);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenComposeSync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeSync(new ComposeRunnable<>(promise, fn, value, true));
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenComposeAsync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeAsync(new ComposeRunnable<>(promise, fn, value, false));
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenComposeDelayedSync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn, final long delayTicks) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedSync(new ComposeRunnable<>(promise, fn, value, true), delayTicks);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenComposeDelayedAsync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn, final long delayTicks) {
    final BasePromise<U> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedAsync(new ComposeRunnable<>(promise, fn, value, false), delayTicks);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public Promise<V> exceptionallySync(@NotNull final Function<Throwable, ? extends V> fn) {
    final BasePromise<V> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t == null) {
        promise.complete(value);
      } else {
        executeSync(new ExceptionallyRunnable<>(promise, fn, t));
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public Promise<V> exceptionallyAsync(@NotNull final Function<Throwable, ? extends V> fn) {
    final BasePromise<V> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t == null) {
        promise.complete(value);
      } else {
        executeAsync(new ExceptionallyRunnable<>(promise, fn, t));
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public Promise<V> exceptionallyDelayedSync(@NotNull final Function<Throwable, ? extends V> fn,
      final long delayTicks) {
    final BasePromise<V> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t == null) {
        promise.complete(value);
      } else {
        executeDelayedSync(new ExceptionallyRunnable<>(promise, fn, t), delayTicks);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public Promise<V> exceptionallyDelayedAsync(@NotNull final Function<Throwable, ? extends V> fn,
      final long delayTicks) {
    final BasePromise<V> promise = empty();
    fut.whenComplete((value, t) -> {
      if (t == null) {
        promise.complete(value);
      } else {
        executeDelayedAsync(new ExceptionallyRunnable<>(promise, fn, t), delayTicks);
      }
    });
    return promise;
  }

  /* delegating behaviour runnables */

  private final class ThrowingSupplyRunnable implements Runnable, Delegate<Callable<V>> {

    private final Callable<V> supplier;

    private ThrowingSupplyRunnable(final Callable<V> supplier) {
      this.supplier = supplier;
    }

    @Override
    public Callable<V> getDelegate() {
      return supplier;
    }

    @Override
    public void run() {

      if (BasePromise.this.cancelled.get()) {
        return;
      }

      try {
        BasePromise.this.fut.complete(supplier.call());

      } catch (final Exception ex) {
        EXCEPTION_CONSUMER.accept(ex);
        BasePromise.this.fut.completeExceptionally(ex);
      }
    }
  }

  private final class SupplyRunnable implements Runnable, Delegate<Supplier<V>> {

    private final Supplier<V> supplier;

    private SupplyRunnable(final Supplier<V> supplier) {
      this.supplier = supplier;
    }

    @Override
    public Supplier<V> getDelegate() {
      return supplier;
    }

    @Override
    public void run() {

      if (BasePromise.this.cancelled.get()) {
        return;
      }

      try {
        BasePromise.this.fut.complete(supplier.get());

      } catch (final Exception ex) {
        EXCEPTION_CONSUMER.accept(ex);
        BasePromise.this.fut.completeExceptionally(ex);
      }
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private final class ApplyRunnable<U> implements Runnable,
      Delegate<Function<? super V, ? extends U>> {

    private final BasePromise<U> promise;
    private final Function<? super V, ? extends U> function;
    private final V value;

    @Override
    public Function<? super V, ? extends U> getDelegate() {
      return function;
    }

    @Override
    public void run() {

      if (BasePromise.this.cancelled.get()) {
        return;
      }

      try {
        promise.complete(function.apply(this.value));

      } catch (final Exception ex) {
        EXCEPTION_CONSUMER.accept(ex);
        promise.completeExceptionally(ex);
      }
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private final class ComposeRunnable<U> implements Runnable,
      Delegate<Function<? super V, ? extends Promise<U>>> {

    private final BasePromise<U> promise;
    private final Function<? super V, ? extends Promise<U>> function;
    private final V value;
    private final boolean sync;

    @Override
    public Function<? super V, ? extends Promise<U>> getDelegate() {
      return function;
    }

    @Override
    public void run() {

      if (BasePromise.this.cancelled.get()) {
        return;
      }

      try {
        final Promise<U> p = function.apply(value);

        if (p == null) {
          promise.complete(null);

        } else {
          if (sync) {
            p.thenAcceptSync(promise::complete);

          } else {
            p.thenAcceptAsync(promise::complete);
          }
        }

      } catch (final Exception ex) {
        EXCEPTION_CONSUMER.accept(ex);
        promise.completeExceptionally(ex);
      }
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private final class ExceptionallyRunnable<U> implements Runnable,
      Delegate<Function<Throwable, ? extends U>> {

    private final BasePromise<U> promise;
    private final Function<Throwable, ? extends U> function;
    private final Throwable throwable;

    @Override
    public Function<Throwable, ? extends U> getDelegate() {
      return function;
    }

    @Override
    public void run() {

      if (BasePromise.this.cancelled.get()) {
        return;
      }

      try {
        promise.complete(function.apply(throwable));

      } catch (final Exception ex) {
        EXCEPTION_CONSUMER.accept(ex);
        promise.completeExceptionally(ex);
      }
    }
  }
}