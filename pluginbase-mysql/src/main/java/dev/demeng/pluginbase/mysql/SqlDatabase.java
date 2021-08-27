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

import be.bendem.sqlstreams.SqlStream;
import be.bendem.sqlstreams.util.SqlConsumer;
import be.bendem.sqlstreams.util.SqlFunction;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.plugin.BaseLoader;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an SQL database. Contains several useful utilities such as quickly querying and
 * executing (prepared) statements as well as initializing the database with optimal settings.
 */
public class SqlDatabase {

  private static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);

  private static final int MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1;
  private static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);

  private static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30);
  private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(10);

  @NotNull @Getter private final HikariDataSource source;
  @NotNull @Getter private final SqlStream stream;

  /**
   * Creates a new SQL database with the most optimal settings. Initializes a new Hikari data source
   * and SQL stream.
   *
   * @param driverType        The driver to use
   * @param credentials       The database credentials POJO
   * @param additionalOptions Additional string to append to the JDBC URL
   */
  public SqlDatabase(final @NotNull DriverType driverType,
      final @NotNull DatabaseCredentials credentials,
      final @NotNull String additionalOptions) {

    final HikariConfig hikari = new HikariConfig();

    hikari.setPoolName(Common.getName() + "-" + POOL_COUNTER.getAndIncrement());

    hikari.setDriverClassName(driverType.getDriverClass());
    hikari.setJdbcUrl(driverType.getJdbcUrl(credentials, additionalOptions));

    hikari.setUsername(credentials.getUser());
    hikari.setPassword(credentials.getPassword());

    hikari.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
    hikari.setMinimumIdle(MINIMUM_IDLE);

    hikari.setMaxLifetime(MAX_LIFETIME);
    hikari.setConnectionTimeout(CONNECTION_TIMEOUT);
    hikari.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);

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

  /**
   * Gets a connection from the data source.
   *
   * @return A connection
   * @throws SQLException If the connection could not be retrieved
   */
  @NotNull
  public Connection getConnection() throws SQLException {
    return Objects.requireNonNull(source.getConnection(), "Connection is null");
  }

  /**
   * Executes a new SQL statement.
   *
   * @param sql      The SQL statement
   * @param preparer The preparer for the statement- this is where you should set your placeholders
   * @throws SQLException If the statement could not be executed
   */
  public final void execute(
      final @NotNull @Language("SQL") String sql,
      final @Nullable SqlConsumer<PreparedStatement> preparer) throws SQLException {

    try (final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement(sql)) {

      if (preparer != null) {
        preparer.accept(statement);
      }

      statement.execute();
    }
  }

  /**
   * Executes a new query to the SQL database.
   *
   * @param sql      The SQL statement
   * @param preparer The preparer for the statement- this is where you should set your placeholders
   * @param handler  The handler for the result set- determines what should be done with the data
   * @param <R>      The return value of the handler
   * @return The return value of the handler
   * @throws SQLException If the statement could not be executed
   */
  public <R> Optional<R> query(
      @NotNull @Language("SQL") final String sql,
      @Nullable final SqlConsumer<PreparedStatement> preparer,
      @NotNull final SqlFunction<ResultSet, R> handler) throws SQLException {

    try (final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement(sql)) {

      if (preparer != null) {
        preparer.accept(statement);
      }

      try (final ResultSet rs = statement.executeQuery()) {
        return Optional.ofNullable(handler.apply(rs));
      }
    }
  }

  /**
   * Executes a quick query to the SQL database. This automatically checks if the result set is
   * empty and will return null by default if the handler cannot return a proper value.
   *
   * @param sql      The SQL statement
   * @param preparer The preparer for the statement- this is where you should set your placeholders
   * @param handler  The handler for the result set- determines what should be done with the data
   * @param <R>      The return value of the handler
   * @return The return value of the handler
   * @throws SQLException If the statement could not be executed
   */
  @Nullable
  public <R> R quickQuery(
      @NotNull @Language("SQL") final String sql,
      @Nullable final SqlConsumer<PreparedStatement> preparer,
      @NotNull final SqlFunction<ResultSet, R> handler) throws SQLException {
    return query(sql, preparer, rs -> rs.next() ? handler.apply(rs) : null).orElse(null);
  }

  /**
   * Executes a batch statement (multiple statements in 1 connection).
   *
   * @param builder The batch builder
   * @throws SQLException If the batch statement could not be executed
   */
  public void executeBatch(@NotNull final BatchBuilder builder) throws SQLException {

    if (builder.getHandlers().isEmpty()) {
      return;
    }

    if (builder.getHandlers().size() == 1) {
      this.execute(builder.getStatement(), builder.getHandlers().iterator().next());
      return;
    }

    try (final Connection connection = getConnection();
        final PreparedStatement statement = connection.prepareStatement(builder.getStatement())) {
      for (final SqlConsumer<PreparedStatement> handlers : builder.getHandlers()) {
        handlers.accept(statement);
        statement.addBatch();
      }

      statement.executeBatch();
    }
  }

  /**
   * Gets a new batch builder for the provided statement.
   *
   * @param statement The statement the batch builder should be based off of
   * @return The batch builder
   */
  @NotNull
  public BatchBuilder batch(@NotNull @Language("SQL") final String statement) {
    return new BatchBuilder(this, statement);
  }

  /**
   * Closes the data source.
   */
  public void close() {
    source.close();
  }

  /**
   * An enum containing the available driver types to choose from.
   */
  @RequiredArgsConstructor
  public enum DriverType {

    /**
     * A local embedded database.
     *
     * @see <a href="https://www.h2database.com/html/main.html">https://h2database.com</a>
     */
    H2(true, "org.h2.Driver", "jdbc:h2:{path};mode=MySQL"),

    /**
     * A standard MySQL database.
     */
    MYSQL(false, "com.mysql.jdbc.Driver",
        "jdbc:mysql://{host}:{port}/{database}{additionalOptions}"),

    /**
     * A more efficient version of the {@link DriverType#MYSQL} driver.
     */
    MARIADB(false, "org.mariadb.jdbc.driver",
        "jdbc:mariadb://{host}:{port}/{database}{additionalOptions}");

    @Getter private final boolean local;
    @NotNull @Getter private final String driverClass;
    @NotNull private final String jdbcUrl;

    /**
     * Gets a driver type from a string. Not case-sensitive.
     *
     * @param strDriver The name of the driver type
     * @return The driver type with the given name
     */
    @Nullable
    public static DriverType fromString(final String strDriver) {
      return Arrays.stream(DriverType.values())
          .filter(driverType -> strDriver.equalsIgnoreCase(driverType.name())).findFirst()
          .orElse(null);
    }

    /**
     * Gets the JDBC URL for the specified driver type, with its placeholders filled based on the
     * database credentials and additional options provided.
     *
     * @param credentials       The database credentials
     * @param additionalOptions The additional options to append to the JDBC URL
     * @return The JDBC URL with its placeholders replaced
     */
    public final String getJdbcUrl(final DatabaseCredentials credentials,
        final String additionalOptions) {

      if (local) {
        final String path =
            BaseLoader.getPlugin().getDataFolder().getAbsolutePath() + File.separator + credentials
                .getDatabase();

        return jdbcUrl.replace("{path}", path)
            .replace("{additionalOptions}", additionalOptions);
      }

      final String host = Objects.requireNonNull(credentials.getHost(), "Database host is null");

      return jdbcUrl
          .replace("{host}", host)
          .replace("{port}", "" + credentials.getPort())
          .replace("{database}", credentials.getDatabase())
          .replace("{additionalOptions}", additionalOptions);
    }
  }
}
