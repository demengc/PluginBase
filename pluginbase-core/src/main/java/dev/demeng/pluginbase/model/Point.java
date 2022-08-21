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
import java.util.Objects;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable and serializable Location object, including the yaw and pitch.
 */
@Data(staticConstructor = "of")
public class Point implements YamlSerializable {

  @NotNull private final String world;
  private final double x;
  private final double y;
  private final double z;
  private final float yaw;
  private final float pitch;

  @NotNull
  public static Point of(@NotNull final Location loc) {
    return of(Objects.requireNonNull(loc.getWorld()).getName(), loc.getX(), loc.getY(), loc.getZ(),
        loc.getYaw(), loc.getPitch());
  }

  @Override
  public void serialize(@NotNull final ConfigurationSection section) {
    section.set("world", world);
    section.set("x", x);
    section.set("y", y);
    section.set("z", z);
    section.set("yaw", yaw);
    section.set("pitch", pitch);
  }

  @NotNull
  public static Point deserialize(@NotNull final ConfigurationSection section) {
    return of(Objects.requireNonNull(section.getString("world")),
        section.getDouble("x"), section.getDouble("y"), section.getDouble("z"),
        (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
  }

  /**
   * Converts the point to a Bukkit {@link Location}.
   *
   * @return The Bukkit Location
   */
  @NotNull
  public Location toLocation() {
    return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
  }
}
