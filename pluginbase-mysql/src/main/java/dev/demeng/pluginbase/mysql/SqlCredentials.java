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

package dev.demeng.pluginbase.mysql;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * The POJO containing the credentials to an SQL database.
 */
@Data
public final class SqlCredentials {

  /**
   * The host (IP) of the database. Local host is supported.
   */
  @NotNull private final String host;

  /**
   * The port to the database, typically 3306.
   */
  private final int port;

  /**
   * The name of the database.
   */
  @NotNull private final String database;

  /**
   * The username to the database.
   */
  @NotNull private final String user;

  /**
   * The password to the database.
   */
  @NotNull private final String password;

  /**
   * Gets database credentials from a configuration section.
   *
   * @param section The configuration section
   * @return The database credentials provided
   */
  @NotNull
  public static SqlCredentials fromConfig(@NotNull final ConfigurationSection section) {
    return new SqlCredentials(
        section.getString("host", "localhost"),
        section.getInt("port", 3306),
        section.getString("database", "minecraft"),
        section.getString("user", "root"),
        section.getString("password", "root"));
  }
}
