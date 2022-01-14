/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Justin Heflin
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
 *
 */

package dev.demeng.pluginbase.dependencyloader.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * When a dependency isn't able to process this error is thrown.
 */
public final class InvalidDependencyException extends RuntimeException {

  /**
   * A basic error that indicates there was an error parsing a dependency.
   */
  public InvalidDependencyException() {
    super();
  }

  /**
   * Creates a new invalid dependency exception.
   *
   * @param message The message to pass
   * @param cause   The cause of the error
   */
  public InvalidDependencyException(
      final @NotNull String message,
      final @NotNull Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new invalid dependency exception.
   *
   * @param cause The cause of the error
   */
  public InvalidDependencyException(final @NotNull Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new invalid dependency exception.
   *
   * @param message The message to pass
   */
  public InvalidDependencyException(final @NotNull String message) {
    super(message);
  }
}
