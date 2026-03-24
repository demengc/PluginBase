---
description: >-
  Automatic resource lifecycle management for events, tasks, and custom
  resources.
---

# Terminables

Terminables represent resources with a defined lifecycle. Binding a `Terminable` to a `TerminableConsumer` (such as `BasePlugin`) guarantees cleanup when the consumer shuts down, eliminating manual unregister and cancel calls.

## Interface hierarchy

| Interface | Extends | Role |
|---|---|---|
| `Terminable` | `AutoCloseable` | Single closeable resource. Provides `close()`, `bindWith(TerminableConsumer)`, `closeSilently()`, `isClosed()`. |
| `TerminableConsumer` | -- | Accepts resources via `bind(AutoCloseable)` and `bindModule(TerminableModule)`. |
| `CompositeTerminable` | `Terminable`, `TerminableConsumer` | Groups multiple terminables. Closes in LIFO order. Created via `CompositeTerminable.create()`. |
| `TerminableModule` | -- | Encapsulates related setup logic. Receives a `TerminableConsumer` in `setup(TerminableConsumer)`. |
| `Task` | `Terminable` | Repeating scheduler task. `close()` delegates to `stop()`. |
| `Promise<V>` | `Future<V>`, `Terminable` | Async computation result. |

`BasePlugin` implements `TerminableConsumer`, so `bind()` and `bindModule()` are available directly via `this` in any `BasePlugin` subclass.

## Binding resources

### Events

```java
Events.subscribe(PlayerJoinEvent.class)
    .handler(e -> {})
    .bindWith(this);
```

### Scheduler tasks

```java
Schedulers.sync().runRepeating(() -> {
    updateScoreboard();
}, 0L, 20L).bindWith(this);
```

### Custom resources

Any class implementing `Terminable` (or `AutoCloseable`) can be bound.

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

DatabaseConnection db = new DatabaseConnection();
bind(db);
```

## CompositeTerminable

Groups multiple terminables under a single handle. Useful for subsystems that need independent lifecycle control.

```java
public class GameArena {
    private final CompositeTerminable terminables = CompositeTerminable.create();

    public void start() {
        Events.subscribe(PlayerMoveEvent.class)
            .filter(e -> isInArena(e.getPlayer()))
            .handler(this::handleMove)
            .bindWith(terminables);

        Schedulers.sync().runRepeating(() -> {
            updateArena();
        }, 0L, 20L).bindWith(terminables);
    }

    public void stop() {
        terminables.close();
    }
}
```

`CompositeTerminable` also provides:

| Method | Description |
|---|---|
| `create()` | New instance with strong references |
| `createWeak()` | New instance with weak references |
| `with(AutoCloseable)` | Add a resource (returns `this` for chaining) |
| `withAll(AutoCloseable...)` | Add multiple resources |
| `withAll(Iterable<AutoCloseable>)` | Add multiple resources from iterable |
| `cleanup()` | Remove already-terminated entries |
| `bind(AutoCloseable)` | Same as `with()`, returns the bound resource |
| `bindModule(TerminableModule)` | Inherited from `TerminableConsumer` |

## TerminableModule

Encapsulates a group of related resources behind a `setup()` method. The consumer passed to `setup()` handles binding.

```java
public class ScoreboardModule implements TerminableModule {

    @Override
    public void setup(TerminableConsumer consumer) {
        Events.subscribe(PlayerJoinEvent.class)
            .handler(this::createScoreboard)
            .bindWith(consumer);

        Schedulers.sync().runRepeating(() -> {
            updateScoreboards();
        }, 0L, 20L).bindWith(consumer);
    }
}

public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        bindModule(new ScoreboardModule());
    }
}
```

## Complete example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        Events.subscribe(PlayerJoinEvent.class)
            .handler(this::onJoin)
            .bindWith(this);

        Schedulers.sync().runRepeating(() -> {
            saveData();
        }, 0L, 6000L).bindWith(this);

        bindModule(new ChatModule());
        bindModule(new ScoreboardModule());

        DatabaseConnection db = new DatabaseConnection();
        bind(db);
    }

    @Override
    protected void disable() {
        // All bound resources are automatically cleaned up.
    }
}
```

## Comparison with manual cleanup

Without terminables, you must track and cancel every resource individually:

```java
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
    HandlerList.unregisterAll(listener);
    task.cancel();
}
```

With terminables, binding handles all of this:

```java
@Override
protected void enable() {
    Events.subscribe(PlayerJoinEvent.class)
        .handler(e -> {})
        .bindWith(this);

    Schedulers.sync().runRepeating(() -> {}, 0L, 20L)
        .bindWith(this);
}

@Override
protected void disable() {
    // Nothing needed.
}
```
