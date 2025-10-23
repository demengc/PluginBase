/*
 * MIT License
 *
 * Copyright (c) 2022 Jiří Apjár
 * Copyright (c) 2022 Filip Zeman
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

package dev.demeng.pluginbase.redis.event;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/** The interface of a Redis message receive event. */
public interface IRedisMessageReceiveEvent {

  /**
   * Gets the ID of the sending server.
   *
   * @return The ID of the sending server
   */
  @NotNull
  String getSenderId();

  /**
   * Gets the channel of the message.
   *
   * @return THe channel name
   */
  @NotNull
  String getChannel();

  /**
   * Gets the raw string message.
   *
   * @return The raw string message
   */
  @NotNull
  String getMessage();

  /**
   * Gets the timestamp of the message
   *
   * @return The timestamp of the message
   */
  long getTimestamp();

  /**
   * Gets the object from the received message
   *
   * @param objectClass Object class
   * @param <T> Object type
   * @return Parsed object, or null if it cannot be parsed
   */
  @SuppressWarnings("Make sure the recieved message can really be converted to provided type!")
  @NotNull
  <T> Optional<T> getMessageObject(@NotNull Class<T> objectClass);
}
