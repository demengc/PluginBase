/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2019 Matthew Harris
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

package dev.demeng.pluginbase.libby.relocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Relocations are used to describe a search and replace pattern for renaming packages in a library
 * jar for the purpose of preventing namespace conflicts with other plugins that bundle their own
 * version of the same library.
 *
 * <p>Use the "{}" character to prevent the string literal from being relocated on compile.
 */
public class Relocation {

  private final String pattern;
  private final String relocatedPattern;
  private final Collection<String> includes;
  private final Collection<String> excludes;

  /**
   * Creates a new relocation.
   *
   * @param pattern Search pattern
   * @param relocatedPattern Replacement pattern
   * @param includes Classes and resources to include
   * @param excludes Classes and resources to exclude
   */
  public Relocation(
      String pattern,
      String relocatedPattern,
      Collection<String> includes,
      Collection<String> excludes) {
    this.pattern = Objects.requireNonNull(pattern, "pattern").replace("{}", ".");
    this.relocatedPattern =
        Objects.requireNonNull(relocatedPattern, "relocatedPattern").replace("{}", ".");
    this.includes =
        includes != null
            ? Collections.unmodifiableList(new LinkedList<>(includes))
            : Collections.emptyList();
    this.excludes =
        excludes != null
            ? Collections.unmodifiableList(new LinkedList<>(excludes))
            : Collections.emptyList();
  }

  /**
   * Creates a new relocation with empty includes and excludes.
   *
   * @param pattern Search pattern
   * @param relocatedPattern Replacement pattern
   */
  public Relocation(String pattern, String relocatedPattern) {
    this(pattern, relocatedPattern, null, null);
  }

  /**
   * Gets the search pattern.
   *
   * @return Pattern to search
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * Gets the replacement pattern.
   *
   * @return Pattern to replace with
   */
  public String getRelocatedPattern() {
    return relocatedPattern;
  }

  /**
   * Gets included classes and resources.
   *
   * @return Classes and resources to include
   */
  public Collection<String> getIncludes() {
    return includes;
  }

  /**
   * Gets excluded classes and resources.
   *
   * @return Classes and resources to exclude
   */
  public Collection<String> getExcludes() {
    return excludes;
  }
}
