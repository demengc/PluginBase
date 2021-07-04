/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.utils;

import dev.demeng.pluginbase.plugin.BaseLoader;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for quickly creating various Bukkit tasks inside your plugin.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskUtils {

  /**
   * Runs the task once on the primary thread, without delay nor repetition.
   *
   * @param task The task to run
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable runSync(final Consumer<BukkitRunnable> task) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTask(BaseLoader.getPlugin());
    return runnable;
  }

  /**
   * Runs the task once, asynchronously, without delay nor repetition.
   *
   * @param task The task to run
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable runAsync(final Consumer<BukkitRunnable> task) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskAsynchronously(BaseLoader.getPlugin());
    return runnable;
  }

  /**
   * Runs the task once, with an initial delay.
   *
   * @param task  The task to run
   * @param delay The number of ticks to delay
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable delay(final Consumer<BukkitRunnable> task, final long delay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskLater(BaseLoader.getPlugin(), delay);
    return runnable;
  }

  /**
   * Runs the task once, asynchronously, with an initial delay.
   *
   * @param task  The task to run
   * @param delay The number of ticks to delay
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable delayAsync(final Consumer<BukkitRunnable> task, final long delay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskLaterAsynchronously(BaseLoader.getPlugin(), delay);
    return runnable;
  }

  /**
   * Runs the task repeatedly, with an initial delay and a delay between runs.
   *
   * @param task         The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay  The number of ticks between each run
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable repeat(
      final Consumer<BukkitRunnable> task, final long initialDelay, final long repeatDelay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskTimer(BaseLoader.getPlugin(), initialDelay, repeatDelay);
    return runnable;
  }

  /**
   * Runs the task repeatedly, with an initial delay, a delay between runs, and a number of
   * repetitions.
   *
   * @param task         The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay  The number of ticks between each run
   * @param limit        The maximum number of times the task will be repeated
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable repeat(
      final Consumer<BukkitRunnable> task, final long initialDelay, final long repeatDelay,
      final int limit) {

    final BukkitRunnable runnable =
        new BukkitRunnable() {
          int repeats = 0;

          @Override
          public void run() {

            if (repeats > limit) {
              return;
            }

            task.accept(this);
            repeats++;
          }
        };

    runnable.runTaskTimer(BaseLoader.getPlugin(), initialDelay, repeatDelay);
    return runnable;
  }

  /**
   * Runs the task repeatedly, asynchronously, with an initial delay and a delay between runs.
   *
   * @param task         The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay  The number of ticks between each run
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable repeatAsync(
      final Consumer<BukkitRunnable> task, final long initialDelay, final long repeatDelay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskTimerAsynchronously(BaseLoader.getPlugin(), initialDelay, repeatDelay);
    return runnable;
  }

  /**
   * Runs the task repeatedly, asynchronously, with an initial delay, a delay between runs, and a
   * number of repetitions.
   *
   * @param task         The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay  The number of ticks between each run
   * @param limit        The maximum number of times the task will be repeated
   * @return The BukkitRunnable that was initialized for this task
   */
  @NotNull
  public static BukkitRunnable repeatAsync(
      final Consumer<BukkitRunnable> task, final long initialDelay, final long repeatDelay,
      final int limit) {

    final BukkitRunnable runnable =
        new BukkitRunnable() {
          int repeats = 0;

          @Override
          public void run() {

            if (repeats > limit) {
              return;
            }

            task.accept(this);
            repeats++;
          }
        };

    runnable.runTaskTimerAsynchronously(BaseLoader.getPlugin(), initialDelay, repeatDelay);
    return runnable;
  }

  private static BukkitRunnable createSimpleTask(final Consumer<BukkitRunnable> task) {
    return new BukkitRunnable() {
      @Override
      public void run() {
        task.accept(this);
      }
    };
  }
}
