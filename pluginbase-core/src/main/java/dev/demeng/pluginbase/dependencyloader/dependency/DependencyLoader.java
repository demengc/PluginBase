/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

import dev.demeng.pluginbase.dependencyloader.dependency.builder.DependencyProvider;
import dev.demeng.pluginbase.dependencyloader.generics.TypeDefinition;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract implementation of a dependency loader.
 *
 * @param <T> Type of dependency implementation
 * @see MavenDependencyLoader
 */
public abstract class DependencyLoader<T extends Dependency> implements TypeDefinition<T> {

  /**
   * The default separator used to have compatibility with gradle/Maven relocation.
   */
  public static final @NotNull String DEFAULT_SEPARATOR = "|";

  /**
   * The base path for files in this dependency loader.
   */
  @Getter private final @NotNull Path basePath;

  /**
   * The dependencies that this dependency loader handles.
   */
  @Getter private final @NotNull List<T> dependencies;

  /**
   * The errors that occurred while loading dependencies.
   */
  @Getter private final @NotNull Set<Exception> errors;

  /**
   * Creates a new dependency loader with the specified base path.
   *
   * @param basePath The base path for dependencies.
   */
  protected DependencyLoader(final @NotNull Path basePath) {
    this.basePath = basePath;
    this.dependencies = new ArrayList<>();
    this.errors = new HashSet<>();
  }

  /**
   * Creates a new dependency loader with the specified base path. Storage destination is used as a
   * relative sub directory for dependencies.
   *
   * @param basePath           The base path for dependencies
   * @param storageDestination The relative path to dependencies in this dependency loader
   */
  protected DependencyLoader(
      final @NotNull Path basePath,
      final @NotNull String storageDestination
  ) {
    this(basePath.resolve(storageDestination));
  }

  /**
   * Adds an error to the error set.
   *
   * @param ex The error that occurred
   */
  protected void addError(final @NotNull Exception ex) {
    this.errors.add(ex);
  }

  /**
   * Adds a dependency to be handled by this dependency loader.
   *
   * @param dependency The dependency to add
   */
  public void addDependency(final @NotNull T dependency) {
    this.dependencies.add(dependency);
  }

  /**
   * Used to load dependencies from a class.
   *
   * @param clazz The class to load dependency annotations from
   */
  public abstract void loadDependenciesFrom(@NotNull Class<?> clazz);

  /**
   * Used to load dependencies from a dependency provider.
   *
   * @param dependencyProvider The dependency provider to load dependencies from
   */
  public abstract void loadDependenciesFrom(@NotNull DependencyProvider<T> dependencyProvider);

  /**
   * Downloads handled dependencies.
   */
  public abstract void downloadDependencies();

  /**
   * Loads the dependencies to be used by the plugin.
   *
   * @param classLoader The class loader to add the dependencies to
   */
  public abstract void loadDependencies(@NotNull URLClassLoader classLoader);
}
