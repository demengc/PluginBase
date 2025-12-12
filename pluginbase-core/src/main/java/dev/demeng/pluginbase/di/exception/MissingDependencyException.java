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

package dev.demeng.pluginbase.di.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a required dependency cannot be resolved.
 *
 * <p>This typically occurs when:
 *
 * <ul>
 *   <li>An interface or abstract class has no registered binding
 *   <li>A required type has not been registered in the container
 *   <li>A dependency is missing from the dependency graph
 * </ul>
 */
public class MissingDependencyException extends DependencyException {

  /**
   * Creates a new missing dependency exception.
   *
   * @param type The type that could not be resolved
   */
  public MissingDependencyException(@NotNull final Class<?> type) {
    super("No binding found for: " + type.getName());
  }

  /**
   * Creates a new missing dependency exception with a custom message.
   *
   * @param message The error message
   */
  public MissingDependencyException(@NotNull final String message) {
    super(message);
  }
}
