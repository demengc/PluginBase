/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Justin Heflin
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
 *
 */

package dev.demeng.pluginbase.dependencyloader.relocation;

import dev.demeng.pluginbase.dependencyloader.annotations.Relocation;
import dev.demeng.pluginbase.dependencyloader.dependency.DependencyLoader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation representing {@link Relocation}.
 *
 * @see Relocation
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public final class RelocationInfo {

  /**
   * The package to search for.
   */
  @Getter private final @NotNull String from;

  /**
   * The package to place.
   */
  @Getter private final @NotNull String to;

  /**
   * The separate used to indicate package names.
   */
  @Getter private final @NotNull String separator;

  /**
   * Creates a new relocation based off of the passed in information.
   *
   * @param from      The package to find
   * @param to        The package to replace with
   * @param separator The separator used to indicate package names
   * @return A new {@link RelocationInfo} instance
   */
  @Contract("_,_,_ -> new")
  public static @NotNull RelocationInfo of(
      final @NotNull String from,
      final @NotNull String to,
      final @NotNull String separator
  ) {
    return new RelocationInfo(from, to, separator);
  }

  /**
   * Creates a new relocation based off of the passed in information. Uses the {@link
   * DependencyLoader#DEFAULT_SEPARATOR} as the separator.
   *
   * @param from The package to find
   * @param to   The package to replace with
   * @return A new {@link RelocationInfo} instance
   */
  @Contract("_,_-> new")
  public static @NotNull RelocationInfo of(
      final @NotNull String from,
      final @NotNull String to
  ) {
    return new RelocationInfo(from, to, DependencyLoader.DEFAULT_SEPARATOR);
  }

  /**
   * Creates a new relocation from the passed in {@link Relocation} annotation.
   *
   * @param relocation The {@link Relocation} annotation
   * @return A new {@link RelocationInfo} instance
   */
  @Contract("_ -> new")
  public static @NotNull RelocationInfo of(final @NotNull Relocation relocation) {
    return new RelocationInfo(relocation.from(), relocation.to(), relocation.separator());
  }
}
