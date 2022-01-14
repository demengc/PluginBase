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

import dev.demeng.pluginbase.dependencyloader.dependency.DependencyLoader;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link DependencyLoader} that supports relocations.
 *
 * @param <T> A Relocatable Dependency to handle.
 */
public abstract class RelocatableDependencyLoader<T extends RelocatableDependency> extends
    DependencyLoader<T> {

  /**
   * Creates a new dependency loader that supports relocations with the specified base path.
   *
   * @param basePath The base path for dependencies in this dependency loader
   */
  protected RelocatableDependencyLoader(final @NotNull Path basePath) {
    super(basePath);
  }

  /**
   * Creates a new dependency loader that supports relocations with the specified base path. The
   * storage destination is a relative sub-directory for dependencies specific to this loader.
   *
   * @param basePath           The base path for this dependency loader
   * @param storageDestination The relative sub-directory for dependencies
   */
  protected RelocatableDependencyLoader(
      final @NotNull Path basePath,
      final @NotNull String storageDestination
  ) {
    super(basePath, storageDestination);
  }

  /**
   * Relocates dependencies based on provided relocations.
   */
  public abstract void relocateDependencies();
}
