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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import dev.demeng.pluginbase.messaging.Channel;
import dev.demeng.pluginbase.messaging.ChannelAgent;
import dev.demeng.pluginbase.messaging.ChannelListener;
import dev.demeng.pluginbase.messaging.Messenger;
import dev.demeng.pluginbase.promise.Promise;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of {@link ConversationChannel}.
 *
 * @param <T> the outgoing message type
 * @param <R> the reply message type
 */
public class SimpleConversationChannel<T extends ConversationMessage, R extends ConversationMessage> implements
    ConversationChannel<T, R> {

  private final String name;
  private final Channel<T> outgoingChannel;
  private final Channel<R> replyChannel;

  private final Set<Agent<T, R>> agents = ConcurrentHashMap.newKeySet();

  private final ScheduledExecutorService replyTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
  private final ChannelAgent<R> replyAgent;
  private final SetMultimap<UUID, ReplyListenerRegistration<R>> replyListeners = Multimaps.newSetMultimap(
      new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

  public SimpleConversationChannel(final Messenger messenger, final String name,
      final TypeToken<T> outgoingType,
      final TypeToken<R> replyType) {
    this.name = name;
    this.outgoingChannel = messenger.getChannel(name + "-o", outgoingType);
    this.replyChannel = messenger.getChannel(name + "-r", replyType);

    this.replyAgent = this.replyChannel.newAgent(new ReplyListener());
  }

  private final class ReplyListener implements ChannelListener<R> {

    @Override
    public void onMessage(@NotNull final ChannelAgent<R> agent, @NotNull final R message) {
      SimpleConversationChannel.this.replyListeners.get(message.getConversationId())
          .removeIf(l -> l.onReply(message));
    }
  }

  private static final class ReplyListenerRegistration<R extends ConversationMessage> {

    private final ConversationReplyListener<R> listener;
    private final List<R> replies = new ArrayList<>();
    private ScheduledFuture<?> timeoutFuture;

    private boolean active = true;

    private ReplyListenerRegistration(final ConversationReplyListener<R> listener) {
      this.listener = listener;

    }

    /**
     * Passes the incoming reply to the listener, and returns true if the listener should be
     * unregistered
     *
     * @param message the message
     * @return if the listener should be unregistered
     */
    public boolean onReply(final R message) {
      synchronized (this) {
        if (!this.active) {
          return true;
        }

        this.replies.add(message);
        final ConversationReplyListener.RegistrationAction action = this.listener.onReply(message);
        if (action == ConversationReplyListener.RegistrationAction.STOP_LISTENING) {

          // unregister
          this.active = false;
          this.timeoutFuture.cancel(false);

          return true;
        } else {
          return false;
        }
      }
    }

    public void timeout() {
      synchronized (this) {
        if (!this.active) {
          return;
        }

        this.listener.onTimeout(this.replies);
        this.active = false;
      }
    }
  }

  @NotNull
  @Override
  public String getName() {
    return this.name;
  }

  @NotNull
  @Override
  public Channel<T> getOutgoingChannel() {
    return this.outgoingChannel;
  }

  @NotNull
  @Override
  public Channel<R> getReplyChannel() {
    return this.replyChannel;
  }

  @NotNull
  @Override
  public ConversationChannelAgent<T, R> newAgent() {
    final Agent<T, R> agent = new Agent<>(this);
    this.agents.add(agent);
    return agent;
  }

  @NotNull
  @Override
  public Promise<Void> sendMessage(@NotNull final T message,
      @NotNull final ConversationReplyListener<R> replyListener, final long timeoutDuration,
      @NotNull final TimeUnit unit) {
    // register the listener
    final ReplyListenerRegistration<R> listenerRegistration = new ReplyListenerRegistration<>(
        replyListener);
    listenerRegistration.timeoutFuture = this.replyTimeoutExecutor.schedule(
        listenerRegistration::timeout, timeoutDuration, unit);
    this.replyListeners.put(message.getConversationId(), listenerRegistration);

    // send the outgoing message
    return this.outgoingChannel.sendMessage(message);
  }

  @Override
  public void close() {
    this.replyAgent.close();
    this.replyTimeoutExecutor.shutdown();
    this.agents.forEach(Agent::close);
  }

  private static final class Agent<T extends ConversationMessage, R extends ConversationMessage> implements
      ConversationChannelAgent<T, R> {

    private final SimpleConversationChannel<T, R> channel;
    private final ChannelAgent<T> delegateAgent;

    private Agent(@NotNull final SimpleConversationChannel<T, R> channel) {
      this.channel = channel;
      this.delegateAgent = this.channel.getOutgoingChannel().newAgent();
    }

    @NotNull
    @Override
    public ConversationChannel<T, R> getChannel() {
      this.delegateAgent.getChannel(); // ensure this agent is still active
      return this.channel;
    }

    @NotNull
    @Override
    public Set<ConversationChannelListener<T, R>> getListeners() {
      final Set<ChannelListener<T>> listeners = this.delegateAgent.getListeners();

      final ImmutableSet.Builder<ConversationChannelListener<T, R>> ret = ImmutableSet.builder();
      for (final ChannelListener<T> listener : listeners) {
        //noinspection unchecked
        ret.add(((WrappedListener) listener).delegate);
      }
      return ret.build();
    }

    @Override
    public boolean hasListeners() {
      return this.delegateAgent.hasListeners();
    }

    @Override
    public boolean addListener(@NotNull final ConversationChannelListener<T, R> listener) {
      return this.delegateAgent.addListener(new WrappedListener(listener));
    }

    @Override
    public boolean removeListener(@NotNull final ConversationChannelListener<T, R> listener) {
      final Set<ChannelListener<T>> listeners = this.delegateAgent.getListeners();
      for (final ChannelListener<T> other : listeners) {
        final WrappedListener wrapped = (WrappedListener) other;
        if (wrapped.delegate == listener) {
          return this.delegateAgent.removeListener(other);
        }
      }
      return false;
    }

    @Override
    public void close() {
      this.delegateAgent.close();
    }

    private final class WrappedListener implements ChannelListener<T> {

      private final ConversationChannelListener<T, R> delegate;

      private WrappedListener(final ConversationChannelListener<T, R> delegate) {
        this.delegate = delegate;
      }

      @Override
      public void onMessage(@NotNull final ChannelAgent<T> agent, @NotNull final T message) {
        final ConversationReply<R> reply = this.delegate.onMessage(Agent.this, message);
        if (reply.hasReply()) {
          reply.getReply().thenAcceptAsync(m -> {
            if (m != null) {
              Agent.this.channel.replyChannel.sendMessage(m);
            }
          });
        }
      }
    }
  }
}
