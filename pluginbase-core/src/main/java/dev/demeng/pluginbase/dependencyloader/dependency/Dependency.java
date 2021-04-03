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

import org.jetbrains.annotations.NotNull;

/**
 * Base dependency abstraction used to define basic information for dependency retrieval.
 */
public interface Dependency {

  /**
   * If there is an issue downloading the dependency this is used to get the manual download
   * location.
   *
   * @return The manual download url.
   */
  @NotNull String getManualDownloadString();

  /**
   * Used to get the download url of a dependency.
   *
   * @return The relative download url.
   */
  @NotNull String getRelativeDownloadString();

  /**
   * Used to get where the dependency should be stored on download. The path is relative.
   *
   * @return The downloaded location of this dependency.
   */
  @NotNull String getDownloadedFileName();

  /**
   * Used to get the name of the dependency for logging information.
   *
   * @return The name of this dependency.
   */
  @NotNull String getName();

  /**
   * Set where or not this dependency has been loaded.
   *
   * @param loaded Whether or not this dependency has been loaded.
   */
  void setLoaded(boolean loaded);

  /**
   * Whether or not this dependency has been loaded into the class path.
   *
   * @return true if it has been loaded already.
   */
  boolean isLoaded();
}
