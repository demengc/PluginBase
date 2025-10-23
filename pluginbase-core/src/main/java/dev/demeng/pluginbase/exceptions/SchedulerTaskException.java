/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

package dev.demeng.pluginbase.exceptions;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.delegate.Delegate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A general exception caused by a scheduler task.
 */
public class SchedulerTaskException extends BaseException {

  public SchedulerTaskException(@NotNull final Throwable cause) {
    super("scheduler task", cause);
  }

  /**
   * Wraps a runnable into a runnable that catches its own exceptions and logs them using
   * {@link Common#error(Throwable, String, boolean, CommandSender...)}.
   *
   * @param runnable The runnable
   * @return The wrapped runnable
   */
  @NotNull
  public static Runnable wrap(@NotNull final Runnable runnable) {
    return new SchedulerWrappedRunnable(runnable);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class SchedulerWrappedRunnable implements Runnable, Delegate<Runnable> {

    @Getter @NotNull private final Runnable delegate;

    @Override
    public void run() {
      try {
        this.delegate.run();
      } catch (final Throwable t) {
        Common.error(new SchedulerTaskException(t),
            "Error whilst executing scheduler task.", false);
      }
    }
  }
}
