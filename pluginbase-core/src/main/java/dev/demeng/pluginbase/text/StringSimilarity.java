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

package dev.demeng.pluginbase.text;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for calculating string similarity using various algorithms. Useful for implementing
 * fuzzy matching, command suggestions, and typo correction.
 */
@UtilityClass
public class StringSimilarity {

  /**
   * Calculates the Levenshtein distance between two strings. The Levenshtein distance is the
   * minimum number of single-character edits (insertions, deletions, or substitutions) required to
   * change one string into the other.
   *
   * @param s1 The first string
   * @param s2 The second string
   * @return The Levenshtein distance between the two strings
   */
  public static int levenshteinDistance(@NotNull final String s1, @NotNull final String s2) {
    final String str1 = s1.toLowerCase();
    final String str2 = s2.toLowerCase();

    final int len1 = str1.length();
    final int len2 = str2.length();

    if (len1 == 0) {
      return len2;
    }

    if (len2 == 0) {
      return len1;
    }

    final int[][] dp = new int[len1 + 1][len2 + 1];

    for (int i = 0; i <= len1; i++) {
      dp[i][0] = i;
    }

    for (int j = 0; j <= len2; j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i <= len1; i++) {
      for (int j = 1; j <= len2; j++) {
        final int cost = str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1;

        dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
      }
    }

    return dp[len1][len2];
  }

  /**
   * Calculates the similarity percentage between two strings based on Levenshtein distance. Returns
   * a value between 0.0 (completely different) and 1.0 (identical).
   *
   * @param s1 The first string
   * @param s2 The second string
   * @return A similarity score between 0.0 and 1.0
   */
  public static double similarity(@NotNull final String s1, @NotNull final String s2) {
    final int distance = levenshteinDistance(s1, s2);
    final int maxLength = Math.max(s1.length(), s2.length());

    if (maxLength == 0) {
      return 1.0;
    }

    return 1.0 - ((double) distance / maxLength);
  }

  /**
   * Checks if two strings are similar enough based on a threshold. Useful for determining if a
   * string is "close enough" to another for fuzzy matching purposes.
   *
   * @param s1 The first string
   * @param s2 The second string
   * @param threshold The minimum similarity score (0.0 to 1.0) required to consider strings similar
   * @return true if the similarity score is greater than or equal to the threshold
   */
  public static boolean isSimilar(
      @NotNull final String s1, @NotNull final String s2, final double threshold) {
    return similarity(s1, s2) >= threshold;
  }
}
