# Schedulers

Execute tasks synchronously and asynchronously with fluent API.

## Basic Usage

### Sync Tasks

```java
// Run on main thread
Schedulers.sync().run(() -> {
    player.teleport(location);
});

// Delayed (20 ticks = 1 second)
Schedulers.sync().runLater(() -> {
    player.sendMessage("Delayed message");
}, 20L);

// Repeating (every 20 ticks)
Schedulers.sync().runRepeating(() -> {
    player.sendActionBar("Repeating message");
}, 0L, 20L);
```

### Async Tasks

```java
// Run off main thread
Schedulers.async().run(() -> {
    // Heavy computation, database queries, etc.
    PlayerData data = database.load(uuid);
});

// Delayed async
Schedulers.async().runLater(() -> {
    // Heavy async task
}, 20L);

// Repeating async
Schedulers.async().runRepeating(() -> {
    // Periodic background task
}, 0L, 100L);
```

## Promises

Async computations with callbacks:

```java
// Supply value asynchronously
Schedulers.async().supply(() -> {
    // Heavy computation
    return database.loadPlayer(uuid);
}).thenApplySync(data -> {
    // Back on main thread
    player.sendMessage("Data loaded: " + data);
    return data;
});
```

## Binding Tasks

Automatically cancel tasks when plugin disables:

```java
Schedulers.sync().runRepeating(() -> {
    // This task auto-cancels on plugin disable
}, 0L, 20L).bindWith(this);
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        // Load player data async, apply sync
        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();
                UUID uuid = player.getUniqueId();

                // Async database load
                Schedulers.async().supply(() -> loadPlayerData(uuid))
                    .thenApplySync(data -> {
                        // Back on main thread
                        applyPlayerData(player, data);
                        Text.tell(player, "&aData loaded!");
                        return data;
                    });
            })
            .bindWith(this);

        // Periodic save (every 5 minutes)
        Schedulers.async().runRepeating(() -> {
            saveAllPlayerData();
        }, 0L, 20L * 60 * 5)  // 0 delay, 5 minutes interval
            .bindWith(this);

        // Delayed teleport
        Events.subscribe(PlayerInteractEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();
                Text.tell(player, "&7Teleporting in 3 seconds...");

                Schedulers.sync().runLater(() -> {
                    player.teleport(spawn);
                    Text.tell(player, "&aTeleported!");
                }, 60L);  // 3 seconds = 60 ticks
            })
            .bindWith(this);
    }

    private PlayerData loadPlayerData(UUID uuid) {
        // Database query (async safe)
        return database.load(uuid);
    }

    private void applyPlayerData(Player player, PlayerData data) {
        // Apply to player (must be on main thread)
        player.setLevel(data.getLevel());
    }

    private void saveAllPlayerData() {
        // Save all data (async safe)
        database.saveAll();
    }
}
```

## Thread Safety

**Important**: Bukkit API is not thread-safe!

```java
// WRONG - Bukkit API from async thread
Schedulers.async().run(() -> {
    player.teleport(location);  // ERROR! Not thread-safe!
});

// CORRECT - Bukkit API on main thread
Schedulers.async().supply(() -> {
    return database.loadData();  // Heavy work async
}).thenApplySync(data -> {
    player.teleport(location);  // Bukkit API on main thread
    return data;
});
```

## Time Units

* **1 tick** = 50ms (1/20th second)
* **20 ticks** = 1 second
* **1200 ticks** = 1 minute
* **72000 ticks** = 1 hour
