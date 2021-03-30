/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2019 Matt
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

package dev.demeng.pluginbase.command.objects;

import lombok.Getter;

/**
 * Essentially a pair of objects- a command argument's resolved/mapped value, and the argument
 * argument name.
 */
public final class TypeResult {

  @Getter private final Object resolvedValue;
  @Getter private final String argumentName;

  /**
   * Creates a new type result that has already been resolved..
   *
   * @param resolvedValue The resolved value
   * @param argumentName  The argument name
   */
  public TypeResult(Object resolvedValue, final Object argumentName) {
    this.resolvedValue = resolvedValue;
    this.argumentName = String.valueOf(argumentName);
  }

  /**
   * Creates a new type result with a null resolve value.
   *
   * @param argumentName The argument name
   */
  public TypeResult(final Object argumentName) {
    this(null, argumentName);
  }
}
