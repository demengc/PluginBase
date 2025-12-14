---
description: >-
  Automatic resource lifecycle management for events, tasks, and custom
  resources.
---

# Terminables

## What are Terminables?

Terminables are resources that can be automatically cleaned up when no longer needed. Using `.bindWith()` ensures resources are properly closed when your plugin disables.

## Basic Usage

### Events

```java
Events.subscribe(PlayerJoinEvent.class)
    .handler(e -> {})
    .bindWith(this);  // Auto-unregister on plugin disable
```

### Schedulers

```java
Schedulers.sync().runRepeating(() -> {
    // Repeating task
}, 0L, 20L).bindWith(this);  // Auto-cancel on plugin disable
```

### Custom Resources

```java
public class DatabaseConnection implements Terminable {
    private Connection connection;

    public DatabaseConnection() {
        this.connection = openConnection();
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.close();
        }
    }
}

// Usage
DatabaseConnection db = new DatabaseConnection();
bind(db);  // Auto-close on plugin disable
```

## CompositeTerminable

Group multiple terminables together:

```java
public class GameArena {
    private final CompositeTerminable terminables = CompositeTerminable.create();

    public void start() {
        // Register events for this arena
        Events.subscribe(PlayerMoveEvent.class)
            .filter(e -> isInArena(e.getPlayer()))
            .handler(this::handleMove)
            .bindWith(terminables);

        // Start arena tasks
        Schedulers.sync().runRepeating(() -> {
            updateArena();
        }, 0L, 20L).bindWith(terminables);
    }

    public void stop() {
        // Clean up ALL arena resources at once
        terminables.close();
    }
}
```

## TerminableModule

Organize related functionality:

```java
public class ScoreboardModule implements TerminableModule {

    @Override
    public void setup(TerminableConsumer consumer) {
        // Events
        Events.subscribe(PlayerJoinEvent.class)
            .handler(this::createScoreboard)
            .bindWith(consumer);

        // Tasks
        Schedulers.sync().runRepeating(() -> {
            updateScoreboards();
        }, 0L, 20L).bindWith(consumer);
    }

    private void createScoreboard(PlayerJoinEvent event) {
        // Create scoreboard for player
    }

    private void updateScoreboards() {
        // Update all scoreboards
    }
}

// Usage in plugin
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        bindModule(new ScoreboardModule());  // Auto-cleanup on disable
    }
}
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        // Events - auto cleanup
        Events.subscribe(PlayerJoinEvent.class)
            .handler(this::onJoin)
            .bindWith(this);

        // Tasks - auto cleanup
        Schedulers.sync().runRepeating(() -> {
            saveData();
        }, 0L, 6000L).bindWith(this);

        // Modules - auto cleanup
        bindModule(new ChatModule());
        bindModule(new ScoreboardModule());

        // Custom resources - auto cleanup
        DatabaseConnection db = new DatabaseConnection();
        bind(db);
    }

    @Override
    protected void disable() {
        // All resources automatically cleaned up!
        // No manual unregister/cancel needed
    }
}
```

## Benefits

✅ **No memory leaks** - Resources always cleaned up ✅ **Less boilerplate** - No manual unregister code ✅ **Safer** - Can't forget to clean up ✅ **Organized** - Group related resources together

## Without Terminables

```java
// Traditional approach - easy to forget cleanup!
private Task task;
private Listener listener;

@Override
public void onEnable() {
    this.listener = new MyListener();
    getServer().getPluginManager().registerEvents(listener, this);

    this.task = Bukkit.getScheduler().runTaskTimer(this, () -> {
        // Task logic
    }, 0L, 20L);
}

@Override
public void onDisable() {
    HandlerList.unregisterAll(listener);  // Easy to forget!
    task.cancel();  // Easy to forget!
}
```

## With Terminables

```java
// PluginBase approach - automatic!
@Override
protected void enable() {
    Events.subscribe(PlayerJoinEvent.class)
        .handler(e -> {})
        .bindWith(this);  // Auto cleanup

    Schedulers.sync().runRepeating(() -> {}, 0L, 20L)
        .bindWith(this);  // Auto cleanup
}

@Override
protected void disable() {
    // Nothing to do - automatic cleanup!
}
```
