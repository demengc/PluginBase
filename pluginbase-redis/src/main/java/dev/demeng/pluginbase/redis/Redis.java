/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) LuckPerms/LuckPerms contributors
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

import dev.demeng.pluginbase.Schedulers;
import dev.demeng.pluginbase.messaging.Messenger;
import dev.demeng.pluginbase.text.Text;
import java.util.logging.Level;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;

public class Redis extends Messenger implements IRedis {

  @Getter private final JedisPool jedisPool;
  private final String[] channels;
  private final Subscription sub;

  private boolean closing = false;

  /**
   * Creates a new Redis instance.
   *
   * @param credentials The credentials
   * @param channels    The channels to subscribe
   */
  public Redis(@NotNull final RedisCredentials credentials, final String... channels) {

    if (credentials.getUser() == null) {
      this.jedisPool = new JedisPool(new JedisPoolConfig(), credentials.getHost(),
          credentials.getPort(), Protocol.DEFAULT_TIMEOUT, credentials.getPassword(),
          credentials.isSsl());

    } else {
      this.jedisPool = new JedisPool(new JedisPoolConfig(), credentials.getHost(),
          credentials.getPort(), Protocol.DEFAULT_TIMEOUT,
          credentials.getUser(), credentials.getPassword(), credentials.isSsl());
    }

    this.channels = channels;
    this.sub = new Subscription();
    Schedulers.async().run(sub);
  }

  @Override
  public void sendMessage(@NotNull final String channel, @NotNull final String encoded) {
    try (final Jedis jedis = getJedis()) {
      jedis.publish(channel, encoded);
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void close() {
    this.closing = true;

    if (this.sub != null) {
      this.sub.unsubscribe();
    }

    if (this.jedisPool != null) {
      this.jedisPool.destroy();
    }
  }

  @Override
  public @NotNull Jedis getJedis() {
    return getJedisPool().getResource();
  }

  private class Subscription extends JedisPubSub implements Runnable {

    @Override
    public void run() {

      boolean first = true;

      while (!Redis.this.closing && !Thread.interrupted() && !Redis.this.jedisPool.isClosed()) {
        try (final Jedis jedis = Redis.this.getJedis()) {
          if (first) {
            first = false;
          } else {
            Text.log("Redis pub/sub connection re-established.");
          }

          if (Redis.this.channels != null) {
            jedis.subscribe(this, channels); // blocking call
          }

        } catch (final Exception ex) {
          if (Redis.this.closing) {
            return;
          }

          Text.log(Level.WARNING,
              "Redis pub-sub connection dropped, attempting to re-open connection...");

          try {
            unsubscribe();
          } catch (final Exception ignored) {
          }

          // Sleep for 5 seconds to prevent massive spam in console.
          try {
            Thread.sleep(5000);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }

    @Override
    public void onMessage(final String channel, final String msg) {
      try {
        Redis.this.consumeMessage(channel, msg);
      } catch (final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
