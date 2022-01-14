/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

package dev.demeng.pluginbase.terminable;

import org.jetbrains.annotations.Nullable;

/**
 * An extension of {@link AutoCloseable}.
 */
@FunctionalInterface
public interface Terminable extends AutoCloseable {

  Terminable EMPTY = () -> {
  };

  /**
   * Closes this resource.
   */
  @Override
  void close() throws Exception;

  /**
   * Gets if the object represented by this instance is already permanently closed.
   *
   * @return True if this terminable is closed permanently
   */
  default boolean isClosed() {
    return false;
  }

  /**
   * Silently closes this resource, and returns the exception if one is thrown.
   *
   * @return The exception is one is thrown
   */
  @Nullable
  default Exception closeSilently() {
    try {
      close();
      return null;
    } catch (final Exception ex) {
      return ex;
    }
  }
}
