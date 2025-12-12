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
 * Exception thrown when attempting to register an instance with a type that is not assignable from
 * the instance's actual class.
 *
 * <p>This occurs when trying to register an instance with an incompatible explicit type parameter.
 *
 * @since 1.36.0
 */
public class TypeMismatchException extends DependencyException {

  /**
   * Creates a new type mismatch exception with details about the expected and actual types.
   *
   * @param expected The expected type (what was specified in register)
   * @param actual   The actual type of the instance
   */
  public TypeMismatchException(@NotNull final Class<?> expected, @NotNull final Class<?> actual) {
    super(
        "Instance type "
            + actual.getName()
            + " is not assignable to "
            + expected.getName()
            + ". Ensure the instance implements or extends the specified type.");
  }
}
