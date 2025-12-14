# MongoDB Module

MongoDB database integration for document-based storage.

## Installation

```xml
<dependency>
    <groupId>com.github.demengc.PluginBase</groupId>
    <artifactId>pluginbase-mongo</artifactId>
    <version>VERSION</version>
</dependency>
```

## Setup

```java
import dev.demeng.pluginbase.mongo.Mongo;
import dev.demeng.pluginbase.mongo.MongoCredentials;

// Create credentials with connection URI
MongoCredentials credentials = MongoCredentials.of(
    "mongodb://username:password@localhost:27017",
    "minecraft"
);

// Or simple localhost connection
MongoCredentials credentials = MongoCredentials.of(
    "mongodb://localhost:27017",
    "minecraft"
);

// Create Mongo instance
Mongo mongo = new Mongo(credentials);
```

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

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    private Mongo mongo;
    private MongoCollection<Document> players;

    @Override
    protected DependencyContainer configureDependencies() {
        String uri = getConfig().getString("mongo.uri", "mongodb://localhost:27017");
        String database = getConfig().getString("mongo.database", "minecraft");

        MongoCredentials credentials = MongoCredentials.of(uri, database);

        this.mongo = new Mongo(credentials);
        this.players = mongo.getDatabase().getCollection("players");

        return DependencyInjection.builder()
            .register(this)
            .register(Mongo.class, mongo)
            .build();
    }

    @Override
    protected void enable() {
        // Load on join
        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> loadPlayer(e.getPlayer()))
            .bindWith(this);

        // Save on quit
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
                // Apply loaded data
                PlayerData data = new PlayerData(
                    uuid,
                    doc.getString("name"),
                    doc.getInteger("coins"),
                    doc.getInteger("level")
                );
                cache.put(uuid, data);
                Text.tell(player, "&aData loaded!");
            } else {
                // Create new player
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

// Complex query
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

// Create index for faster queries
players.createIndex(Indexes.ascending("uuid"));
players.createIndex(Indexes.descending("coins"));
```

## Cleanup

MongoDB connections are automatically closed when plugin disables (implements AutoCloseable).
