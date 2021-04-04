/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
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

package dev.demeng.pluginbase.dependencyloader.dependency;

import dev.demeng.pluginbase.dependencyloader.annotations.MavenRepository;
import java.net.URL;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation representing {@link MavenRepository}.
 *
 * @see MavenRepository
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public final class MavenRepositoryInfo {

  /**
   * The URL for this Maven repository.
   */
  @Getter private final @NotNull String url;

  /**
   * Creates a new Maven repository from a {@link URL}.
   *
   * @param url The url
   * @return a new {@link MavenRepositoryInfo}.
   */
  @Contract("_ -> new")
  public static @NotNull MavenRepositoryInfo of(final @NotNull URL url) {
    return new MavenRepositoryInfo(String.valueOf(url));
  }

  /**
   * Creates a new Maven repository from a {@link MavenRepository} annotation.
   *
   * @param repository The annotation
   * @return a new {@link MavenRepositoryInfo}.
   */
  @Contract("_ -> new")
  public static @NotNull MavenRepositoryInfo of(final @NotNull MavenRepository repository) {
    return new MavenRepositoryInfo(repository.value());
  }

  /**
   * Creates a new Maven repository from a string representing a url.
   *
   * @param url the url
   * @return a new {@link MavenRepositoryInfo}.
   */
  @Contract("_ -> new")
  public static @NotNull MavenRepositoryInfo of(final @NotNull String url) {
    return new MavenRepositoryInfo(url);
  }
}
