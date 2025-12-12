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

package dev.demeng.pluginbase.di;

import dev.demeng.pluginbase.di.container.DependencyContainer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for creating and working with dependency injection containers.
 *
 * <p>This class provides convenient static methods for creating {@link DependencyContainer}
 * instances.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Create services
 * DatabaseConnection db = new MySQLConnection(plugin);
 * CacheService cache = new RedisCacheService();
 *
 * // Create a container with interface-based registration
 * DependencyContainer container = DependencyInjection.builder()
 *     .register(plugin)  // Register by concrete type
 *     .register(DatabaseConnection.class, db)  // Register by interface
 *     .register(CacheService.class, cache)     // Register by interface
 *     .build();
 *
 * // Classes annotated with @Component will be auto-created:
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
 * // Retrieve auto-created components
 * PlayerDataManager manager = container.getInstance(PlayerDataManager.class);
 *
 * // Or create an empty container
 * DependencyContainer container = DependencyInjection.create();
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DependencyInjection {

  /**
   * Creates a new dependency container builder.
   *
   * <p>Use the builder to configure bindings, register types, and register singleton instances
   * before building the container.
   *
   * @return A new {@link DependencyContainer.Builder} instance
   */
  @NotNull
  public static DependencyContainer.Builder builder() {
    return DependencyContainer.builder();
  }

  /**
   * Creates a new empty dependency container.
   *
   * <p>The container will have no pre-configured bindings. You can register dependencies
   * dynamically using the container's registration methods.
   *
   * @return A new {@link DependencyContainer} instance
   */
  @NotNull
  public static DependencyContainer create() {
    return builder().build();
  }
}
