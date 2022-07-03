/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

package dev.demeng.pluginbase.placeholders;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link DynamicPlaceholders}, allowing you to quickly add and set placeholders
 * as an object. Also permits setting placeholders into nullable objects (which returns a default,
 * non-null object).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Placeholders implements DynamicPlaceholders {

  /**
   * A map of all the placeholders, with the key being the string to replace and the value being the
   * string to replace with.
   */
  @NotNull @Getter private final Map<String, String> replacements = new HashMap<>();

  /**
   * Creates a new placeholders object from an existing replacements map.
   *
   * @param replacements The map of all placeholders
   * @return The placeholders object
   */
  @NotNull
  public static Placeholders of(@NotNull final Map<String, String> replacements) {
    final Placeholders obj = new Placeholders();
    obj.getReplacements().putAll(replacements);
    return obj;
  }

  /**
   * Creates a new placeholders object from an existing Placeholders object.
   *
   * @param placeholders The placeholdesr to clone
   * @return The placeholders object (cloned)
   */
  public static Placeholders of(@NotNull final Placeholders placeholders) {
    return of(placeholders.getReplacements());
  }

  /**
   * Creates a new placeholders object with the given placeholder.
   *
   * @param toReplace   The string to replace
   * @param replaceWith The string to replace with
   * @return The placeholders object
   */
  @NotNull
  public static Placeholders of(
      @NotNull final String toReplace,
      @NotNull final String replaceWith) {
    final Placeholders obj = new Placeholders();
    obj.getReplacements().put(toReplace, replaceWith);
    return obj;
  }

  /**
   * Adds a new placeholder to hte placeholders object.
   *
   * @param toReplace   The string to replace
   * @param replaceWith The string to replace with
   * @return this
   */
  @NotNull
  public Placeholders add(@NotNull final String toReplace, @NotNull final String replaceWith) {
    replacements.put(toReplace, replaceWith);
    return this;
  }

  @Override
  public @NotNull String setPlaceholders(@NotNull final String str) {

    String replaced = str;

    for (final Map.Entry<String, String> entry : replacements.entrySet()) {
      replaced = replaced.replace(entry.getKey(), entry.getValue());
    }

    return replaced;
  }

  /**
   * Creates an empty placeholders object.
   *
   * @return An empty placeholders object
   */
  @NotNull
  public static Placeholders empty() {
    return new Placeholders();
  }
}
