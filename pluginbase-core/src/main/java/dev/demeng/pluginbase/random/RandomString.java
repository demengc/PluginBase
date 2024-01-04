/*
 * MIT License
 *
 * Copyright (c) 2024 Demeng Chen
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

package dev.demeng.pluginbase.random;

import java.util.concurrent.ThreadLocalRandom;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for generating random strings with customizable length.
 *
 * <p><b>WARNING:</b> IDs generated with this utility are NOT guaranteed to be random, though the
 * chance of getting a duplicate ID becomes increasingly unlikely as you increase the length.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomString {

  public static final char[] ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  public static final char[] ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz".toCharArray();
  public static final char[] NUMBERS = "0123456789".toCharArray();
  public static final char[] ALPHANUMERIC_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
  public static final char[] ALPHANUMERIC_LOWER = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  public static final char[] ALPHANUMERIC_MIXED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

  /**
   * Generates a new "unique" string identifier with the specified length.
   *
   * @param length The length of the ID
   * @return The generated ID
   */
  @NotNull
  public static String generate(
      final int length,
      final char[] chars) {

    final StringBuilder sb = new StringBuilder(length);

    for (int n = 0; n < length; n++) {
      sb.append(chars[(ThreadLocalRandom.current().nextInt(chars.length))]);
    }

    return sb.toString();
  }
}
