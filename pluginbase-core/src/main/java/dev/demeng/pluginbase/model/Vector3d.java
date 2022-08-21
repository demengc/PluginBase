/*
 * MIT License
 *
 * Copyright (c) 2022 Demeng Chen
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
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable and serializable 3D vector.
 */
@Data(staticConstructor = "of")
public class Vector3d implements YamlSerializable {

  private final double x;
  private final double y;
  private final double z;

  @NotNull
  public static Vector3d of(@NotNull final Location loc) {
    return of(loc.getX(), loc.getY(), loc.getZ());
  }

  @NotNull
  public static Vector3d of(@NotNull final Vector vector) {
    return of(vector.getX(), vector.getY(), vector.getZ());
  }

  @Override
  public void serialize(@NotNull final ConfigurationSection section) {
    section.set("x", x);
    section.set("y", y);
    section.set("z", z);
  }

  @NotNull
  public static Vector3d deserialize(@NotNull final ConfigurationSection section) {
    return of(section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
  }

  /**
   * Converts the 3D vector to a Bukkit {@link Location}.
   *
   * @param world The world
   * @return The Bukkit Location
   */
  @NotNull
  public Location toLocation(@Nullable final World world) {
    return new Location(world, x, y, z);
  }

  /**
   * Converts the 3D vector to a Bukkit {@link Vector}.
   *
   * @return The Bukkit Vector
   */
  @NotNull
  public Vector toBukkitVector() {
    return new Vector(x, y, z);
  }
}
