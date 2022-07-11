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

package dev.demeng.pluginbase.scheduler.builder;

import dev.demeng.pluginbase.promise.ThreadContext;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * Functional builder providing chained access to the functionality in
 * {@link dev.demeng.pluginbase.scheduler.Scheduler};
 */
public interface TaskBuilder {

  /**
   * Gets a task builder instance
   *
   * @return a task builder instance
   */
  @NotNull
  static TaskBuilder newBuilder() {
    return TaskBuilderImpl.INSTANCE;
  }

  /**
   * Defines the thread context of the new task, and returns the next builder in the chain.
   *
   * @param context the context to run the task in
   * @return a contextual builder
   */
  @NotNull
  default TaskBuilder.ThreadContextual on(@NotNull final ThreadContext context) {
    switch (context) {
      case SYNC:
        return sync();
      case ASYNC:
        return async();
      default:
        throw new AssertionError();
    }
  }

  /**
   * Marks that the new task should run sync, and returns the next builder in the chain.
   *
   * @return a "sync" contextual builder
   */
  @NotNull
  TaskBuilder.ThreadContextual sync();

  /**
   * Marks that the new task should run async, and returns the next builder in the chain.
   *
   * @return an "async" contextual builder
   */
  @NotNull
  TaskBuilder.ThreadContextual async();

  /**
   * The next builder in the task chain, which already has a defined task context.
   */
  interface ThreadContextual {

    /**
     * Marks that the new task should execute immediately, and returns the next builder in the
     * chain.
     *
     * @return an "instant" promise builder
     */
    @NotNull
    ContextualPromiseBuilder now();

    /**
     * Marks that the new task should run after the specified delay, and returns the next builder in
     * the chain.
     *
     * @param ticks the number of ticks to delay execution by
     * @return a delayed builder
     */
    @NotNull
    TaskBuilder.DelayedTick after(long ticks);

    /**
     * Marks that the new task should run after the specified delay, and returns the next builder in
     * the chain.
     *
     * @param duration the duration to delay execution by
     * @param unit     the units of the duration
     * @return a delayed builder
     */
    @NotNull
    TaskBuilder.DelayedTime after(long duration, @NotNull TimeUnit unit);

    /**
     * Marks that the new task should run after the specified delay, then repeat on the specified
     * interval, and returns the next builder in the chain.
     *
     * @param ticks the number of ticks to delay execution by
     * @return a delayed builder
     */
    @NotNull
    ContextualTaskBuilder afterAndEvery(long ticks);

    /**
     * Marks that the new task should run after the specified delay, then repeat on the specified
     * interval, and returns the next builder in the chain.
     *
     * @param duration the duration to delay execution by
     * @param unit     the units of the duration
     * @return a delayed builder
     */
    @NotNull
    ContextualTaskBuilder afterAndEvery(long duration, @NotNull TimeUnit unit);

    /**
     * Marks that the new task should start running instantly, but repeat on the specified interval,
     * and returns the next builder in the chain.
     *
     * @param ticks the number of ticks to wait between executions
     * @return a delayed builder
     */
    @NotNull
    ContextualTaskBuilder every(long ticks);

    /**
     * Marks that the new task should start running instantly, but repeat on the specified interval,
     * and returns the next builder in the chain.
     *
     * @param duration the duration to wait between executions
     * @param unit     the units of the duration
     * @return a delayed builder
     */
    @NotNull
    ContextualTaskBuilder every(long duration, @NotNull TimeUnit unit);

  }

  /**
   * The next builder in the task chain, which already has a defined delay context.
   *
   * <p>This interface extends {@link ContextualPromiseBuilder} to allow for
   * delayed, non-repeating tasks.</p>
   */
  interface Delayed extends ContextualPromiseBuilder {

  }

  interface DelayedTick extends Delayed {

    /**
     * Marks that the new task should repeat on the specified interval, and returns the next builder
     * in the chain.
     *
     * @param ticks the number of ticks to wait between executions
     * @return a delayed builder
     */
    @NotNull
    ContextualTaskBuilder every(long ticks);

  }

  interface DelayedTime extends Delayed {

    /**
     * Marks that the new task should repeat on the specified interval, and returns the next builder
     * in the chain.
     *
     * @param duration the duration to wait between executions
     * @param unit     the units of the duration
     * @return a delayed builder
     */
    @NotNull
    ContextualTaskBuilder every(long duration, TimeUnit unit);
  }
}
