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

import dev.demeng.pluginbase.redis.MessageTransferObject;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Asynchronous event called when a Redis message is received by the server.
 */
public class AsyncRedisMessageReceiveEvent extends Event implements IRedisMessageReceiveEvent {

  @NotNull private static final HandlerList HANDLERS = new HandlerList();

  /**
   * Name of the channel the message came from.
   */
  @Getter @NotNull private final String channel;

  /**
   * MessageTransferObject containing message's data.
   */
  @NotNull private final MessageTransferObject messageTransferObject;

  public AsyncRedisMessageReceiveEvent(
      @NotNull final String channel,
      @NotNull final MessageTransferObject mto) {
    super(true);
    this.channel = channel;
    this.messageTransferObject = mto;
  }

  @Override
  public @NotNull String getSenderId() {
    return this.messageTransferObject.getServerId();
  }

  @Override
  public @NotNull String getMessage() {
    return this.messageTransferObject.getMessage();
  }

  @Override
  public <T> @NotNull Optional<T> getMessageObject(@NotNull final Class<T> objectClass) {
    return this.messageTransferObject.parseMessageObject(objectClass);
  }

  @Override
  public long getTimestamp() {
    return this.messageTransferObject.getTimestamp();
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }
}
