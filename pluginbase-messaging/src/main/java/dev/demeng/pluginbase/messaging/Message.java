/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

package dev.demeng.pluginbase.messaging;

import dev.demeng.pluginbase.exceptions.BaseException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that can be sent or received through a messaging service, such as Redis. All classes
 * extending this abstract class should contain a static <code>decode(String)</code> method that
 * returns the object from the encoded string.
 */
public interface Message {

  /**
   * Encodes the object into a string.
   *
   * @return The encoded object
   */
  @NotNull String encode();

  /**
   * Decodes a string into a Message object.
   *
   * @param clazz The Message class
   * @param str   The string to decode
   * @param <T>   The Message type
   * @return The decoded Message
   * @throws IllegalStateException If the class does not have a decoding method
   */
  @NotNull
  static <T extends Message> T decode(
      @NotNull final Class<T> clazz,
      @NotNull final String str) {

    final Method decodeMethod = getDecodeMethod(clazz);

    if (decodeMethod == null) {
      throw new IllegalStateException("Class does not have a decode method accessible");
    }

    try {
      //noinspection unchecked
      return (T) decodeMethod.invoke(null, str);
    } catch (final IllegalAccessException | InvocationTargetException ex) {
      throw new BaseException(ex);
    }
  }

  /**
   * Decodes a String to a Message object.
   *
   * @param clazz The Message class
   * @param str   The string to decode
   * @return The decoded Message
   * @throws IllegalStateException If the clazz does not have a decode method
   */
  @NotNull
  static Message decodeRaw(
      @NotNull final Class<?> clazz,
      @NotNull final String str) {
    return decode(clazz.asSubclass(Message.class), str);
  }

  /**
   * Gets the code method for a given class.
   *
   * @param clazz The class
   * @return The decode method, if the class has one
   */
  @Nullable
  static Method getDecodeMethod(@NotNull final Class<?> clazz) {

    if (!Message.class.isAssignableFrom(clazz)) {
      return null;
    }

    final Method decodeMethod;

    try {
      //noinspection JavaReflectionMemberAccess
      decodeMethod = clazz.getDeclaredMethod("decode", String.class);
      decodeMethod.setAccessible(true);
    } catch (final Exception ex) {
      return null;
    }

    if (!Modifier.isStatic(decodeMethod.getModifiers())) {
      return null;
    }

    return decodeMethod;
  }
}
