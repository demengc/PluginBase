/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package dev.demeng.pluginbase.promise;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.delegate.Delegate;
import dev.demeng.pluginbase.exceptions.PromiseChainException;
import dev.demeng.pluginbase.exceptions.SchedulerTaskException;
import dev.demeng.pluginbase.plugin.BaseManager;
import dev.demeng.pluginbase.scheduler.BaseExecutors;
import dev.demeng.pluginbase.scheduler.Ticks;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link Promise} using the server scheduler.
 *
 * @param <V> the result type
 */
final class BasePromise<V> implements Promise<V> {

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
   * If the promise is currently being supplied
   */
  private final AtomicBoolean supplied = new AtomicBoolean(false);

  /**
   * If the execution of the promise is cancelled
   */
  private final AtomicBoolean cancelled = new AtomicBoolean(false);

  /**
   * The completable future backing this promise
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
    (this.fut = new CompletableFuture<>()).completeExceptionally(t);
    this.supplied.set(true);
  }

  private BasePromise(@NotNull final CompletableFuture<V> fut) {
    this.fut = Objects.requireNonNull(fut, "future");
    this.supplied.set(true);
    this.cancelled.set(fut.isCancelled());
  }

  /* utility methods */

  private void executeSync(@NotNull final Runnable runnable) {
    if (ThreadContext.forCurrentThread() == ThreadContext.SYNC) {
      SchedulerTaskException.wrap(runnable).run();
    } else {
      BaseExecutors.sync().execute(runnable);
    }
  }

  private void executeAsync(@NotNull final Runnable runnable) {
    BaseExecutors.asyncHelper().execute(runnable);
  }

  private void executeDelayedSync(@NotNull final Runnable runnable, final long delayTicks) {
    if (delayTicks <= 0) {
      executeSync(runnable);
    } else {
      Bukkit.getScheduler()
          .runTaskLater(BaseManager.getPlugin(), SchedulerTaskException.wrap(runnable), delayTicks);
    }
  }

  private void executeDelayedAsync(@NotNull final Runnable runnable, final long delayTicks) {
    if (delayTicks <= 0) {
      executeAsync(runnable);
    } else {
      Bukkit.getScheduler().runTaskLaterAsynchronously(BaseManager.getPlugin(),
          SchedulerTaskException.wrap(runnable), delayTicks);
    }
  }

  private void executeDelayedSync(@NotNull final Runnable runnable, final long delay,
      final TimeUnit unit) {
    if (delay <= 0) {
      executeSync(runnable);
    } else {
      Bukkit.getScheduler()
          .runTaskLater(BaseManager.getPlugin(), SchedulerTaskException.wrap(runnable),
              Ticks.from(delay, unit));
    }
  }

  private void executeDelayedAsync(@NotNull final Runnable runnable, final long delay,
      final TimeUnit unit) {
    if (delay <= 0) {
      executeAsync(runnable);
    } else {
      BaseExecutors.asyncHelper().schedule(SchedulerTaskException.wrap(runnable), delay, unit);
    }
  }

  private boolean complete(final V value) {
    return !this.cancelled.get() && this.fut.complete(value);
  }

  private boolean completeExceptionally(@NotNull final Throwable t) {
    return !this.cancelled.get() && this.fut.completeExceptionally(t);
  }

  private void markAsSupplied() {
    if (!this.supplied.compareAndSet(false, true)) {
      throw new IllegalStateException("Promise is already being supplied.");
    }
  }

  /* future methods */

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    this.cancelled.set(true);
    return this.fut.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return this.fut.isCancelled();
  }

  @Override
  public boolean isDone() {
    return this.fut.isDone();
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    return this.fut.get();
  }

  @Override
  public V get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return this.fut.get(timeout, unit);
  }

  @Override
  public V join() {
    return this.fut.join();
  }

  @Override
  public V getNow(final V valueIfAbsent) {
    return this.fut.getNow(valueIfAbsent);
  }

  @Override
  public CompletableFuture<V> toCompletableFuture() {
    return this.fut.thenApply(Function.identity());
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
  public Promise<V> supplyDelayedSync(@NotNull final Supplier<V> supplier, final long delay,
      @NotNull final TimeUnit unit) {
    markAsSupplied();
    executeDelayedSync(new SupplyRunnable(supplier), delay, unit);
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
  public Promise<V> supplyDelayedAsync(@NotNull final Supplier<V> supplier, final long delay,
      @NotNull final TimeUnit unit) {
    markAsSupplied();
    executeDelayedAsync(new SupplyRunnable(supplier), delay, unit);
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
  public Promise<V> supplyExceptionallyDelayedSync(@NotNull final Callable<V> callable,
      final long delay,
      @NotNull final TimeUnit unit) {
    markAsSupplied();
    executeDelayedSync(new ThrowingSupplyRunnable(callable), delay, unit);
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
  public Promise<V> supplyExceptionallyDelayedAsync(@NotNull final Callable<V> callable,
      final long delay,
      @NotNull final TimeUnit unit) {
    markAsSupplied();
    executeDelayedAsync(new ThrowingSupplyRunnable(callable), delay, unit);
    return this;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenApplySync(@NotNull final Function<? super V, ? extends U> fn) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
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
    this.fut.whenComplete((value, t) -> {
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
    this.fut.whenComplete((value, t) -> {
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
  public <U> Promise<U> thenApplyDelayedSync(@NotNull final Function<? super V, ? extends U> fn,
      final long delay, @NotNull final TimeUnit unit) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedSync(new ApplyRunnable<>(promise, fn, value), delay, unit);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenApplyDelayedAsync(@NotNull final Function<? super V, ? extends U> fn,
      final long delayTicks) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
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
  public <U> Promise<U> thenApplyDelayedAsync(@NotNull final Function<? super V, ? extends U> fn,
      final long delay, @NotNull final TimeUnit unit) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedAsync(new ApplyRunnable<>(promise, fn, value), delay, unit);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenComposeSync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
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
    this.fut.whenComplete((value, t) -> {
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
    this.fut.whenComplete((value, t) -> {
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
  public <U> Promise<U> thenComposeDelayedSync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn, final long delay,
      @NotNull final TimeUnit unit) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedSync(new ComposeRunnable<>(promise, fn, value, true), delay, unit);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public <U> Promise<U> thenComposeDelayedAsync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn, final long delayTicks) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
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
  public <U> Promise<U> thenComposeDelayedAsync(
      @NotNull final Function<? super V, ? extends Promise<U>> fn, final long delay,
      @NotNull final TimeUnit unit) {
    final BasePromise<U> promise = empty();
    this.fut.whenComplete((value, t) -> {
      if (t != null) {
        promise.completeExceptionally(t);
      } else {
        executeDelayedAsync(new ComposeRunnable<>(promise, fn, value, false), delay, unit);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public Promise<V> exceptionallySync(@NotNull final Function<Throwable, ? extends V> fn) {
    final BasePromise<V> promise = empty();
    this.fut.whenComplete((value, t) -> {
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
    this.fut.whenComplete((value, t) -> {
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
    this.fut.whenComplete((value, t) -> {
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
  public Promise<V> exceptionallyDelayedSync(@NotNull final Function<Throwable, ? extends V> fn,
      final long delay, @NotNull final TimeUnit unit) {
    final BasePromise<V> promise = empty();
    this.fut.whenComplete((value, t) -> {
      if (t == null) {
        promise.complete(value);
      } else {
        executeDelayedSync(new ExceptionallyRunnable<>(promise, fn, t), delay, unit);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public Promise<V> exceptionallyDelayedAsync(@NotNull final Function<Throwable, ? extends V> fn,
      final long delayTicks) {
    final BasePromise<V> promise = empty();
    this.fut.whenComplete((value, t) -> {
      if (t == null) {
        promise.complete(value);
      } else {
        executeDelayedAsync(new ExceptionallyRunnable<>(promise, fn, t), delayTicks);
      }
    });
    return promise;
  }

  @NotNull
  @Override
  public Promise<V> exceptionallyDelayedAsync(@NotNull final Function<Throwable, ? extends V> fn,
      final long delay, @NotNull final TimeUnit unit) {
    final BasePromise<V> promise = empty();
    this.fut.whenComplete((value, t) -> {
      if (t == null) {
        promise.complete(value);
      } else {
        executeDelayedAsync(new ExceptionallyRunnable<>(promise, fn, t), delay, unit);
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
      return this.supplier;
    }

    @Override
    public void run() {
      if (BasePromise.this.cancelled.get()) {
        return;
      }
      try {
        BasePromise.this.fut.complete(this.supplier.call());
      } catch (final Throwable t) {
        Common.error(new PromiseChainException(t),
            "Error whilst executing promise chain.", false);
        BasePromise.this.fut.completeExceptionally(t);
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
      return this.supplier;
    }

    @Override
    public void run() {
      if (BasePromise.this.cancelled.get()) {
        return;
      }
      try {
        BasePromise.this.fut.complete(this.supplier.get());
      } catch (final Throwable t) {
        Common.error(new PromiseChainException(t),
            "Error whilst executing promise chain.", false);
        BasePromise.this.fut.completeExceptionally(t);
      }
    }
  }

  private final class ApplyRunnable<U> implements Runnable, Delegate<Function> {

    private final BasePromise<U> promise;
    private final Function<? super V, ? extends U> function;
    private final V value;

    private ApplyRunnable(final BasePromise<U> promise,
        final Function<? super V, ? extends U> function,
        final V value) {
      this.promise = promise;
      this.function = function;
      this.value = value;
    }

    @Override
    public Function getDelegate() {
      return this.function;
    }

    @Override
    public void run() {
      if (BasePromise.this.cancelled.get()) {
        return;
      }
      try {
        this.promise.complete(this.function.apply(this.value));
      } catch (final Throwable t) {
        Common.error(new PromiseChainException(t),
            "Error whilst executing promise chain.", false);
        this.promise.completeExceptionally(t);
      }
    }
  }

  private final class ComposeRunnable<U> implements Runnable, Delegate<Function> {

    private final BasePromise<U> promise;
    private final Function<? super V, ? extends Promise<U>> function;
    private final V value;
    private final boolean sync;

    private ComposeRunnable(final BasePromise<U> promise,
        final Function<? super V, ? extends Promise<U>> function, final V value,
        final boolean sync) {
      this.promise = promise;
      this.function = function;
      this.value = value;
      this.sync = sync;
    }

    @Override
    public Function getDelegate() {
      return this.function;
    }

    @Override
    public void run() {
      if (BasePromise.this.cancelled.get()) {
        return;
      }
      try {
        final Promise<U> p = this.function.apply(this.value);
        if (p == null) {
          this.promise.complete(null);
        } else {
          if (this.sync) {
            p.thenAcceptSync(this.promise::complete);
          } else {
            p.thenAcceptAsync(this.promise::complete);
          }
        }
      } catch (final Throwable t) {
        Common.error(new PromiseChainException(t),
            "Error whilst executing promise chain.", false);
        this.promise.completeExceptionally(t);
      }
    }
  }

  private final class ExceptionallyRunnable<U> implements Runnable, Delegate<Function> {

    private final BasePromise<U> promise;
    private final Function<Throwable, ? extends U> function;
    private final Throwable t;

    private ExceptionallyRunnable(final BasePromise<U> promise,
        final Function<Throwable, ? extends U> function,
        final Throwable t) {
      this.promise = promise;
      this.function = function;
      this.t = t;
    }

    @Override
    public Function getDelegate() {
      return this.function;
    }

    @Override
    public void run() {
      if (BasePromise.this.cancelled.get()) {
        return;
      }
      try {
        this.promise.complete(this.function.apply(this.t));
      } catch (final Throwable t) {
        Common.error(new PromiseChainException(t),
            "Error whilst executing promise chain.", false);
        this.promise.completeExceptionally(t);
      }
    }
  }
}
