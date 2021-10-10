/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for checking and validating objects.
 */
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validate {

  // ---------------------------------------------------------------------------------
  // GENERAL
  // ---------------------------------------------------------------------------------

  /**
   * Simple method to check if a class exists.
   *
   * @param className The class's package and name (Example: dev.demeng.pluginbase.Validate)
   * @return The actual class if the class exists, null otherwise
   */
  @Nullable
  public static Class<?> checkClass(@NotNull final String className) {
    try {
      return Class.forName(className, false, Validate.class.getClassLoader());
    } catch (final ClassNotFoundException ex) {
      return null;
    }
  }

  // ---------------------------------------------------------------------------------
  // NUMBER CHECK
  // ---------------------------------------------------------------------------------

  /**
   * Checks if the provided string is an integer.
   *
   * @param str The string to check
   * @return The actual integer if the string is an integer, null otherwise
   */
  @Nullable
  public static Integer checkInt(@NotNull final String str) {
    try {
      return Integer.parseInt(str);
    } catch (final NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a long.
   *
   * @param str The string to check
   * @return The actual long if the string is a long, null otherwise
   */
  @Nullable
  public static Long checkLong(@NotNull final String str) {
    try {
      return Long.parseLong(str);
    } catch (final NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a double.
   *
   * @param str The string to check
   * @return The actual double if the string is a double, null otherwise
   */
  @Nullable
  public static Double checkDouble(@NotNull final String str) {
    try {
      return Double.parseDouble(str);
    } catch (final NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a float.
   *
   * @param str The string to check
   * @return The actual float if the string is a double, null otherwise
   */
  @Nullable
  public static Float checkFloat(@NotNull final String str) {
    try {
      return Float.parseFloat(str);
    } catch (final NumberFormatException ex) {
      return null;
    }
  }
}
