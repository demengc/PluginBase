---
description: Simple DI system for managing class dependencies with constructor injection.
---

# Dependency Injection

## Basic Usage

### Registration

```java
@Override
protected DependencyContainer configureDependencies() {
    // Create instances
    Sql sql = new Sql(null, null, credentials);
    DataStorage storage = new MySQLStorage(sql);

    // Register dependencies
    return DependencyInjection.builder()
        .register(this)  // Register plugin instance
        .register(Sql.class, sql)
        .register(DataStorage.class, storage)
        .build();
}
```

### Using @Component

```java
@Component
public class PlayerManager {
    private final DataStorage storage;

    // Constructor injection
    public PlayerManager(DataStorage storage) {
        this.storage = storage;
    }
}
```

Components are automatically created when first requested and reused (singleton).

### Getting Dependencies Manually

```java
// In BasePlugin
PlayerManager manager = getDependency(PlayerManager.class);

// Anywhere else (requires access to plugin instance)
PlayerManager manager = plugin.getDependency(PlayerManager.class);
// Or via the container directly
PlayerManager manager = plugin.getDependencyContainer().getInstance(PlayerManager.class);
```

## Complete Example

```java
// Interface
public interface DataStorage {
    void save(UUID uuid, PlayerData data);
    PlayerData load(UUID uuid);
}

// Implementation
public class MySQLStorage implements DataStorage {
    private final Sql sql;

    public MySQLStorage(Sql sql) {
        this.sql = sql;
    }

    @Override
    public void save(UUID uuid, PlayerData data) {
        sql.execute("INSERT INTO players VALUES (?, ?)", stmt -> {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, data.serialize());
        });
    }

    @Override
    public PlayerData load(UUID uuid) {
        return sql.query("SELECT * FROM players WHERE uuid = ?",
            stmt -> stmt.setString(1, uuid.toString()),
            rs -> rs.next() ? PlayerData.deserialize(rs.getString("data")) : null
        ).orElse(null);
    }
}

// Component using the storage
@Component
public class PlayerManager {
    private final DataStorage storage;

    public PlayerManager(DataStorage storage) {
        this.storage = storage;
    }

    public void savePlayer(UUID uuid, PlayerData data) {
        storage.save(uuid, data);
    }
}

// Plugin setup
public class MyPlugin extends BasePlugin {
    @Override
    protected DependencyContainer configureDependencies() {
        Sql sql = new Sql(null, null, credentials);
        DataStorage storage = new MySQLStorage(sql);

        return DependencyInjection.builder()
            .register(this)
            .register(Sql.class, sql)
            .register(DataStorage.class, storage)  // Register by interface
            .build();
    }

    @Override
    protected void enable() {
        PlayerManager manager = getDependency(PlayerManager.class);
        // Use manager...
    }
}
```
