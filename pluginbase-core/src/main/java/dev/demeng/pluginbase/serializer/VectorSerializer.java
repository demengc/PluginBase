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

import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.serializer.interfaces.YamlSerializable;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * The serializer for {@link Vector}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VectorSerializer implements YamlSerializable<Vector> {

  private static final VectorSerializer NOOP = new VectorSerializer();

  /**
   * Gets the singular instance of this serializer.
   *
   * @return The serializer instance
   */
  public static VectorSerializer get() {
    return NOOP;
  }

  @Override
  public void serialize(
      @NotNull final Vector obj,
      @NotNull final YamlConfig configFile,
      @NotNull final String path
  ) throws IOException {

    configFile.getConfig().set(path + ".x", obj.getX());
    configFile.getConfig().set(path + ".y", obj.getY());
    configFile.getConfig().set(path + ".z", obj.getZ());

    configFile.save();
  }

  @Override
  public Vector deserialize(@NotNull final ConfigurationSection section) {
    return new Vector(
        section.getDouble("x"),
        section.getDouble("y"),
        section.getDouble("z"));
  }
}
