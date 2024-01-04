/*
 * MIT License
 *
 * Copyright (c) 2024 Demeng Chen
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for {@link org.bukkit.Location}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Locations {

  /**
   * Rounds a location to the center of a block.
   *
   * @param loc The location to center
   * @return The centered location
   */
  @NotNull
  public static Location center(@NotNull final Location loc) {
    return new Location(loc.getWorld(),
        loc.getBlockX() + 0.5,
        loc.getBlockY(),
        loc.getBlockZ() + 0.5,
        loc.getYaw(),
        loc.getPitch());
  }

  /**
   * Converts a location to a block location (world and integer coordinates).
   *
   * @param loc The location to convert to a block location
   * @return The block position
   */
  @NotNull
  public static Location toBlockLocation(@NotNull final Location loc) {
    return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
  }
}
