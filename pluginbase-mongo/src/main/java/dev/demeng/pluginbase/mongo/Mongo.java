/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

package dev.demeng.pluginbase.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an individual Mongo datasource, created by the library.
 */
public class Mongo implements IMongo {

  @Getter @NotNull private final MongoClient client;
  @Getter @NotNull private final MongoDatabase database;

  public Mongo(@NotNull final MongoCredentials credentials) {
    this.client = MongoClients.create(credentials.getUri());
    this.database = this.client.getDatabase(credentials.getDatabase());
  }

  @Override
  public MongoDatabase getDatabase(final String name) {
    return this.client.getDatabase(name);
  }

  @Override
  public void close() {
    if (this.client != null) {
      this.client.close();
    }
  }
}
