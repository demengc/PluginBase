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

package dev.demeng.pluginbase.queue;

import dev.demeng.pluginbase.TaskUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * An abstract manager for {@link Queueable} objects.
 *
 * @param <T> The queueable object
 */
public abstract class QueueManager<T extends Queueable> {

  /**
   * The list of all currently queued objects. Queued objects will remain in this list until the
   * entirety of its {@link Queueable#getDuration()} is over.
   */
  @Getter private final List<T> queueList = new ArrayList<>();

  private final List<BukkitRunnable> tasks = new ArrayList<>();

  /**
   * Queues a new queueable object.
   *
   * @param queueable The queueable object
   */
  public void queue(T queueable) {

    final boolean queued = !queueList.isEmpty();
    final int waitTime = calculateWaitTime();

    if (!queued) {
      queueable.run();
    }

    queueList.add(queueable);

    tasks.add(TaskUtils.delay(task -> {
      if (queued) {
        queueable.run();
      }
    }, queued ? waitTime : queueable.getDuration()));
  }

  /**
   * Calculates the current wait time if a new object were to be queued right now.
   *
   * @return The current wait time
   */
  public int calculateWaitTime() {
    return queueList.stream().mapToInt(T::getDuration).sum();
  }

  /**
   * Clears the current queue and cancels all queued tasks.
   */
  public void clear() {

    for (BukkitRunnable task : tasks) {
      if (!task.isCancelled()) {
        task.cancel();
      }
    }

    queueList.clear();
    tasks.clear();
  }
}
