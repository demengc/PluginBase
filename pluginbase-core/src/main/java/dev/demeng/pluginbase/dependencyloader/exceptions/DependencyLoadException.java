/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
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

import dev.demeng.pluginbase.dependencyloader.dependency.Dependency;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Used when there is an error loading a dependency.
 */
public final class DependencyLoadException extends RuntimeException {

  /**
   * The dependency that had an error while loading.
   */
  @Getter private final transient @NotNull Dependency dependency;

  /**
   * Creates a new dependency load exception.
   *
   * @param dependency The dependency that had an error while loading.
   * @param message    The message to pass.
   */
  public DependencyLoadException(
      final @NotNull Dependency dependency,
      final @NotNull String message
  ) {
    super(message);
    this.dependency = dependency;
  }

  /**
   * Creates a new dependency load exception.
   *
   * @param dependency The dependency that had an error while loading.
   * @param cause      The cause of the error.
   */
  public DependencyLoadException(
      final @NotNull Dependency dependency,
      final @NotNull Throwable cause
  ) {
    super(cause);
    this.dependency = dependency;
  }
}
