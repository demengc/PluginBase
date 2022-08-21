/*
 * MIT License
 *
 * Copyright (c) 2022 Demeng Chen
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

import dev.demeng.pluginbase.plugin.BaseManager;

/**
 * Represents the two main types of {@link Thread} on the server.
 */
public enum ThreadContext {

  SYNC, ASYNC;

  /**
   * Gets the thread context of the current thread.
   *
   * @return The threat context of the current thread
   */
  public static ThreadContext forCurrentThread() {
    return forThread(Thread.currentThread());
  }

  /**
   * Gets the thread context of the specified thread.
   *
   * @param thread The thread to get the context of
   * @return The thread context of the specified thread
   */
  public static ThreadContext forThread(final Thread thread) {
    return thread == BaseManager.getMainThread() ? SYNC : ASYNC;
  }
}
