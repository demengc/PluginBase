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

import dev.demeng.pluginbase.terminable.Terminable;
import org.jetbrains.annotations.NotNull;

/**
 * A dependency injection container that manages singleton instances with auto-injection.
 *
 * <p>The container supports two modes:
 *
 * <ul>
 *   <li><b>Manual registration:</b> Register pre-created instances that you control
 *   <li><b>Auto-creation:</b> Classes annotated with {@code @Component} are automatically created
 *       with dependencies injected
 * </ul>
 *
 * <p>All instances are singletons - only one instance per type is created and cached. Instances
 * implementing {@link AutoCloseable} are automatically cleaned up when the container is closed.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Manually create and register core services
 * DatabaseConnection db = new DatabaseConnection(plugin);
 * CacheService cache = new CacheService();
 *
 * DependencyContainer container = DependencyContainer.builder()
 *     .register(plugin)
 *     .register(db)
 *     .register(cache)
 *     .build();
 *
 * // Classes marked with @Component are auto-created with dependencies injected
 * @Component
 * public class PlayerDataManager {
 *   private final DatabaseConnection db;
 *   private final CacheService cache;
 *
 *   public PlayerDataManager(DatabaseConnection db, CacheService cache) {
 *     this.db = db;
 *     this.cache = cache;
 *   }
 * }
 *
 * // Auto-creates PlayerDataManager with db and cache injected from registry
 * PlayerDataManager manager = container.getInstance(PlayerDataManager.class);
 * }</pre>
 */
public interface DependencyContainer extends Terminable {

  /**
   * Creates a new builder for configuring a dependency container.
   *
   * @return A new builder instance
   */
  @NotNull
  static Builder builder() {
    return SimpleDependencyContainer.builder();
  }

  /**
   * Registers an instance in the container.
   *
   * <p>The instance will be registered under its actual runtime class type and returned whenever
   * that type is requested from the container.
   *
   * @param instance The instance to register
   * @param <T>      The type parameter
   * @return This container for method chaining
   */
  @NotNull
  <T> DependencyContainer register(@NotNull T instance);

  /**
   * Registers an instance in the container with an explicit type.
   *
   * <p>This allows registering instances by their interface or abstract class rather than just
   * their concrete implementation type, enabling programming to interfaces and dependency inversion
   * principle.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * // Register by interface
   * PlayerService service = new PlayerServiceImpl();
   * container.register(PlayerService.class, service);
   *
   * // Can now retrieve by interface in @Component classes:
   * @Component
   * public class GameManager {
   *   private final PlayerService playerService;
   *
   *   public GameManager(PlayerService playerService) {
   *     this.playerService = playerService;
   *   }
   * }
   * }</pre>
   *
   * @param type     The type to register the instance under (interface or class)
   * @param instance The instance to register
   * @param <T>      The type parameter
   * @return This container for method chaining
   * @throws dev.demeng.pluginbase.di.exception.TypeMismatchException if instance is not assignable
   */
  @NotNull
  <T> DependencyContainer register(@NotNull Class<T> type, @NotNull T instance);

  /**
   * Gets an instance of the specified type from the container.
   *
   * <p>If the instance is already registered or created, it returns the cached instance. If the
   * class is annotated with {@code @Component}, it will be auto-created with dependencies injected
   * from the container.
   *
   * @param type The class type to get an instance of
   * @param <T>  The type parameter
   * @return The instance (either registered or auto-created)
   * @throws dev.demeng.pluginbase.di.exception.MissingDependencyException if no instance is
   *                                                                       registered and class is
   *                                                                       not annotated with
   *                                                                       @Component
   * @throws dev.demeng.pluginbase.di.exception.DependencyException        if a circular dependency
   *                                                                       is detected or instance
   *                                                                       creation fails
   */
  @NotNull
  <T> T getInstance(@NotNull Class<T> type);

  /**
   * Checks if an instance is registered for the given type.
   *
   * @param type The class type to check
   * @return True if an instance is registered, false otherwise
   */
  boolean hasInstance(@NotNull Class<?> type);

  /**
   * Builder for creating and configuring a {@link DependencyContainer}.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * DatabaseConnection db = new DatabaseConnection(plugin);
   * CacheService cache = new CacheService();
   *
   * DependencyContainer container = DependencyContainer.builder()
   *     .register(plugin)  // Register plugin instance
   *     .register(db)      // Register database
   *     .register(cache)   // Register cache
   *     .build();
   *
   * // Classes with @Component are auto-created
   * PlayerDataManager manager = container.getInstance(PlayerDataManager.class);
   * }</pre>
   */
  interface Builder {

    /**
     * Registers an instance in the container.
     *
     * <p>The instance will be stored and can be retrieved later via {@link #getInstance(Class)}.
     * It
     * will also be injected into {@code @Component} classes that depend on it.
     *
     * @param instance The instance to register
     * @return This builder for method chaining
     */
    @NotNull
    Builder register(@NotNull Object instance);

    /**
     * Registers an instance in the container with an explicit type.
     *
     * <p>This allows registering instances by their interface or abstract class rather than just
     * their concrete implementation type.
     *
     * <p>Example:
     *
     * <pre>{@code
     * DatabaseConnection db = new MySQLConnection(plugin);
     * CacheService cache = new RedisCacheService();
     *
     * DependencyContainer container = DependencyContainer.builder()
     *     .register(plugin)  // Concrete type
     *     .register(DatabaseConnection.class, db)  // Interface
     *     .register(CacheService.class, cache)     // Interface
     *     .build();
     * }</pre>
     *
     * @param type     The type to register the instance under
     * @param instance The instance to register
     * @param <T>      The type parameter
     * @return This builder for method chaining
     * @throws dev.demeng.pluginbase.di.exception.TypeMismatchException if instance is not
     */
    @NotNull
    <T> Builder register(@NotNull Class<T> type, @NotNull T instance);

    /**
     * Builds and returns the configured dependency container.
     *
     * @return A new {@link DependencyContainer} instance
     */
    @NotNull
    DependencyContainer build();
  }
}
