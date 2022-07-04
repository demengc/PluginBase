/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) lucko/helper contributors
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

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * The POJO containing the credentials to a Redis instance.
 */
@Data(staticConstructor = "of")
public final class RedisCredentials {

  /**
   * The host (IP) of the instance. Local host is supported.
   */
  @NotNull private final String host;

  /**
   * The port to the instance, typically 6379.
   */
  private final int port;

  /**
   * The password to the instance. Can be empty.
   */
  @NotNull private final String password;

  /**
   * Creates a new instance of RedisCredentials without a password.
   *
   * @param host The host of the instance
   * @param port The port to the instance
   * @return The Redis credentials with an empty password
   */
  @NotNull
  public static RedisCredentials of(@NotNull final String host, final int port) {
    return of(host, port, "");
  }

  /**
   * Gets Redis credentials from a configuration section.
   *
   * @param section The configuration section
   * @return The Redis credentials provided
   */
  @NotNull
  public static RedisCredentials of(@NotNull final ConfigurationSection section) {
    return of(
        section.getString("host", "localhost"),
        section.getInt("port", 6379),
        section.getString("password", ""));
  }
}
