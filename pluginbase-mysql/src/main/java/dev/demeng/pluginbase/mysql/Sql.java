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

package dev.demeng.pluginbase.mysql;

import be.bendem.sqlstreams.SqlStream;
import be.bendem.sqlstreams.util.SqlConsumer;
import be.bendem.sqlstreams.util.SqlFunction;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.demeng.pluginbase.Common;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an SQL database. Contains several useful utilities such as quickly querying and
 * executing (prepared) statements as well as initializing the database with optimal settings.
 */
public class Sql implements ISql {

  private static final String DEFAULT_JDBC_URL =
      "jdbc:mysql://{host}:{port}/{database}" + "?autoReconnect=true&useSSL=false";

  private static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);

  private static final int MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1;
  private static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);

  private static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30);
  private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

  @NotNull @Getter private final HikariDataSource source;
  @NotNull @Getter private final SqlStream stream;

  /**
   * Creates a new SQL database manager instance with the optimized settings. Initializes a new
   * Hikari data source and SQL stream.
   *
   * @param driverClass The driver class name (ex. com.mysql.cj.jdbc.Driver)
   * @param jdbcUrl     The JDBC URL, or null for the default URL
   * @param credentials The database credentials
   */
  public Sql(final @NotNull String driverClass, final @Nullable String jdbcUrl,
      final @NotNull SqlCredentials credentials) {

    final HikariConfig hikari = new HikariConfig();

    hikari.setPoolName(Common.getName() + "-" + POOL_COUNTER.getAndIncrement());

    hikari.setDriverClassName(driverClass);
    hikari.setJdbcUrl(
        Common.getOrDefault(jdbcUrl, DEFAULT_JDBC_URL).replace("{host}", credentials.getHost())
            .replace("{port}", "" + credentials.getPort())
            .replace("{database}", credentials.getDatabase()));

    hikari.setUsername(credentials.getUser());
    hikari.setPassword(credentials.getPassword());

    hikari.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
    hikari.setMinimumIdle(MINIMUM_IDLE);

    hikari.setMaxLifetime(MAX_LIFETIME);
    hikari.setConnectionTimeout(CONNECTION_TIMEOUT);

    final Map<String, String> properties = new HashMap<>();
    properties.put("useUnicode", "true");
    properties.put("characterEncoding", "utf8");
    properties.put("cachePrepStmts", "true");
    properties.put("prepStmtCacheSize", "250");
    properties.put("prepStmtCacheSqlLimit", "2048");
    properties.put("useServerPrepStmts", "true");
    properties.put("useLocalSessionState", "true");
    properties.put("rewriteBatchedStatements", "true");
    properties.put("cacheResultSetMetadata", "true");
    properties.put("cacheServerConfiguration", "true");
    properties.put("elideSetAutoCommits", "true");
    properties.put("maintainTimeStats", "false");
    properties.put("alwaysSendSetIsolation", "false");
    properties.put("cacheCallableStmts", "true");
    properties.put("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));

    for (final Map.Entry<String, String> property : properties.entrySet()) {
      hikari.addDataSourceProperty(property.getKey(), property.getValue());
    }

    this.source = new HikariDataSource(hikari);
    this.stream = SqlStream.connect(this.source);
  }

  @NotNull
  @Override
  public HikariDataSource getHikari() {
    return this.source;
  }

  @NotNull
  @Override
  public Connection getConnection() throws SQLException {
    return Objects.requireNonNull(this.source.getConnection(), "Connection is null");
  }

  @NotNull
  @Override
  public SqlStream stream() {
    return this.stream;
  }

  @Override
  public void execute(@Language("MySQL") @NotNull final String statement,
      @NotNull final SqlConsumer<PreparedStatement> preparer) {
    try (final Connection c = this.getConnection();
        final PreparedStatement s = c.prepareStatement(statement)) {
      preparer.accept(s);
      s.execute();
    } catch (final SQLException ex) {
      Common.error(ex, "Failed to execute SQL statement.", false);
    }
  }

  @Override
  public <R> @NotNull Optional<R> query(@Language("MySQL") @NotNull final String query,
      @NotNull final SqlConsumer<PreparedStatement> preparer,
      @NotNull final SqlFunction<ResultSet, R> handler) {
    try (final Connection c = this.getConnection();
        final PreparedStatement s = c.prepareStatement(query)) {
      preparer.accept(s);
      try (final ResultSet r = s.executeQuery()) {
        return Optional.ofNullable(handler.apply(r));
      }
    } catch (final SQLException ex) {
      Common.error(ex, "Failed to query SQL statement.", false);
      return Optional.empty();
    }
  }

  @Override
  public void executeBatch(@NotNull final BatchBuilder builder) {

    if (builder.getHandlers().isEmpty()) {
      return;
    }

    if (builder.getHandlers().size() == 1) {
      this.execute(builder.getStatement(), builder.getHandlers().iterator().next());
      return;
    }

    try (final Connection c = this.getConnection();
        final PreparedStatement s = c.prepareStatement(builder.getStatement())) {
      for (final SqlConsumer<PreparedStatement> handlers : builder.getHandlers()) {
        handlers.accept(s);
        s.addBatch();
      }
      s.executeBatch();
    } catch (final SQLException ex) {
      Common.error(ex, "Failed to batch execute SQL statement.", false);
    }
  }

  @Override
  public @NotNull BatchBuilder batch(@Language("MySQL") @NotNull final String statement) {
    return new BatchBuilder(this, statement);
  }

  @Override
  public void close() {
    this.source.close();
  }
}
