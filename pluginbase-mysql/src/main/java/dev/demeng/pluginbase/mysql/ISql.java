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
import com.zaxxer.hikari.HikariDataSource;
import dev.demeng.pluginbase.delegate.Delegates;
import dev.demeng.pluginbase.promise.Promise;
import dev.demeng.pluginbase.promise.ThreadContext;
import dev.demeng.pluginbase.terminable.Terminable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public interface ISql extends Terminable {

  /**
   * Gets the Hikari instance backing the datasource.
   *
   * @return The hikari instance
   */
  @NotNull HikariDataSource getHikari();

  /**
   * Gets a connection from the datasource.
   *
   * <p>The connection should be returned once it has been used.</p>
   *
   * @return A connection
   */
  @NotNull Connection getConnection() throws SQLException;

  /**
   * Gets a {@link SqlStream} instance for this {@link ISql}.
   *
   * @return An instance of the stream library for this connection
   */
  @NotNull SqlStream stream();

  /**
   * Executes a database statement with no preparation.
   *
   * <p>This will be executed on an asynchronous thread.</p>
   *
   * @param statement The statement to be executed
   * @return A Promise of an asynchronous database execution
   * @see #execute(String) to perform this action synchronously
   */
  @NotNull
  default Promise<Void> executeAsync(@Language("MySQL") @NotNull String statement) {
    return Promise.supplying(ThreadContext.ASYNC,
        Delegates.runnableToSupplier(() -> this.execute(statement)));
  }

  /**
   * Executes a database statement with no preparation.
   *
   * <p>This will be executed on whichever thread it's called from.</p>
   *
   * @param statement The statement to be executed
   * @see #executeAsync(String) to perform the same action asynchronously
   */
  default void execute(@Language("MySQL") @NotNull String statement) {
    this.execute(statement, stmt -> {
    });
  }

  /**
   * Executes a database statement with preparation.
   *
   * <p>This will be executed on an asynchronous thread.</p>
   *
   * @param statement The statement to be executed
   * @param preparer  The preparation used for this statement
   * @return A Promise of an asynchronous database execution
   * @see #executeAsync(String, SqlConsumer) to perform this action synchronously
   */
  @NotNull
  default Promise<Void> executeAsync(
      @Language("MySQL") @NotNull String statement,
      @NotNull SqlConsumer<PreparedStatement> preparer) {
    return Promise.supplying(ThreadContext.ASYNC,
        Delegates.runnableToSupplier(() -> this.execute(statement, preparer)));
  }

  /**
   * Executes a database statement with preparation.
   *
   * <p>This will be executed on whichever thread it's called from.</p>
   *
   * @param statement The statement to be executed
   * @param preparer  The preparation used for this statement
   * @see #executeAsync(String, SqlConsumer) to perform this action asynchronously
   */
  void execute(
      @Language("MySQL") @NotNull String statement,
      @NotNull SqlConsumer<PreparedStatement> preparer);

  /**
   * Executes a database query with no preparation.
   *
   * <p>This will be executed on an asynchronous thread.</p>
   *
   * <p>In the case of a {@link SQLException} or in the case of
   * no data being returned, or the handler evaluating to null, this method will return an
   * {@link Optional#empty()} object.</p>
   *
   * @param query   The query to be executed
   * @param handler The handler for the data returned by the query
   * @param <R>     The returned type
   * @return A Promise of an asynchronous database query
   * @see #query(String, SqlFunction) to perform this query synchronously
   */
  @NotNull
  default <R> Promise<Optional<R>> queryAsync(
      @Language("MySQL") @NotNull String query,
      @NotNull SqlFunction<ResultSet, R> handler) {
    return Promise.supplying(ThreadContext.ASYNC, () -> this.query(query, handler));
  }

  /**
   * Executes a database query with no preparation.
   *
   * <p>This will be executed on whichever thread it's called from.</p>
   *
   * <p>In the case of a {@link SQLException} or in the case of
   * no data being returned, or the handler evaluating to null, this method will return an
   * {@link Optional#empty()} object.</p>
   *
   * @param query   The query to be executed
   * @param handler The handler for the data returned by the query
   * @param <R>     The returned type
   * @return The results of the database query
   * @see #queryAsync(String, SqlFunction) to perform this query asynchronously
   */
  @NotNull
  default <R> Optional<R> query(
      @Language("MySQL") @NotNull String query,
      @NotNull SqlFunction<ResultSet, R> handler) {
    return this.query(query, stmt -> {
    }, handler);
  }

  /**
   * Executes a database query with preparation.
   *
   * <p>This will be executed on an asynchronous thread.</p>
   *
   * <p>In the case of a {@link SQLException} or in the case of
   * no data being returned, or the handler evaluating to null, this method will return an
   * {@link Optional#empty()} object.</p>
   *
   * @param query    The query to be executed
   * @param preparer The preparation used for this statement
   * @param handler  The handler for the data returned by the query
   * @param <R>      The returned type
   * @return A Promise of an asynchronous database query
   * @see #query(String, SqlFunction) to perform this query synchronously
   */
  @NotNull
  default <R> Promise<Optional<R>> queryAsync(
      @Language("MySQL") @NotNull String query,
      @NotNull SqlConsumer<PreparedStatement> preparer,
      @NotNull SqlFunction<ResultSet, R> handler) {
    return Promise.supplying(ThreadContext.ASYNC, () -> this.query(query, preparer, handler));
  }

  /**
   * Executes a database query with preparation.
   *
   * <p>This will be executed on whichever thread it's called from.</p>
   *
   * <p>In the case of a {@link SQLException} or in the case of
   * no data being returned, or the handler evaluating to null, this method will return an
   * {@link Optional#empty()} object.</p>
   *
   * @param query    The query to be executed
   * @param preparer The preparation used for this statement
   * @param handler  The handler for the data returned by the query
   * @param <R>      The returned type
   * @return The results of the database query
   * @see #queryAsync(String, SqlFunction) to perform this query asynchronously
   */
  @NotNull <R> Optional<R> query(@Language("MySQL") @NotNull String query,
      @NotNull SqlConsumer<PreparedStatement> preparer, @NotNull SqlFunction<ResultSet, R> handler);

  /**
   * Executes a batched database execution.
   *
   * <p>This will be executed on an asynchronous thread.</p>
   *
   * <p>Note that proper implementations of this method should determine
   * if the provided {@link BatchBuilder} is actually worth of being a batched statement. For
   * instance, a BatchBuilder with only one handler can safely be referred to
   * {@link #executeAsync(String, SqlConsumer)}</p>
   *
   * @param builder The builder to be used.
   * @return A Promise of an asynchronous batched database execution
   * @see #executeBatch(BatchBuilder) to perform this action synchronously
   */
  @NotNull
  default Promise<Void> executeBatchAsync(@NotNull BatchBuilder builder) {
    return Promise.supplying(ThreadContext.ASYNC,
        Delegates.runnableToSupplier(() -> this.executeBatch(builder)));
  }

  /**
   * Executes a batched database execution.
   *
   * <p>This will be executed on whichever thread it's called from.</p>
   *
   * <p>Note that proper implementations of this method should determine
   * if the provided {@link BatchBuilder} is actually worth of being a batched statement. For
   * instance, a BatchBuilder with only one handler can safely be referred to
   * {@link #execute(String, SqlConsumer)}</p>
   *
   * @param builder The builder to be used.
   * @see #executeBatchAsync(BatchBuilder) to perform this action asynchronously
   */
  void executeBatch(@NotNull BatchBuilder builder);

  /**
   * Gets a {@link BatchBuilder} for the provided statement.
   *
   * @param statement the statement to prepare for batching.
   * @return a BatchBuilder
   */
  @NotNull
  BatchBuilder batch(@Language("MySQL") @NotNull String statement);
}
