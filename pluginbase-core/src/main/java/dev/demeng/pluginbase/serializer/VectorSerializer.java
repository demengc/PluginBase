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

import dev.demeng.pluginbase.Validate;
import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.serializer.interfaces.StringSerializable;
import dev.demeng.pluginbase.serializer.interfaces.YamlSerializable;
import java.io.IOException;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * The serializer for {@link Vector}.
 */
public class VectorSerializer implements StringSerializable<Vector>, YamlSerializable<Vector> {

  @Override
  public @NotNull String serialize(@NotNull Vector obj) {
    return obj.getX() + SEPARATOR + obj.getY() + SEPARATOR + obj.getZ();
  }

  @Override
  public Optional<Vector> deserialize(@NotNull String str) {

    final String[] split = str.split(SEPARATOR);

    if (split.length != 3) {
      return Optional.empty();
    }

    final Double x = Validate.checkDouble(split[0]);
    final Double y = Validate.checkDouble(split[1]);
    final Double z = Validate.checkDouble(split[2]);

    if (x == null || y == null || z == null) {
      return Optional.empty();
    }

    return Optional.of(new Vector(x, y, z));
  }

  @Override
  public void serializeToConfig(
      @NotNull Vector obj,
      @NotNull YamlConfig configFile,
      @NotNull String path
  ) throws IOException {

    configFile.getConfig().set(path + ".x", obj.getX());
    configFile.getConfig().set(path + ".y", obj.getY());
    configFile.getConfig().set(path + ".z", obj.getZ());

    configFile.save();
  }

  @Override
  public Optional<Vector> deserializeFromSection(@NotNull ConfigurationSection section) {
    return Optional.of(new Vector(
        section.getDouble("x"),
        section.getDouble("y"),
        section.getDouble("z")));
  }
}
