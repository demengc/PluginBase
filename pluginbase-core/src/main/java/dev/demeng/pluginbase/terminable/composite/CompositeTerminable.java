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

package dev.demeng.pluginbase.terminable.composite;

import dev.demeng.pluginbase.terminable.Terminable;
import dev.demeng.pluginbase.terminable.TerminableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Terminable} made up of several other {@link Terminable}s.
 *
 * <p>The {@link #close()} method closes in LIFO (Last-In-First-Out) order.</p>
 *
 * <p>{@link Terminable}s can be reused. The instance is effectively
 * cleared on each invocation of {@link #close()}.</p>
 */
public interface CompositeTerminable extends Terminable, TerminableConsumer {

  /**
   * Creates a new standalone {@link CompositeTerminable}.
   *
   * @return a new {@link CompositeTerminable}
   */
  @NotNull
  static CompositeTerminable create() {
    return new AbstractCompositeTerminable();
  }

  /**
   * Creates a new standalone {@link CompositeTerminable}, which wraps contained terminables in
   * {@link java.lang.ref.WeakReference}s.
   *
   * @return a new {@link CompositeTerminable}
   */
  @NotNull
  static CompositeTerminable createWeak() {
    return new AbstractWeakCompositeTerminable();
  }

  /**
   * Closes this composite terminable.
   *
   * @throws CompositeClosingException if any of the sub-terminables throw an exception on close
   */
  @Override
  void close() throws CompositeClosingException;

  @Nullable
  @Override
  default CompositeClosingException closeSilently() {
    try {
      close();
      return null;
    } catch (final CompositeClosingException e) {
      return e;
    }
  }

  @Override
  default void closeAndReportException() {
    try {
      close();
    } catch (final CompositeClosingException e) {
      e.printAllStackTraces();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Binds an {@link AutoCloseable} with this composite closable.
   *
   * <p>Note that implementations do not keep track of duplicate contained
   * terminables. If a single {@link AutoCloseable} is added twice, it will be
   * {@link AutoCloseable#close() closed} twice.</p>
   *
   * @param autoCloseable the closable to bind
   * @return this (for chaining)
   * @throws NullPointerException if the closable is null
   */
  CompositeTerminable with(AutoCloseable autoCloseable);

  /**
   * Binds all given {@link AutoCloseable} with this composite closable.
   *
   * <p>Note that implementations do not keep track of duplicate contained
   * terminables. If a single {@link AutoCloseable} is added twice, it will be
   * {@link AutoCloseable#close() closed} twice.</p>
   *
   * <p>Ignores null values.</p>
   *
   * @param autoCloseables the closables to bind
   * @return this (for chaining)
   */
  default CompositeTerminable withAll(final AutoCloseable... autoCloseables) {
    for (final AutoCloseable autoCloseable : autoCloseables) {
      if (autoCloseable == null) {
        continue;
      }
      bind(autoCloseable);
    }
    return this;
  }

  /**
   * Binds all given {@link AutoCloseable} with this composite closable.
   *
   * <p>Note that implementations do not keep track of duplicate contained
   * terminables. If a single {@link AutoCloseable} is added twice, it will be
   * {@link AutoCloseable#close() closed} twice.</p>
   *
   * <p>Ignores null values.</p>
   *
   * @param autoCloseables the closables to bind
   * @return this (for chaining)
   */
  default CompositeTerminable withAll(final Iterable<? extends AutoCloseable> autoCloseables) {
    for (final AutoCloseable autoCloseable : autoCloseables) {
      if (autoCloseable == null) {
        continue;
      }
      bind(autoCloseable);
    }
    return this;
  }

  @NotNull
  @Override
  default <T extends AutoCloseable> T bind(@NotNull final T terminable) {
    with(terminable);
    return terminable;
  }

  /**
   * Removes instances which have already been terminated.
   */
  void cleanup();

}
