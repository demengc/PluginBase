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

import com.google.common.reflect.TypeToken;
import dev.demeng.pluginbase.messaging.conversation.ConversationChannel;
import dev.demeng.pluginbase.messaging.conversation.ConversationMessage;
import dev.demeng.pluginbase.messaging.conversation.SimpleConversationChannel;
import dev.demeng.pluginbase.messaging.reqresp.ReqRespChannel;
import dev.demeng.pluginbase.messaging.reqresp.SimpleReqRespChannel;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which manages messaging {@link Channel}s.
 */
public interface Messenger {

  /**
   * Gets a channel by name.
   *
   * @param name the name of the channel.
   * @param type the channel message typetoken
   * @param <T>  the channel message type
   * @return a channel
   */
  @NotNull <T> Channel<T> getChannel(@NotNull String name, @NotNull TypeToken<T> type);


  /**
   * Gets a conversation channel by name.
   *
   * @param name      the name of the channel
   * @param type      the channel outgoing message typetoken
   * @param replyType the channel incoming (reply) message typetoken
   * @param <T>       the channel message type
   * @param <R>       the channel reply type
   * @return a conversation channel
   */
  @NotNull
  default <T extends ConversationMessage, R extends ConversationMessage> ConversationChannel<T, R> getConversationChannel(
      @NotNull final String name, @NotNull final TypeToken<T> type,
      @NotNull final TypeToken<R> replyType) {
    return new SimpleConversationChannel<>(this, name, type, replyType);
  }

  /**
   * Gets a req/resp channel by name.
   *
   * @param name     the name of the channel
   * @param reqType  the request typetoken
   * @param respType the response typetoken
   * @param <Req>    the request type
   * @param <Resp>   the response type
   * @return the req/resp channel
   */
  @NotNull
  default <Req, Resp> ReqRespChannel<Req, Resp> getReqRespChannel(@NotNull final String name,
      @NotNull final TypeToken<Req> reqType, @NotNull final TypeToken<Resp> respType) {
    return new SimpleReqRespChannel<>(this, name, reqType, respType);
  }

  /**
   * Gets a channel by name.
   *
   * @param name  the name of the channel.
   * @param clazz the channel message class
   * @param <T>   the channel message type
   * @return a channel
   */
  @NotNull
  default <T> Channel<T> getChannel(@NotNull final String name, @NotNull final Class<T> clazz) {
    return getChannel(name, TypeToken.of(Objects.requireNonNull(clazz)));
  }

  /**
   * Gets a conversation channel by name.
   *
   * @param name       the name of the channel
   * @param clazz      the channel outgoing message class
   * @param replyClazz the channel incoming (reply) message class
   * @param <T>        the channel message type
   * @param <R>        the channel reply type
   * @return a conversation channel
   */
  @NotNull
  default <T extends ConversationMessage, R extends ConversationMessage> ConversationChannel<T, R> getConversationChannel(
      @NotNull final String name, @NotNull final Class<T> clazz,
      @NotNull final Class<R> replyClazz) {
    return getConversationChannel(name, TypeToken.of(Objects.requireNonNull(clazz)),
        TypeToken.of(Objects.requireNonNull(replyClazz)));
  }

  /**
   * Gets a req/resp channel by name.
   *
   * @param name      the name of the channel
   * @param reqClass  the request class
   * @param respClass the response class
   * @param <Req>     the request type
   * @param <Resp>    the response type
   * @return the req/resp channel
   */
  @NotNull
  default <Req, Resp> ReqRespChannel<Req, Resp> getReqRespChannel(@NotNull final String name,
      @NotNull final Class<Req> reqClass, @NotNull final Class<Resp> respClass) {
    return getReqRespChannel(name, TypeToken.of(Objects.requireNonNull(reqClass)),
        TypeToken.of(Objects.requireNonNull(respClass)));
  }
}
