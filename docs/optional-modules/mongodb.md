---
description: MongoDB database integration for document-based storage.
---

# MongoDB

The `pluginbase-mongo` module provides a thin wrapper around the MongoDB Java driver (v5.6.1, shaded and relocated) for use in Spigot plugins.

## Dependency

Add `pluginbase-mongo` as a dependency. The MongoDB driver is shaded into the module jar, so no additional runtime dependencies are needed.

```xml
<dependency>
  <groupId>dev.demeng</groupId>
  <artifactId>pluginbase-mongo</artifactId>
  <version>1.36.1-SNAPSHOT</version>
</dependency>
```

## Credentials

`MongoCredentials` holds the connection URI and database name.

| Field      | Type     | Description                                 |
|------------|----------|---------------------------------------------|
| `uri`      | `String` | MongoDB connection URI (required, non-null)  |
| `database` | `String` | Name of the default database (required, non-null) |

There are two ways to create credentials:

```java
import dev.demeng.pluginbase.mongo.MongoCredentials;

// From explicit values
MongoCredentials credentials = MongoCredentials.of(
    "mongodb://username:password@localhost:27017",
    "minecraft"
);

// From a Bukkit ConfigurationSection (keys: "uri", "database")
MongoCredentials credentials = MongoCredentials.of(
    getConfig().getConfigurationSection("mongo")
);
```

The `ConfigurationSection` overload reads `uri` (default `mongodb://localhost:27017`) and `database` (default `minecraft`).

## Creating a Connection

Pass credentials to the `Mongo` constructor. This immediately opens a connection and selects the default database.

```java
import dev.demeng.pluginbase.mongo.Mongo;

Mongo mongo = new Mongo(credentials);
```

## API Reference

`Mongo` implements `IMongo`, which extends `Terminable`.

| Method                         | Return Type     | Description                                    |
|--------------------------------|-----------------|------------------------------------------------|
| `getClient()`                  | `MongoClient`   | The underlying MongoDB client instance         |
| `getDatabase()`                | `MongoDatabase` | The default database specified in credentials  |
| `getDatabase(String name)`     | `MongoDatabase` | A database by name from the same client        |
| `close()`                      | `void`          | Closes the client connection                   |

## Basic Operations

### Insert Document

```java
import com.mongodb.client.MongoCollection;
import org.bson.Document;

MongoCollection<Document> players = mongo.getDatabase().getCollection("players");

Document player = new Document()
    .append("uuid", uuid.toString())
    .append("name", playerName)
    .append("coins", 100)
    .append("level", 1);

players.insertOne(player);
```

### Find Document

```java
import com.mongodb.client.model.Filters;

// Find one
Document player = players.find(
    Filters.eq("uuid", uuid.toString())
).first();

if (player != null) {
    int coins = player.getInteger("coins");
    int level = player.getInteger("level");
}

// Find many
for (Document doc : players.find(Filters.gte("level", 10))) {
    String name = doc.getString("name");
    int level = doc.getInteger("level");
}
```

### Update Document

```java
import com.mongodb.client.model.Updates;

// Update single field
players.updateOne(
    Filters.eq("uuid", uuid.toString()),
    Updates.set("coins", 500)
);

// Update multiple fields
players.updateOne(
    Filters.eq("uuid", uuid.toString()),
    Updates.combine(
        Updates.set("coins", 500),
        Updates.set("level", 5)
    )
);

// Increment value
players.updateOne(
    Filters.eq("uuid", uuid.toString()),
    Updates.inc("coins", 100)
);
```

### Delete Document

```java
players.deleteOne(Filters.eq("uuid", uuid.toString()));
```

## Queries

```java
import com.mongodb.client.model.Sorts;

// Top players by coins
List<Document> top = players
    .find()
    .sort(Sorts.descending("coins"))
    .limit(10)
    .into(new ArrayList<>());

// Players with level >= 10
List<Document> highLevel = players
    .find(Filters.gte("level", 10))
    .into(new ArrayList<>());

// Compound filter
List<Document> results = players
    .find(Filters.and(
        Filters.gte("level", 5),
        Filters.lte("coins", 1000)
    ))
    .into(new ArrayList<>());
```

## Indexes

```java
import com.mongodb.client.model.Indexes;

players.createIndex(Indexes.ascending("uuid"));
players.createIndex(Indexes.descending("coins"));
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    private Mongo mongo;
    private MongoCollection<Document> players;

    @Override
    protected DependencyContainer configureDependencies() {
        MongoCredentials credentials = MongoCredentials.of(
            getConfig().getConfigurationSection("mongo")
        );

        this.mongo = new Mongo(credentials);
        this.players = mongo.getDatabase().getCollection("players");

        return DependencyInjection.builder()
            .register(this)
            .register(Mongo.class, mongo)
            .build();
    }

    @Override
    protected void enable() {
        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> loadPlayer(e.getPlayer()))
            .bindWith(this);

        Events.subscribe(PlayerQuitEvent.class)
            .handler(e -> savePlayer(e.getPlayer()))
            .bindWith(this);
    }

    private void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        Schedulers.async().supply(() -> {
            return players.find(Filters.eq("uuid", uuid.toString())).first();
        }).thenApplySync(doc -> {
            if (doc != null) {
                PlayerData data = new PlayerData(
                    uuid,
                    doc.getString("name"),
                    doc.getInteger("coins"),
                    doc.getInteger("level")
                );
                cache.put(uuid, data);
                Text.tell(player, "&aData loaded!");
            } else {
                createPlayer(uuid, player.getName());
            }
            return doc;
        });
    }

    private void savePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = cache.get(uuid);

        if (data != null) {
            Schedulers.async().run(() -> {
                Document doc = new Document()
                    .append("uuid", uuid.toString())
                    .append("name", player.getName())
                    .append("coins", data.getCoins())
                    .append("level", data.getLevel())
                    .append("lastLogin", System.currentTimeMillis());

                players.replaceOne(
                    Filters.eq("uuid", uuid.toString()),
                    doc,
                    new com.mongodb.client.model.ReplaceOptions().upsert(true)
                );
            });
        }
    }

    private void createPlayer(UUID uuid, String name) {
        Document doc = new Document()
            .append("uuid", uuid.toString())
            .append("name", name)
            .append("coins", 0)
            .append("level", 1);

        players.insertOne(doc);
        cache.put(uuid, new PlayerData(uuid, name, 0, 1));
    }
}
```

## Cleanup

`Mongo` implements `Terminable`. The connection is closed automatically when the plugin disables or when `close()` is called manually.
