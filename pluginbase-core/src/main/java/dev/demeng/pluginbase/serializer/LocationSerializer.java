/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.serializer;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.ServerProperties;
import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.serializer.interfaces.YamlSerializable;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * The serializer for {@link Location}.
 */
public class LocationSerializer implements YamlSerializable<Location> {

  @Override
  public void serialize(
      @NotNull final Location obj,
      @NotNull final YamlConfig configFile,
      @NotNull final String path
  ) throws IOException {

    if (obj.getWorld() != null) {
      configFile.getConfig().set(path + ".world", obj.getWorld().getName());
    }

    serializeNoWorld(obj, configFile, path);
  }

  /**
   * Serializes a location without the world name. When deserialized, it will attempt to get the
   * default world from server.properties.
   *
   * @param obj        The object to serialize
   * @param configFile The configuration file to save
   * @param path       The configuration path to save in
   * @throws IOException If the file fails to save
   */
  public void serializeNoWorld(
      @NotNull final Location obj,
      @NotNull final YamlConfig configFile,
      @NotNull final String path
  ) throws IOException {

    configFile.getConfig().set(path + ".x", obj.getX());
    configFile.getConfig().set(path + ".y", obj.getY());
    configFile.getConfig().set(path + ".z", obj.getZ());
    configFile.getConfig().set(path + ".yaw", obj.getYaw());
    configFile.getConfig().set(path + ".pitch", obj.getPitch());

    configFile.save();
  }

  /**
   * Serializes a location without the yaw or pitch. When deserialized, the yaw and pitch will be
   * 0.
   *
   * @param obj        The object to serialize
   * @param configFile The configuration file to save
   * @param path       The configuration path to save in
   * @throws IOException if the file fails to save
   */
  public void serializeNoRotation(
      @NotNull final Location obj,
      @NotNull final YamlConfig configFile,
      @NotNull final String path
  ) throws IOException {

    if (obj.getWorld() != null) {
      configFile.getConfig().set(path + ".world", obj.getWorld().getName());
    }

    configFile.getConfig().set(path + ".x", obj.getX());
    configFile.getConfig().set(path + ".y", obj.getY());
    configFile.getConfig().set(path + ".z", obj.getZ());

    configFile.save();
  }

  @Override
  public Location deserialize(@NotNull final ConfigurationSection section) {

    String worldName = section.getString("world");

    if (worldName == null) {
      worldName = Common.getOrDefault(ServerProperties.getProperty("level-name"), "world");
    }

    final World world = Bukkit.getWorld(worldName);

    if (world == null) {
      throw new IllegalArgumentException("World '" + worldName + "' is not loaded");
    }

    return new Location(world,
        section.getDouble("x"),
        section.getDouble("y"),
        section.getDouble("z"),
        (float) section.getDouble("yaw"),
        (float) section.getDouble("pitch"));
  }
}
