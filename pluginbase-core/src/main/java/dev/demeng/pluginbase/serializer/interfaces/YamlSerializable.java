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

package dev.demeng.pluginbase.serializer.interfaces;

import dev.demeng.pluginbase.YamlConfig;
import java.io.IOException;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that can be serialized to a YAML configuration section.
 *
 * @param <T> The serializable object
 */
public interface YamlSerializable<T> {

  /**
   * Serializes an object and saves it into a configuration file.
   *
   * @param obj    The object to serialize
   * @param configFile The configuration file to save
   * @param path   The configuration path to save in
   */
  void serializeToConfig(@NotNull T obj, @NotNull YamlConfig configFile, @NotNull String path)
      throws IOException;

  /**
   * Deserializes an object from a configuration section.
   *
   * @param section The serialized object
   * @return The deserialized object or null if unable to deserialize, wrapped in an optional
   */
  Optional<T> deserializeFromSection(@NotNull ConfigurationSection section);
}
