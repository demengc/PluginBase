/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Utility for operations involving multiple players. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Players {

  /**
   * Gets all players on the server.
   *
   * @return All players on the server
   */
  @NotNull
  public static Collection<Player> all() {
    //noinspection unchecked
    return (Collection<Player>) Bukkit.getOnlinePlayers();
  }

  /**
   * Gets a stream of all players on the server.
   *
   * @return A stream of all players on the server
   */
  @NotNull
  public static Stream<Player> stream() {
    return all().stream();
  }

  /**
   * Applies a given action to all players on the server.
   *
   * @param consumer The action to apply
   */
  public static void forEach(@NotNull final Consumer<Player> consumer) {
    all().forEach(consumer);
  }

  /**
   * Gets a stream of all players within a given radius of a point.
   *
   * @param center The point
   * @param radius The radius
   * @return A stream of players
   */
  @NotNull
  public static Stream<Player> streamInRange(@NotNull final Location center, final double radius) {
    Objects.requireNonNull(center.getWorld(), "Provided location does not specify world");
    return center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
        .filter(e -> e.getType() == EntityType.PLAYER)
        .map(Player.class::cast);
  }

  /**
   * Applies an action to all players within a given radius of a point.
   *
   * @param center The point
   * @param radius The radius
   * @param consumer The action to apply
   */
  public static void forEachInRange(
      @NotNull final Location center,
      final double radius,
      @NotNull final Consumer<Player> consumer) {
    streamInRange(center, radius).forEach(consumer);
  }
}
