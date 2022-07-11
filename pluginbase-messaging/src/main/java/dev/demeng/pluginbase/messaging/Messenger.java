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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.demeng.pluginbase.exceptions.BaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A service that is able to send and receive {@link Message}s.
 */
public abstract class Messenger {

  public static final Gson GSON = new GsonBuilder().create();

  private static final String ID_KEY = "id";
  private static final String TYPE_KEY = "type";
  private static final String CONTENT_KEY = "content";

  private final List<BiConsumer<String, Message>> listeners = new ArrayList<>();
  private final Map<UUID, Long> receivedMessages = ExpiringMap.builder()
      .expiration(1, TimeUnit.HOURS)
      .expirationPolicy(ExpirationPolicy.CREATED)
      .build();

  /**
   * Sends a message.
   *
   * @param channel The channel to send the message in
   * @param encoded The encoded message
   */
  public abstract void sendMessage(@NotNull String channel, @NotNull String encoded);

  /**
   * Sends a message.
   *
   * @param channel The channel to send the message in
   * @param id      The unique ID of the message (NOT the player UUID)
   * @param message The message
   */
  public void sendMessage(@NotNull final String channel, @NotNull final UUID id, @NotNull final Message message) {
    sendMessage(channel, encodeMessage(id, message));
  }

  /**
   * Sends a message.
   *
   * @param channel The channel to send the message in
   * @param message The message
   */
  public void sendMessage(@NotNull final String channel, @NotNull final Message message) {
    sendMessage(channel, UUID.randomUUID(), message);
  }

  /**
   * Accepts all message consumers.
   *
   * @param channel The message channel
   * @param message The message
   */
  public void consumeMessage(@NotNull final String channel, @NotNull final Message message) {
    for (final BiConsumer<String, Message> consumer : listeners) {
      consumer.accept(channel, message);
    }
  }

  /**
   * Accepts all message listeners.
   *
   * @param channel The message channel
   * @param encoded The encoded message
   */
  public void consumeMessage(@NotNull final String channel, @NotNull final String encoded) {

    final Message decoded = decodeMessage(encoded);

    if (decoded == null) {
      return;
    }

    consumeMessage(channel, decoded);
  }

  /**
   * Gets the class of the message type with the specified string identifier.
   *
   * @param type The message type's string identifier
   * @return The message type's class
   */
  @NotNull
  protected Optional<Class<? extends Message>> getTypeClass(@NotNull final String type) {

    final Class<?> clazz;

    try {
      clazz = Class.forName(type);
    } catch (final ClassNotFoundException ex) {
      return Optional.empty();
    }

    if (!clazz.isAssignableFrom(Message.class)) {
      return Optional.empty();
    }

    return Optional.of(clazz.asSubclass(Message.class));
  }

  /**
   * Adds a new listener.
   *
   * @param consumer The consumer of the channel name and message
   */
  public void addListener(@NotNull final BiConsumer<String, Message> consumer) {
    listeners.add(consumer);
  }

  /**
   * Removes a listener.
   *
   * @param consumer The consumer of the channel name and message
   */
  public void removeListener(@NotNull final BiConsumer<String, Message> consumer) {
    listeners.remove(consumer);
  }

  /**
   * Removes all listeners.
   */
  public void removeAllListener() {
    listeners.clear();
  }

  /**
   * Encodes a message.
   *
   * @param id      The unique ID of this message
   * @param message The message
   * @return The encoded message
   */
  @NotNull
  protected String encodeMessage(
      @NotNull final UUID id,
      @NotNull final Message message) {

    final JsonObject json = new JsonObject();
    json.addProperty(ID_KEY, id.toString());
    json.addProperty(TYPE_KEY, message.getClass().getCanonicalName());
    json.addProperty(CONTENT_KEY, message.encode());

    return json.toString();
  }

  /**
   * Decodes the message from an encoded string message.
   *
   * @param encoded The encoded message, or null if already received or unknown type
   * @return The decoded message
   */
  @Nullable
  protected Message decodeMessage(@NotNull final String encoded) {

    final JsonObject json;

    try {
      json = GSON.fromJson(encoded, JsonObject.class);
    } catch (final JsonParseException ex) {
      throw new BaseException("Unable to decode message: " + encoded, ex);
    }

    final JsonElement idElement = json.get(ID_KEY);

    if (idElement == null) {
      throw new BaseException("No '" + ID_KEY + "' element in message: " + encoded);
    }

    final UUID id = UUID.fromString(idElement.getAsString());

    if (receivedMessages.containsKey(id)) {
      return null;
    }

    receivedMessages.put(id, System.currentTimeMillis());

    final JsonElement typeElement = json.get(TYPE_KEY);

    if (typeElement == null) {
      throw new BaseException("No '" + TYPE_KEY + "' element in message: " + encoded);
    }

    final Optional<Class<? extends Message>> type = getTypeClass(typeElement.getAsString());

    if (!type.isPresent()) {
      return null;
    }

    final JsonElement contentElement = json.get(CONTENT_KEY);

    if (contentElement == null) {
      throw new BaseException("No '" + CONTENT_KEY + "' element in message: " + encoded);
    }

    final String content = contentElement.getAsString();
    return Message.decodeRaw(type.get(), content);
  }
}
