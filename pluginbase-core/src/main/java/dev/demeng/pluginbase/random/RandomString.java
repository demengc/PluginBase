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

package dev.demeng.pluginbase.random;

import java.security.SecureRandom;
import java.util.Random;
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

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      + "abcdefghijklmnopqrstuvwxyz"
      + "0123456789"
      + "-_";

  private static final Random RANDOM = new Random();
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * Generates a new "unique" string identifier with the specified length.
   *
   * @param random The instance of {@link Random} to use
   * @param length The length of the ID
   * @return The generated ID
   */
  @NotNull
  public static String generateId(@NotNull final Random random, final int length) {

    final StringBuilder sb = new StringBuilder(length);

    for (int n = 0; n < length; n++) {
      sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }

    return sb.toString();
  }

  /**
   * Generates a new "unique" string identifier with the specified length INSECURELY. If this ID
   * should be kept secret, use {@link #generateIdSecure(int)}.
   *
   * @param length The length of the ID
   * @return The generated ID
   */
  @NotNull
  public static String generateId(final int length) {
    return generateId(RANDOM, length);
  }

  /**
   * Generates a new "unique" string identifier with the specified length securely using {@link
   * SecureRandom}. Note that although this makes the ID more secure, it is slower than {@link
   * #generateId(int)}.
   *
   * @param length The length of the ID
   * @return The generated ID
   */
  @NotNull
  public static String generateIdSecure(final int length) {
    return generateId(SECURE_RANDOM, length);
  }
}
