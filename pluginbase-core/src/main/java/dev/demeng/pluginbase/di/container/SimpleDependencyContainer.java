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

package dev.demeng.pluginbase.di.container;

import dev.demeng.pluginbase.di.annotation.Component;
import dev.demeng.pluginbase.di.exception.DependencyException;
import dev.demeng.pluginbase.di.exception.MissingDependencyException;
import dev.demeng.pluginbase.di.exception.TypeMismatchException;
import dev.demeng.pluginbase.terminable.composite.CompositeTerminable;
import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of {@link DependencyContainer}.
 */
final class SimpleDependencyContainer implements DependencyContainer {

  /**
   * Cache of registered instances.
   */
  private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

  /**
   * Tracks construction path to detect circular dependencies (per-thread).
   */
  private final ThreadLocal<Set<Class<?>>> constructionPath =
      ThreadLocal.withInitial(LinkedHashSet::new);

  /**
   * Manages cleanup of AutoCloseable instances.
   */
  private final CompositeTerminable terminables = CompositeTerminable.create();

  /**
   * Whether this container has been closed.
   */
  private volatile boolean closed = false;

  private SimpleDependencyContainer() {
  }

  @NotNull
  @Override
  @SuppressWarnings("unchecked")
  public <T> DependencyContainer register(@NotNull final T instance) {
    Objects.requireNonNull(instance, "instance");

    // Delegate to explicit type registration using runtime class
    return register((Class<T>) instance.getClass(), instance);
  }

  @NotNull
  @Override
  public <T> DependencyContainer register(
      @NotNull final Class<T> type, @NotNull final T instance) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(instance, "instance");
    checkNotClosed();

    // Validate type assignment
    if (!type.isAssignableFrom(instance.getClass())) {
      throw new TypeMismatchException(type, instance.getClass());
    }

    // Warn if duplicate registration
    if (this.instances.containsKey(type)) {
      System.err.println(
          "WARNING: Overwriting existing registration for type: " + type.getName());
    }

    // Register instance
    this.instances.put(type, instance);

    // Bind AutoCloseable instances for cleanup
    if (instance instanceof AutoCloseable) {
      this.terminables.bind((AutoCloseable) instance);
    }

    return this;
  }

  @NotNull
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getInstance(@NotNull final Class<T> type) {
    Objects.requireNonNull(type, "type");
    checkNotClosed();

    final Set<Class<?>> path = this.constructionPath.get();
    final boolean isTopLevel = path.isEmpty();

    try {
      // Check if instance already exists (registered or created)
      final Object existing = this.instances.get(type);
      if (existing != null) {
        return (T) existing;
      }

      // Check if class is annotated with @Component
      if (!type.isAnnotationPresent(Component.class)) {
        throw new MissingDependencyException(
            "No instance registered for "
                + type.getName()
                + " and class is not annotated with @Component");
      }

      // Auto-create the component (singleton)
      return (T) this.instances.computeIfAbsent(type, k -> createInstance(type, path));

    } finally {
      // Clean up ThreadLocal for top-level calls to prevent memory leaks
      if (isTopLevel) {
        path.clear();
        this.constructionPath.remove();
      }
    }
  }

  @Override
  public boolean hasInstance(@NotNull final Class<?> type) {
    Objects.requireNonNull(type, "type");
    return this.instances.containsKey(type) || type.isAnnotationPresent(Component.class);
  }

  @Override
  public void close() throws Exception {
    if (this.closed) {
      return;
    }

    this.closed = true;

    // Close all AutoCloseable instances
    this.terminables.close();

    // Clear cache
    this.instances.clear();
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  /**
   * Creates a new instance of the given type by auto-injecting dependencies.
   *
   * @param type The type to instantiate
   * @param path The current construction path (for circular dependency detection)
   * @param <T>  The type parameter
   * @return A new instance
   */
  private <T> T createInstance(final Class<T> type, final Set<Class<?>> path) {

    // Check for circular dependency
    if (!path.add(type)) {
      final String cycle =
          String.join(" -> ", path.stream().map(Class::getSimpleName).toArray(String[]::new));
      throw new DependencyException(
          "Circular dependency detected: " + cycle + " -> " + type.getSimpleName());
    }

    try {
      // Find constructor to use (Lombok support)
      final Constructor<T> constructor = findConstructor(type);

      // Make constructor accessible
      constructor.setAccessible(true);

      // Resolve dependencies from container
      final Class<?>[] parameterTypes = constructor.getParameterTypes();
      final Object[] dependencies = new Object[parameterTypes.length];

      for (int i = 0; i < parameterTypes.length; i++) {
        dependencies[i] = getInstance(parameterTypes[i]);
      }

      // Create instance
      final T instance = constructor.newInstance(dependencies);

      // Bind AutoCloseable instances to the terminable registry
      if (instance instanceof AutoCloseable) {
        this.terminables.bind((AutoCloseable) instance);
      }

      return instance;

    } catch (final DependencyException e) {
      // Re-throw DI exceptions as-is
      throw e;
    } catch (final Exception e) {
      throw new DependencyException("Failed to create instance of " + type.getName(), e);
    } finally {
      path.remove(type);
    }
  }

  /**
   * Finds the constructor to use for dependency injection.
   *
   * <p>Uses the constructor with the most parameters (supports Lombok's
   * {@code @RequiredArgsConstructor} and {@code @AllArgsConstructor}).
   *
   * @param type The type to find a constructor for
   * @param <T>  The type parameter
   * @return The constructor to use
   */
  @SuppressWarnings("unchecked")
  private <T> Constructor<T> findConstructor(final Class<T> type) {
    final Constructor<?>[] constructors = type.getDeclaredConstructors();

    if (constructors.length == 0) {
      throw new DependencyException("No constructors found for " + type.getName());
    }

    // Use the only constructor if there's exactly one
    if (constructors.length == 1) {
      return (Constructor<T>) constructors[0];
    }

    // Multiple constructors - choose the one with the most parameters (Lombok support)
    Constructor<?> longestConstructor = constructors[0];
    for (final Constructor<?> constructor : constructors) {
      if (constructor.getParameterCount() > longestConstructor.getParameterCount()) {
        longestConstructor = constructor;
      }
    }

    return (Constructor<T>) longestConstructor;
  }

  /**
   * Checks if the container is closed and throws an exception if it is.
   */
  private void checkNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("DependencyContainer has been closed");
    }
  }

  /**
   * Creates a new builder for {@link SimpleDependencyContainer}.
   *
   * @return A new builder instance
   */
  @NotNull
  static Builder builder() {
    return new BuilderImpl();
  }

  /**
   * Builder implementation for creating {@link SimpleDependencyContainer}.
   */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class BuilderImpl implements Builder {

    private final SimpleDependencyContainer container = new SimpleDependencyContainer();

    @NotNull
    @Override
    public Builder register(@NotNull final Object instance) {
      this.container.register(instance);
      return this;
    }

    @NotNull
    @Override
    public <T> Builder register(@NotNull final Class<T> type, @NotNull final T instance) {
      this.container.register(type, instance);
      return this;
    }

    @NotNull
    @Override
    public DependencyContainer build() {
      return this.container;
    }
  }
}
