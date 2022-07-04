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

package dev.demeng.pluginbase.redis;

import com.google.common.reflect.TypeToken;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.TaskUtils;
import dev.demeng.pluginbase.exceptions.BaseException;
import dev.demeng.pluginbase.messaging.AbstractMessenger;
import dev.demeng.pluginbase.messaging.Channel;
import dev.demeng.pluginbase.plugin.BaseManager;
import dev.demeng.pluginbase.text.TextUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis implements IRedis {

  private final String logPrefix = "[ " + Common.getName() + "-redis" + "] ";

  private final JedisPool jedisPool;
  private final AbstractMessenger messenger;

  private final Set<String> channels = new HashSet<>();
  private final Set<BukkitTask> registry = new HashSet<>();

  private PubSubListener listener = null;

  public Redis(@NotNull RedisCredentials credentials) {
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(16);

    // setup jedis
    if (credentials.getPassword().trim().isEmpty()) {
      this.jedisPool = new JedisPool(config, credentials.getHost(), credentials.getPort());
    } else {
      this.jedisPool = new JedisPool(config, credentials.getHost(), credentials.getPort(), 2000,
          credentials.getPassword());
    }

    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.ping();
    }

    new BukkitRunnable() {

      private boolean broken = false;

      @Override
      public void run() {
        if (this.broken) {
          TextUtils.log(logPrefix + "Retrying subscription...");
          this.broken = false;
        }

        try (Jedis jedis = getJedis()) {
          try {
            Redis.this.listener = new PubSubListener();
            jedis.subscribe(Redis.this.listener,
                "pluginbase-redis-dummy".getBytes(StandardCharsets.UTF_8));
          } catch (Exception e) {
            // Attempt to unsubscribe this instance and try again.
            new BaseException("Error subscribing to listener", e).printStackTrace();
            try {
              Redis.this.listener.unsubscribe();
            } catch (Exception ignored) {
            }
            Redis.this.listener = null;
            this.broken = true;
          }
        }

        if (this.broken) {
          // reschedule the runnable
          TaskUtils.delayAsync(task -> this.run(), 1L);
        }
      }
    }.runTaskAsynchronously(BaseManager.getPlugin());

    registry.add(TaskUtils.repeatAsync(task -> {
      // ensure subscribed to all channels
      PubSubListener pubSubListener = Redis.this.listener;

      if (pubSubListener == null || !pubSubListener.isSubscribed()) {
        return;
      }

      for (String channel : this.channels) {
        pubSubListener.subscribe(channel.getBytes(StandardCharsets.UTF_8));
      }
    }, 2L, 2L));

    this.messenger = new AbstractMessenger(
        (channel, message) -> {
          try (Jedis jedis = getJedis()) {
            jedis.publish(channel.getBytes(StandardCharsets.UTF_8), message);
          }
        },
        channel -> {
          TextUtils.log(logPrefix + "Subscribing to channel: " + channel);
          this.channels.add(channel);
          this.listener.subscribe(channel.getBytes(StandardCharsets.UTF_8));
        },
        channel -> {
          TextUtils.log(logPrefix + "Unsubscribing to channel: " + channel);
          this.channels.remove(channel);
          this.listener.unsubscribe(channel.getBytes(StandardCharsets.UTF_8));
        }
    );
  }

  @NotNull
  @Override
  public JedisPool getJedisPool() {
    Objects.requireNonNull(this.jedisPool, "jedisPool");
    return this.jedisPool;
  }

  @NotNull
  @Override
  public Jedis getJedis() {
    return getJedisPool().getResource();
  }

  @Override
  public void close() throws Exception {
    if (this.listener != null) {
      this.listener.unsubscribe();
      this.listener = null;
    }

    if (this.jedisPool != null) {
      this.jedisPool.close();
    }

    for (BukkitTask task : registry) {
      task.cancel();
    }
  }

  @NotNull
  @Override
  public <T> Channel<T> getChannel(@NotNull String name, @NotNull TypeToken<T> type) {
    return this.messenger.getChannel(name, type);
  }

  private final class PubSubListener extends BinaryJedisPubSub {

    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> subscribed = ConcurrentHashMap.newKeySet();

    @Override
    public void subscribe(byte[]... channels) {
      this.lock.lock();
      try {
        for (byte[] channel : channels) {
          String channelName = new String(channel, StandardCharsets.UTF_8);
          if (this.subscribed.add(channelName)) {
            super.subscribe(channel);
          }
        }
      } finally {
        this.lock.unlock();
      }
    }

    @Override
    public void unsubscribe(byte[]... channels) {
      this.lock.lock();
      try {
        super.unsubscribe(channels);
      } finally {
        this.lock.unlock();
      }
    }

    @Override
    public void onSubscribe(byte[] channel, int subscribedChannels) {
      TextUtils.log(
          logPrefix + "Subscribed to channel: " + new String(channel, StandardCharsets.UTF_8));
    }

    @Override
    public void onUnsubscribe(byte[] channel, int subscribedChannels) {
      String channelName = new String(channel, StandardCharsets.UTF_8);
      TextUtils.log(logPrefix + "Unsubscribed from channel: " + channelName);
      this.subscribed.remove(channelName);
    }

    @Override
    public void onMessage(byte[] channel, byte[] message) {
      String channelName = new String(channel, StandardCharsets.UTF_8);
      try {
        Redis.this.messenger.registerIncomingMessage(channelName, message);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
