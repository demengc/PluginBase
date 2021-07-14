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

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that can be serialized to a string.
 *
 * @param <T> The serializable object
 */
public interface StringSerializable<T> {

  /**
   * The string to use to split data in serialized strings.
   */
  String SEPARATOR = ";";

  /**
   * Serializes an object into a string.
   *
   * @param obj The object to serialize
   * @return The serialized object
   */
  @NotNull String serialize(@NotNull T obj);

  /**
   * Deserializes an object from a string.
   *
   * @param str The serialized object
   * @return The deserialized object or null if unable to deserialize, wrapped in an optional
   */
  Optional<T> deserialize(@NotNull String str);
}
