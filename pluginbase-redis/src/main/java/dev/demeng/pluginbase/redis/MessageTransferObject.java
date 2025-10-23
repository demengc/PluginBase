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

package dev.demeng.pluginbase.redis;

import com.google.gson.Gson;
import dev.demeng.pluginbase.gson.GsonProvider;
import java.util.Optional;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The object that wraps an object to be sent across the network, serializable using {@link Gson}.
 */
@Data(staticConstructor = "of")
public class MessageTransferObject {

  @NotNull private final String serverId;
  @NotNull private final String message;
  private final long timestamp;

  /**
   * Wraps the given object to a {@link MessageTransferObject} object.
   *
   * @param serverId Identifier of the sending server
   * @param obj Object that should be wrapped
   * @param timestamp Timestamp of the message
   * @return The serialized object wrapped into a {@link MessageTransferObject}
   */
  public static MessageTransferObject of(
      @NotNull final String serverId, @NotNull final Object obj, final long timestamp) {
    return new MessageTransferObject(serverId, GsonProvider.standard().toJson(obj), timestamp);
  }

  /**
   * Converts current data to JSON.
   *
   * @return Object serialized to JSON String
   */
  @Nullable
  public String toJson() {
    try {
      return GsonProvider.standard().toJson(this);
    } catch (final Exception ex) {
      return null;
    }
  }

  /**
   * Obtains {@link MessageTransferObject} from the provided JSON String.
   *
   * @param json Serialized {@link MessageTransferObject} in JSON String
   * @return Deserialized {@link MessageTransferObject} optional
   */
  @NotNull
  public static Optional<MessageTransferObject> fromJson(@NotNull final String json) {
    try {
      return Optional.ofNullable(
          GsonProvider.standard().fromJson(json, MessageTransferObject.class));
    } catch (final Exception ex) {
      return Optional.empty();
    }
  }

  /**
   * Parses the message string to provided object type.
   *
   * @param objectType Class of the object
   * @param <T> Type of the object
   * @return Parsed object or null if object cannot be parsed
   */
  @NotNull
  public <T> Optional<T> parseMessageObject(@NotNull final Class<T> objectType) {
    try {
      return Optional.ofNullable(GsonProvider.standard().fromJson(this.message, objectType));
    } catch (final Exception ex) {
      return Optional.empty();
    }
  }
}
