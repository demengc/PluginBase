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

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Schedulers;
import dev.demeng.pluginbase.redis.event.AsyncRedisMessageReceiveEvent;
import dev.demeng.pluginbase.redis.event.RedisMessageReceiveEvent;
import dev.demeng.pluginbase.text.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;

public class Redis implements IRedis {

  @NotNull @Getter private final String serverId;
  @Getter private final JedisPool jedisPool;

  @NotNull private final List<String> channels = new ArrayList<>();
  @NotNull private final List<Subscription> subscriptions = new ArrayList<>();

  private boolean closing;

  /**
   * Creates a new Redis manager.
   *
   * @param serverId    Server's unique identifier
   * @param credentials Redis server credentials
   * @param channels    Channels to immediately subscribe to
   */
  public Redis(
      @NotNull final String serverId,
      @NotNull final RedisCredentials credentials,
      @NotNull final String... channels) {
    this.serverId = serverId;

    if (credentials.getUser() == null) {
      this.jedisPool = new JedisPool(new JedisPoolConfig(), credentials.getHost(),
          credentials.getPort(), Protocol.DEFAULT_TIMEOUT, credentials.getPassword(),
          credentials.isSsl());
    } else {
      this.jedisPool = new JedisPool(new JedisPoolConfig(), credentials.getHost(),
          credentials.getPort(), Protocol.DEFAULT_TIMEOUT, credentials.getUser(),
          credentials.getPassword(), credentials.isSsl());
    }

    if (channels.length > 0) {
      this.channels.addAll(Arrays.asList(channels));

      final Subscription subscription = new Subscription(this.channels.toArray(new String[0]));
      Schedulers.async().run(subscription);
      this.subscriptions.add(subscription);
    }
  }

  /**
   * Subscribes to the provided channels if they have not been subcribed to already.
   *
   * @param channels Names of the channels to subscribe
   * @return If at least 1 channel has been subcribed to
   */
  public boolean subscribe(@NotNull final String... channels) {

    if (this.closing || channels.length == 0) {
      return false;
    }

    final List<String> newChannels = new ArrayList<>();

    for (final String channel : channels) {
      if (!this.channels.contains(channel)) {
        newChannels.add(channel);
      }
    }

    if (newChannels.isEmpty()) {
      return false;
    }

    this.channels.addAll(newChannels);

    final Subscription subscription = new Subscription(newChannels.toArray(new String[0]));
    Schedulers.async().run(subscription);
    subscriptions.add(subscription);

    return true;
  }

  /**
   * Unsubscribes from the given channels.
   *
   * @param channels Names of the channels to unsubscribe
   */
  public void unsubscribe(@NotNull final String... channels) {

    if (this.closing || channels.length == 0) {
      return;
    }

    for (final Subscription sub : this.subscriptions) {
      sub.unsubscribe(channels);
    }

    this.channels.removeAll(Arrays.asList(channels));
  }

  /**
   * Publishes the object to the provided channel. For simple strings, use
   * {@link #publishString(String, String)} instead.
   *
   * @param channel Channel to be published into
   * @param obj     Object to be published
   * @return True if successful, false if in closing state or failed to serialize
   */
  public boolean publishObject(@NotNull final String channel, @NotNull final Object obj) {
    return this.executePublish(channel,
        MessageTransferObject.of(this.serverId, obj, System.currentTimeMillis()));
  }


  /**
   * Publishes the object to the provided channel. To publish objects, use
   * {@link #publishObject(String, Object)} instead.
   *
   * @param channel Channel to be published into
   * @param str     String to be published
   * @return True if successful, false if in closing state or failed to serialize
   */
  public boolean publishString(@NotNull final String channel, @NotNull final String str) {
    return this.executePublish(channel,
        MessageTransferObject.of(this.serverId, str, System.currentTimeMillis()));
  }

  private boolean executePublish(
      @NotNull final String channel,
      @NotNull final MessageTransferObject mto) {

    if (this.closing) {
      return false;
    }

    final String messageJson = mto.toJson();

    if (messageJson == null) {
      return false;
    }

    Schedulers.async().run(() -> {
      try (final Jedis jedis = this.jedisPool.getResource()) {
        jedis.publish(channel, messageJson);
      } catch (final Exception ex) {
        Common.error(ex, "Failed to publish Redis message.", false);
      }
    });

    return true;
  }

  /**
   * Closes the Redis connection and unsubscribes to all channels.
   */
  public void close() {

    if (this.closing) {
      return;
    }

    this.closing = true;

    for (final Subscription sub : this.subscriptions) {
      if (sub.isSubscribed()) {
        sub.unsubscribe();
      }
    }

    if (this.jedisPool == null) {
      return;
    }

    this.jedisPool.destroy();
  }

  /**
   * Returns whether the channel is subscribed to or not.
   *
   * @param channel The channel to check
   * @return If the channel is subscribed to or not
   */
  public boolean isSubscribed(final String channel) {
    return this.channels.contains(channel);
  }

  @RequiredArgsConstructor
  private class Subscription extends JedisPubSub implements Runnable {

    private final String[] channels;

    @Override
    public void run() {
      boolean firstTry = true;

      while (!Redis.this.closing && !Thread.interrupted() && !Redis.this.jedisPool.isClosed()) {
        try (final Jedis jedis = Redis.this.jedisPool.getResource()) {
          if (firstTry) {
            Text.log("Redis pub/sub connection established.");
            firstTry = false;
          } else {
            Text.log("Redis pub/sub connection re-established.");
          }

          try {
            jedis.subscribe(this, channels);
            Text.log("Subscribed to Redis channels: " + Arrays.toString(channels));
          } catch (final Exception ex) {
            Text.log(Level.WARNING,
                "Failed to subcribe to Redis channels: " + Arrays.toString(channels));
          }

        } catch (final Exception ex) {
          if (Redis.this.closing) {
            return;
          }

          Text.log(Level.WARNING, "Redis pub/sub connection dropped, attempting to re-open...");

          try {
            super.unsubscribe();
          } catch (final Exception ignored) {
          }

          // Sleep for 5 seconds to prevent massive spam in console
          try {
            Thread.sleep(5000);
          } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }

    @Override
    public void onMessage(final String channel, final String message) {
      if (channel == null || message == null) {
        return;
      }

      final Optional<MessageTransferObject> mto = MessageTransferObject.fromJson(message);

      if (mto.isEmpty()) {
        Text.log(Level.WARNING,
            "Failed to read Redis message from channel '" + channel + "': " + message);
        return;
      }

      Bukkit.getPluginManager().callEvent(new AsyncRedisMessageReceiveEvent(channel, mto.get()));
      Schedulers.sync().run(() -> Bukkit.getPluginManager()
          .callEvent(new RedisMessageReceiveEvent(channel, mto.get())));
    }
  }
}
