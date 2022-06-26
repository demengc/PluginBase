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

package dev.demeng.pluginbase.mongo;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The POJO containing the credentials to a Mongo database.
 */
@Data
public final class DatabaseCredentials {

  /**
   * The connection URI for the database. If this is provided, the remaining credentials (host,
   * port, database, user, and password) are IGNORED.
   */
  @Nullable private final String uri;

  /**
   * The host (address) of the database.
   */
  private final String host;

  /**
   * The port to the database, typically 27017.
   */
  private final int port;

  /**
   * The name of the database.
   */
  private final String database;

  /**
   * The username to the database.
   */
  private final String user;

  /**
   * The password to the database.
   */
  private final String password;

  /**
   * Gets database credentials from a configuration section.
   *
   * @param section The configuration section
   * @return The database credentials provided
   */
  @NotNull
  public static DatabaseCredentials fromConfig(@NotNull final ConfigurationSection section) {
    return new DatabaseCredentials(
        section.getString("uri", ""),
        section.getString("host", "localhost"),
        section.getInt("port", 27017),
        section.getString("database", "minecraft"),
        section.getString("user", "root"),
        section.getString("password", "root"));
  }
}
