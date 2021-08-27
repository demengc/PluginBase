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

import be.bendem.sqlstreams.util.SqlConsumer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import lombok.Getter;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a statement meant to be executed more than a single time. It will be executed all at
 * once, using a single database connection.
 */
public class BatchBuilder {

  /**
   * The {@link SqlDatabase} that owns this batch builder.
   */
  @NotNull private final SqlDatabase owner;


  /**
   * The statement to be executed.
   */
  @NotNull @Getter private final String statement;

  /**
   * A linked list of PreparedStatement handlers.
   */
  @NotNull @Getter private final
  LinkedList<SqlConsumer<PreparedStatement>> handlers = new LinkedList<>();

  public BatchBuilder(@NotNull final SqlDatabase owner,
      @NotNull @Language("SQL") final String statement) {
    this.owner = owner;
    this.statement = statement;
  }

  /**
   * Adds an additional handler which will be used on the statement.
   *
   * @param handler A new statement handler
   * @return this
   */
  public BatchBuilder batch(@NotNull final SqlConsumer<PreparedStatement> handler) {
    handlers.add(handler);
    return this;
  }

  /**
   * Executes the statement for this batch, with the handlers used to prepare it.
   *
   * @throws SQLException If there is an SQL issue executing the batch
   */
  public void execute() throws SQLException {
    owner.executeBatch(this);
  }

  /**
   * Resets this BatchBuilder's handlers, making it possible to be reused.
   *
   * @return this
   */
  public BatchBuilder reset() {
    handlers.clear();
    return this;
  }
}
