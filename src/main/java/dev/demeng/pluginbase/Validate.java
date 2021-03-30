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

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utlity class for checking and validating objects.
 */
@SuppressWarnings("unused")
public class Validate {

  private Validate() {
    throw new IllegalStateException("Utility class");
  }

  // ---------------------------------------------------------------------------------
  // GENERAL
  // ---------------------------------------------------------------------------------

  /**
   * Simple method to check if a class exists.
   *
   * @param className The class's package and name (Example: dev.demeng.pluginbase.Validate)
   * @return The actul class if the class exists, null otherwise
   */
  @Nullable
  public static Class<?> checkClass(@NonNull String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
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
  public static Integer checkInt(@NonNull String str) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a long.
   *
   * @param str The string to check
   * @return The actual long if the string is an long, null otherwise
   */
  @Nullable
  public static Long checkLong(@NonNull String str) {
    try {
      return Long.parseLong(str);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a double.
   *
   * @param str The string to check
   * @return The actual double if the string is an double, null otherwise
   */
  @Nullable
  public static Double checkDouble(@NonNull String str) {
    try {
      return Double.parseDouble(str);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a float.
   *
   * @param str The string to check
   * @return The actual float if the string is an double, null otherwise
   */
  @Nullable
  public static Float checkFloat(@NonNull String str) {
    try {
      return Float.parseFloat(str);
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
