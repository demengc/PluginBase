package dev.demeng.pluginbase;

import dev.demeng.pluginbase.plugin.BaseLoader;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

/** Utilities for quickly creating various Bukkit runnables/tasks inside your plugin. */
public class TaskUtils {

  /**
   * Runs the task once on the primary thread, without delay nor repetition.
   *
   * @param task The task to run
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable runSync(Consumer<BukkitRunnable> task) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTask(BaseLoader.getPlugin());
    return runnable;
  }

  /**
   * Runs the task once, asyncronously, without delay nor repetition.
   *
   * @param task The task to run
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable runAsync(Consumer<BukkitRunnable> task) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskAsynchronously(BaseLoader.getPlugin());
    return runnable;
  }

  /**
   * Runs the task once, with an initial delay.
   *
   * @param task The task to run
   * @param delay The number of ticks to delay
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable delay(Consumer<BukkitRunnable> task, long delay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskLater(BaseLoader.getPlugin(), delay);
    return runnable;
  }

  /**
   * Runs the task once, asyncronously, with an initial delay.
   *
   * @param task The task to run
   * @param delay The number of ticks to delay
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable delayAsync(Consumer<BukkitRunnable> task, long delay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskLaterAsynchronously(BaseLoader.getPlugin(), delay);
    return runnable;
  }

  /**
   * Runs the task repeatedly, with an initial delay and a delay between runs.
   *
   * @param task The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay The number of ticks between eech run
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable repeat(
      Consumer<BukkitRunnable> task, long initialDelay, long repeatDelay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskTimer(BaseLoader.getPlugin(), initialDelay, repeatDelay);
    return runnable;
  }

  /**
   * Runs the task repeatedly, asyncronously, with an initial delay and a delay between runs.
   *
   * @param task The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay The number of ticks between eech run
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable repeatAsync(
      Consumer<BukkitRunnable> task, long initialDelay, long repeatDelay) {
    final BukkitRunnable runnable = createSimpleTask(task);
    runnable.runTaskTimerAsynchronously(BaseLoader.getPlugin(), initialDelay, repeatDelay);
    return runnable;
  }

  /**
   * Runs the task repeatedly, with an initial delay, a delay between runs, and a number of
   * repetitions.
   *
   * @param task The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay The number of ticks between eech run
   * @param limit The maximum number of times the task will be repeated
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable repeat(
      Consumer<BukkitRunnable> task, long initialDelay, long repeatDelay, int limit) {

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
   * Runs the task repeatedly, asyncronously, with an initial delay, a delay between runs, and a
   * number of repetitions.
   *
   * @param task The task to run
   * @param initialDelay The number of ticks to delay before the task starts repeating
   * @param repeatDelay The number of ticks between eech run
   * @param limit The maximum number of times the task will be repeated
   * @return The BukkitRunnable that was initialized for this task
   */
  public static BukkitRunnable repeatAsync(
      Consumer<BukkitRunnable> task, long initialDelay, long repeatDelay, int limit) {

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

  private static BukkitRunnable createSimpleTask(Consumer<BukkitRunnable> task) {
    return new BukkitRunnable() {
      @Override
      public void run() {
        task.accept(this);
      }
    };
  }
}
