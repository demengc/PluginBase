/*
 * MIT License
 *
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) lucko/helper contributors
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

package dev.demeng.pluginbase;

import dev.demeng.pluginbase.plugin.BaseManager;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

/** Utility class for interacting with the Bukkit {@link org.bukkit.plugin.ServicesManager}. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Services {

  /**
   * Loads a service instance, throwing a {@link IllegalStateException} if no registration is
   * present.
   *
   * @param clazz the service class
   * @param <T> the service class type
   * @return the service instance
   */
  @NotNull
  public static <T> T load(@NotNull final Class<T> clazz) {
    Objects.requireNonNull(clazz, "clazz");
    return get(clazz)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No registration present for service '" + clazz.getName() + "'"));
  }

  /**
   * Loads a service instance
   *
   * @param clazz the service class
   * @param <T> the service class type
   * @return the service instance, as an optional
   */
  @NotNull
  public static <T> Optional<T> get(@NotNull final Class<T> clazz) {
    Objects.requireNonNull(clazz, "clazz");
    final RegisteredServiceProvider<T> registration =
        Bukkit.getServicesManager().getRegistration(clazz);
    if (registration == null) {
      return Optional.empty();
    }
    return Optional.of(registration.getProvider());
  }

  /**
   * Provides a service.
   *
   * @param clazz the service class
   * @param instance the service instance
   * @param plugin the plugin to register the service to
   * @param priority the priority to register the service instance at
   * @param <T> the service class type
   * @return the same service instance
   */
  @NotNull
  public static <T> T provide(
      @NotNull final Class<T> clazz,
      @NotNull final T instance,
      @NotNull final Plugin plugin,
      @NotNull final ServicePriority priority) {
    Objects.requireNonNull(clazz, "clazz");
    Objects.requireNonNull(instance, "instance");
    Objects.requireNonNull(plugin, "plugin");
    Objects.requireNonNull(priority, "priority");
    Bukkit.getServicesManager().register(clazz, instance, plugin, priority);
    return instance;
  }

  /**
   * Provides a service.
   *
   * @param clazz the service class
   * @param instance the service instance
   * @param priority the priority to register the service instance at
   * @param <T> the service class type
   * @return the same service instance
   */
  @NotNull
  public static <T> T provide(
      @NotNull final Class<T> clazz,
      @NotNull final T instance,
      @NotNull final ServicePriority priority) {
    return provide(clazz, instance, BaseManager.getPlugin(), priority);
  }

  /**
   * Provides a service.
   *
   * @param clazz the service class
   * @param instance the service instance
   * @param <T> the service class type
   * @return the same service instance
   */
  @NotNull
  public static <T> T provide(@NotNull final Class<T> clazz, @NotNull final T instance) {
    return provide(clazz, instance, ServicePriority.Normal);
  }
}
