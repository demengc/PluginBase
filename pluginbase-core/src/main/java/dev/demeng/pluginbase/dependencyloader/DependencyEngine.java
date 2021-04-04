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

package dev.demeng.pluginbase.dependencyloader;

import dev.demeng.pluginbase.dependencyloader.annotations.LoaderPriority;
import dev.demeng.pluginbase.dependencyloader.dependency.DependencyLoader;
import dev.demeng.pluginbase.dependencyloader.dependency.MavenDependencyLoader;
import dev.demeng.pluginbase.dependencyloader.dependency.builder.DependencyProvider;
import dev.demeng.pluginbase.dependencyloader.relocation.RelocatableDependencyLoader;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A dependency engine is used as the starter point for adding dependencies to be downloaded and
 * loaded at runtime.
 */
public final class DependencyEngine {

  /**
   * The base path for dependencies.
   */
  private final Path basePath;
  /**
   * A map containing all the dependency loaders for this dependency engine.
   */
  private final Map<Class<?>, DependencyLoader<?>> dependencyLoaders;

  /**
   * The errors that occurred during the loading process.
   */
  @Getter private final Set<Throwable> errors;

  /**
   * Creates a new dependency engine with the specified base path.
   *
   * @param basePath The base path for this dependency engine.
   */
  public DependencyEngine(final @NotNull Path basePath) {
    this.basePath = basePath;
    this.dependencyLoaders = new IdentityHashMap<>();
    this.errors = new HashSet<>();
  }

  /**
   * Creates a new Dependency Engine with the specified base path. It includes all the default
   * DependencyLoader's by default.
   *
   * @param basePath The base path for all dependencies to be downloaded.
   * @return A new {@link DependencyEngine}.
   */
  @Contract("_ -> new")
  public static @NotNull DependencyEngine createNew(final @NotNull Path basePath) {
    return DependencyEngine.createNew(basePath, true);
  }

  /**
   * Creates a new Dependency Engine with the specified base path.
   *
   * @param basePath         The base path for all dependencies to be downloaded.
   * @param addDefaultLoader Whether or not to include the default dependency loaders.
   * @return A new {@link DependencyEngine}.
   */
  @Contract("_,_ -> new")
  public static @NotNull DependencyEngine createNew(
      final @NotNull Path basePath,
      final boolean addDefaultLoader
  ) {
    final DependencyEngine engine = new DependencyEngine(basePath);

    if (addDefaultLoader) {
      engine.addDefaultDependencyLoaders();
    }

    return engine;
  }

  private void addDefaultDependencyLoaders() {
    this.addDependencyLoader(new MavenDependencyLoader(this.basePath));
  }

  /**
   * Adds a new dependency loader to the dependency engine. If a duplicate dependency loader is
   * added an error is thrown.
   *
   * @param loader The loader to add to the dependency engine.
   * @return this
   */
  @Contract("_ -> this")
  public @NotNull DependencyEngine addDependencyLoader(final @NotNull DependencyLoader<?> loader) {
    if (this.dependencyLoaders.containsKey(loader.getClass())) {
      throw new IllegalArgumentException(String.format(
          "Loader with type '%s' already loaded.",
          loader.getClass().getName()
      ));
    }

    this.dependencyLoaders.put(loader.getClass(), loader);
    return this;
  }

  /**
   * Adds dependency annotations from a class to the currently loaded dependency loaders in this
   * engine. These are not added retroactively, you need to add the dependency loaders first.
   *
   * @param clazz The class to load the annotations from.
   * @return this
   */
  @Contract("_ -> this")
  public @NotNull DependencyEngine addDependenciesFromClass(final @NotNull Class<?> clazz) {
    this.dependencyLoaders
        .values()
        .forEach(loader -> loader.loadDependenciesFrom(clazz));
    return this;
  }

  /**
   * Adds dependencies to the dependency loaders based on the input dependency provider. These are
   * not added retroactively, you need to add the dependency loaders first.
   *
   * @param provider The provider to add the dependencies from.
   * @return this
   */
  @Contract("_ -> this")
  @SuppressWarnings("unchecked, rawtypes")
  public @NotNull DependencyEngine addDependenciesFromProvider(
      final @NotNull DependencyProvider<?> provider) {
    this.dependencyLoaders
        .values()
        .stream()
        .filter(loader -> loader.getGenericType() == provider.getGenericType())
        .forEach(loader -> loader.loadDependenciesFrom((DependencyProvider) provider));
    return this;
  }


  /**
   * Loads all the dependencies in the specified executor.
   *
   * @param executor An executor to load the dependencies in.
   * @return A completable future that completes when all dependencies are loaded.
   */
  @Contract("_ -> new")
  public @NotNull CompletableFuture<Void> loadDependencies(final @NotNull Executor executor) {
    return CompletableFuture.runAsync(
        () -> this.dependencyLoaders
            .values()
            .stream()
            .sorted(
                Comparator.comparingInt(loader ->
                    loader.getClass()
                        .getAnnotation(LoaderPriority.class)
                        .value())
            )
            .forEach(loader -> {
              if (!this.errors.isEmpty()) {
                return;
              }

              try {
                loader.downloadDependencies();

                this.errors.addAll(loader.getErrors());

                if (this.errors.isEmpty() && loader instanceof RelocatableDependencyLoader) {
                  ((RelocatableDependencyLoader<?>) loader).relocateDependencies();
                  this.errors.addAll(loader.getErrors());
                }

                if (this.errors.isEmpty()) {
                  loader.loadDependencies((URLClassLoader) this.getClass().getClassLoader());
                }
              } catch (final Exception e) {
                this.errors.add(e);
              }
            }),
        executor
    );
  }

  /**
   * Loads all dependencies in {@link ForkJoinPool#commonPool()}.
   *
   * @return A completable future that completes when all dependencies are loaded.
   */
  @Contract("-> new")
  public @NotNull CompletableFuture<Void> loadDependencies() {
    return this.loadDependencies(ForkJoinPool.commonPool());
  }
}
