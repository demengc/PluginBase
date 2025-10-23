/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

package dev.demeng.pluginbase.event.filter;

import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Defines standard event predicates for use in functional event handlers.
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventHandlers {

  private static final Consumer<? extends Cancellable> SET_CANCELLED = e -> e.setCancelled(true);
  private static final Consumer<? extends Cancellable> UNSET_CANCELLED = e -> e.setCancelled(false);

  /**
   * Returns a consumer which cancels the event
   *
   * @param <T> the event type
   * @return a consumer which cancels the event
   */
  @NotNull
  public static <T extends Cancellable> Consumer<T> cancel() {
    return (Consumer<T>) SET_CANCELLED;
  }

  /**
   * Returns a consumer which un-cancels the event
   *
   * @param <T> the event type
   * @return a consumer which un-cancels the event
   */
  @NotNull
  public static <T extends Cancellable> Consumer<T> uncancel() {
    return (Consumer<T>) UNSET_CANCELLED;
  }
}
