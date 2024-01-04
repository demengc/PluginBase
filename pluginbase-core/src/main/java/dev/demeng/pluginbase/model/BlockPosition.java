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

package dev.demeng.pluginbase.model;

import dev.demeng.pluginbase.serialize.YamlSerializable;
import java.util.Objects;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable and serializable Location object with integer values and no yaw nor pitch.
 */
@Data(staticConstructor = "of")
public class BlockPosition implements YamlSerializable {

  @NotNull private final String world;
  private final int x;
  private final int y;
  private final int z;

  @NotNull
  public static BlockPosition of(@NotNull final Location loc) {
    return of(Objects.requireNonNull(loc.getWorld()).getName(),
        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
  }

  @Override
  public void serialize(@NotNull final ConfigurationSection section) {
    section.set("world", world);
    section.set("x", x);
    section.set("y", y);
    section.set("z", z);
  }

  @NotNull
  public static BlockPosition deserialize(@NotNull final ConfigurationSection section) {
    return of(Objects.requireNonNull(section.getString("world")),
        section.getInt("x"), section.getInt("y"), section.getInt("z"));
  }

  /**
   * Converts the block position to a Bukkit {@link Location}.
   *
   * @return The Bukkit Location
   */
  @NotNull
  public Location toLocation() {
    return new Location(Bukkit.getWorld(world), x, y, z);
  }

  /**
   * Gets the block at the given location.
   *
   * @return The block at the location
   */
  @NotNull
  public Block toBlock() {
    return toLocation().getBlock();
  }
}
