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

package dev.demeng.pluginbase.messaging.conversation;

import dev.demeng.pluginbase.messaging.Channel;
import dev.demeng.pluginbase.promise.Promise;
import dev.demeng.pluginbase.terminable.Terminable;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link Channel} providing an abstraction for two-way "conversations".
 *
 * @param <T> the outgoing message type
 * @param <R> the reply message type
 */
public interface ConversationChannel<T extends ConversationMessage, R extends ConversationMessage> extends
    Terminable {

  /**
   * Gets the name of the channel.
   *
   * @return the channel name
   */
  @NotNull
  String getName();

  /**
   * Gets the channel for primary outgoing messages.
   *
   * @return the outgoing channel
   */
  @NotNull
  Channel<T> getOutgoingChannel();

  /**
   * Gets the channel replies are sent on.
   *
   * @return the reply channel
   */
  @NotNull
  Channel<R> getReplyChannel();

  /**
   * Creates a new {@link ConversationChannelAgent} for this channel.
   *
   * @return a new channel agent.
   */
  @NotNull
  ConversationChannelAgent<T, R> newAgent();

  /**
   * Creates a new {@link ConversationChannelAgent} for this channel, and immediately adds the given
   * {@link ConversationChannelListener} to it.
   *
   * @param listener the listener to register
   * @return the resultant agent
   */
  @NotNull
  default ConversationChannelAgent<T, R> newAgent(ConversationChannelListener<T, R> listener) {
    ConversationChannelAgent<T, R> agent = newAgent();
    agent.addListener(listener);
    return agent;
  }

  /**
   * Sends a new message to the channel.
   *
   * <p>This method will return immediately, and the promise will be completed
   * once the message has been sent.</p>
   *
   * @param message         the message to dispatch
   * @param replyListener   the reply listener
   * @param timeoutDuration the timeout duration for the reply listener
   * @param unit            the unit of timeoutDuration
   * @return a promise which will complete when the message has sent.
   */
  @NotNull
  Promise<Void> sendMessage(@NotNull T message, @NotNull ConversationReplyListener<R> replyListener,
      long timeoutDuration, @NotNull TimeUnit unit);

  @Override
  void close();
}
