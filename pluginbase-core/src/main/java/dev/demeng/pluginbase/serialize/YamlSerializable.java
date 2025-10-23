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

package dev.demeng.pluginbase.serialize;

import dev.demeng.pluginbase.exceptions.BaseException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that can be serialized to a YAML configuration section. Implementing classes should
 * implement a public static <code>deserialize(ConfigurationSection)</code> method.
 */
public interface YamlSerializable {

  /**
   * Deserializes a ConfigurationSection to a YamlSerializable object.
   *
   * @param clazz   The YamlSerializable class
   * @param section The configuration section to deserialize
   * @param <T>     The YamlSerializable type
   * @return The deserialized object
   * @throws IllegalStateException If the class does not have a deserialization method
   */
  @NotNull
  static <T extends YamlSerializable> T deserialize(
      @NotNull final Class<T> clazz,
      @NotNull final ConfigurationSection section) {

    final Method deserializeMethod = getDeserializeMethod(clazz);

    if (deserializeMethod == null) {
      throw new IllegalStateException("Class does not have a deserialize method accessible");
    }

    try {
      //noinspection unchecked
      return (T) deserializeMethod.invoke(null, section);
    } catch (final IllegalAccessException | InvocationTargetException ex) {
      throw new BaseException(ex);
    }
  }

  /**
   * Deserializes a ConfigurationSection to a YamlSerializable object.
   *
   * @param clazz   The YamlSerializable class
   * @param section The configuration section to deserialize
   * @return The deserialized object
   * @throws IllegalStateException If the clazz does not have a deserialization method
   */
  @NotNull
  static YamlSerializable deserializeRaw(
      @NotNull final Class<?> clazz,
      @NotNull final ConfigurationSection section) {
    return deserialize(clazz.asSubclass(YamlSerializable.class), section);
  }

  /**
   * Gets the deserialization method for a given class.
   *
   * @param clazz the class
   * @return The deserialization method, if the class has one
   */
  @Nullable
  static Method getDeserializeMethod(@NotNull final Class<?> clazz) {

    if (!YamlSerializable.class.isAssignableFrom(clazz)) {
      return null;
    }

    final Method deserializeMethod;

    try {
      deserializeMethod = clazz.getDeclaredMethod("deserialize", ConfigurationSection.class);
      deserializeMethod.setAccessible(true);
    } catch (final Exception ex) {
      return null;
    }

    if (!Modifier.isStatic(deserializeMethod.getModifiers())) {
      return null;
    }

    return deserializeMethod;
  }

  /**
   * Serializes the object into the configuration section. The configuration file is not saved after
   * serialization.
   *
   * @param section The section the object will be serialized into
   */
  void serialize(@NotNull ConfigurationSection section);
}
