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

package dev.demeng.pluginbase.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import dev.demeng.pluginbase.plugin.BaseManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.DefaultCreator;

/**
 * Represents an individual Mongo datasource, created by the library.
 */
public class Mongo implements IMongo {

  @Getter @NotNull private final MongoClient client;
  @Getter @NotNull private final MongoDatabase database;
  @Getter private final Morphia morphia;
  @Getter private final Datastore morphiaDatastore;

  public Mongo(@NotNull final DatabaseCredentials credentials) {

    if (credentials.getUri() == null || credentials.getUri().isEmpty()) {
      final MongoCredential mongoCredential = MongoCredential.createCredential(
          credentials.getUser(),
          credentials.getDatabase(),
          credentials.getPassword().toCharArray()
      );

      this.client = new MongoClient(
          new ServerAddress(credentials.getHost(), credentials.getPort()),
          mongoCredential,
          MongoClientOptions.builder().build()
      );
    } else {
      this.client = new MongoClient(new MongoClientURI(credentials.getUri()));
    }

    this.database = this.client.getDatabase(credentials.getDatabase());
    this.morphia = new Morphia();
    this.morphiaDatastore = this.morphia.createDatastore(this.client, credentials.getDatabase());
    this.morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator() {
      @Override
      protected ClassLoader getClassLoaderForClass() {
        return BaseManager.getPlugin().getClass().getClassLoader();
      }
    });
  }

  @Override
  public MongoDatabase getDatabase(final String name) {
    return this.client.getDatabase(name);
  }

  @Override
  public Datastore getMorphiaDatastore(final String name) {
    return this.morphia.createDatastore(this.client, name);
  }

  @Override
  public void close() {
    this.client.close();
  }
}
