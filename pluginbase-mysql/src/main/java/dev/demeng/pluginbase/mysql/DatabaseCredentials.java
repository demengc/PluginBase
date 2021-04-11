/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;

/**
 * The POJO containing the credentials to a SQL database.
 */
@Data
public final class DatabaseCredentials {

  /**
   * The host (IP) of the database. Local host is supported.
   */
  @Nullable private final String host;

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
  @Nullable private final String user;

  /**
   * The password to the database.
   */
  @Nullable private final String password;

  /**
   * Retrieves the database credentials from a configuration section.
   *
   * @param section The configuration section containing the credentials
   * @return The {@link DatabaseCredentials} object with the specified credentials
   */
  public static DatabaseCredentials fromConfig(@NotNull final ConfigurationSection section) {
    return new DatabaseCredentials(section.getString("host"),
        section.getInt("port"),
        section.getString("database"),
        section.getString("user"),
        section.getString("password"));
  }
}
