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

package dev.demeng.pluginbase;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Utility for converting to and from trimmed UUIDs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrimmedUuids {

  /**
   * Returns a UUID string without dashes (trimmed).
   *
   * @param uuid The UUID
   * @return The trimmed UUID
   */
  public static String toString(final UUID uuid) {
    return (digits(uuid.getMostSignificantBits() >> 32, 8)
        + digits(uuid.getMostSignificantBits() >> 16, 4)
        + digits(uuid.getMostSignificantBits(), 4)
        + digits(uuid.getLeastSignificantBits() >> 48, 4)
        + digits(uuid.getLeastSignificantBits(), 12));
  }

  /**
   * Parses a UUID from a trimmed string.
   *
   * @param string The trimmed UUID
   * @return The UUID
   */
  public static UUID fromString(final String string) throws IllegalArgumentException {

    if (string.length() != 32) {
      throw new IllegalArgumentException("Invalid length '" + string.length() + "': " + string);
    }

    try {
      return new UUID(
          Long.parseUnsignedLong(string.substring(0, 16), 16),
          Long.parseUnsignedLong(string.substring(16), 16));
    } catch (final NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid UUID string: " + string, ex);
    }
  }

  private static String digits(final long val, final int digits) {
    final long hi = 1L << (digits * 4);
    return Long.toHexString(hi | (val & (hi - 1))).substring(1);
  }
}
