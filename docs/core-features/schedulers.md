---
description: Execute tasks synchronously and asynchronously with fluent API.
---

# Schedulers

`Schedulers` provides static access to sync and async `Scheduler` instances. Each `Scheduler` returns `Promise` objects from one-shot methods and `Task` objects from repeating methods, both of which implement `Terminable` for automatic lifecycle management.

## Scheduler access

| Method | Returns | Description |
|---|---|---|
| `Schedulers.sync()` | `Scheduler` | Main server thread |
| `Schedulers.async()` | `Scheduler` | Async thread pool |
| `Schedulers.get(ThreadContext)` | `Scheduler` | Determined by `SYNC` or `ASYNC` context |
| `Schedulers.bukkit()` | `BukkitScheduler` | Raw Bukkit scheduler |
| `Schedulers.builder()` | `TaskBuilder` | Fluent task builder (see below) |

## Scheduler methods

All one-shot methods return `Promise<T>` (or `Promise<Void>` for `run`/`runLater`). Repeating methods return `Task`.

| Method | Parameters | Description |
|---|---|---|
| `run(Runnable)` | runnable | Execute immediately |
| `supply(Supplier<T>)` | supplier | Execute immediately, return value |
| `call(Callable<T>)` | callable | Execute immediately, return value (checked exceptions) |
| `runLater(Runnable, long)` | runnable, delayTicks | Execute after delay |
| `supplyLater(Supplier<T>, long)` | supplier, delayTicks | Execute after delay, return value |
| `callLater(Callable<T>, long)` | callable, delayTicks | Execute after delay, return value |
| `runRepeating(Runnable, long, long)` | runnable, delayTicks, intervalTicks | Repeat on interval |
| `runRepeating(Consumer<Task>, long, long)` | consumer, delayTicks, intervalTicks | Repeat with access to the `Task` handle |

`runLater`, `supplyLater`, `callLater`, and `runRepeating` each have a `TimeUnit` overload (e.g., `runLater(Runnable, long, TimeUnit)`).

## Sync and async task execution

```java
Schedulers.sync().run(() -> {
    player.teleport(location);
});

Schedulers.sync().runLater(() -> {
    player.sendMessage("Delayed message");
}, 20L);

Schedulers.sync().runRepeating(() -> {
    player.sendActionBar("Repeating message");
}, 0L, 20L);
```

```java
Schedulers.async().run(() -> {
    PlayerData data = database.load(uuid);
});

Schedulers.async().runLater(() -> {
    database.cleanup();
}, 20L);

Schedulers.async().runRepeating(() -> {
    database.heartbeat();
}, 0L, 100L);
```

## Promises

All one-shot methods (`run`, `supply`, `call`, and their `*Later` variants) return a `Promise<T>`, which supports chaining across thread contexts.

**Promise chain methods:**

| Method | Runs on | Input | Output |
|---|---|---|---|
| `thenApplySync(Function<V, U>)` | main thread | value | value |
| `thenApplyAsync(Function<V, U>)` | async thread | value | value |
| `thenAcceptSync(Consumer<V>)` | main thread | value | void |
| `thenAcceptAsync(Consumer<V>)` | async thread | value | void |
| `thenRunSync(Runnable)` | main thread | nothing | void |
| `thenRunAsync(Runnable)` | async thread | nothing | void |
| `thenComposeSync(Function<V, Promise<U>>)` | main thread | value | flattened promise |
| `thenComposeAsync(Function<V, Promise<U>>)` | async thread | value | flattened promise |

Each of these also has a delayed variant (e.g., `thenApplyDelayedSync(Function, long)` and `thenApplyDelayedSync(Function, long, TimeUnit)`).

```java
Schedulers.async().supply(() -> {
    return database.loadPlayer(uuid);
}).thenAcceptSync(data -> {
    player.sendMessage("Data loaded: " + data);
});
```

## TaskBuilder API

`Schedulers.builder()` provides a fluent builder for constructing tasks with explicit thread context and timing.

**Builder chain:**

`Schedulers.builder()` -> `.sync()` or `.async()` -> timing -> terminal

**Timing methods** (on `ThreadContextual`):

| Method | Description |
|---|---|
| `now()` | Execute immediately. Returns `ContextualPromiseBuilder`. |
| `after(long ticks)` | Execute after delay. Returns `DelayedTick`. |
| `after(long, TimeUnit)` | Execute after delay. Returns `DelayedTime`. |
| `every(long ticks)` | Repeat with no initial delay. Returns `ContextualTaskBuilder`. |
| `afterAndEvery(long ticks)` | Delay then repeat at same interval. Returns `ContextualTaskBuilder`. |
| `afterAndEvery(long, TimeUnit)` | Delay then repeat at same interval. Returns `ContextualTaskBuilder`. |

**Terminal methods:**

`ContextualPromiseBuilder` (one-shot): `run(Runnable)`, `supply(Supplier<T>)`, `call(Callable<T>)`

`ContextualTaskBuilder` (repeating): `run(Runnable)`, `consume(Consumer<Task>)`

`DelayedTick`/`DelayedTime` extend `ContextualPromiseBuilder` and add `.every(...)` to convert into a repeating task.

```java
Schedulers.builder()
    .async()
    .afterAndEvery(5, TimeUnit.MINUTES)
    .run(() -> saveAllPlayerData());

Schedulers.builder()
    .sync()
    .after(60)
    .run(() -> player.teleport(spawn));

Schedulers.builder()
    .async()
    .now()
    .supply(() -> database.loadPlayer(uuid))
    .thenAcceptSync(data -> applyPlayerData(player, data));
```

## Binding tasks for automatic cleanup

`Task` extends `Terminable`, so repeating tasks can be bound to any `TerminableConsumer`. `Promise` also extends `Terminable`.

```java
Schedulers.sync().runRepeating(() -> {
    updateScoreboard();
}, 0L, 20L).bindWith(this);

Schedulers.builder()
    .async()
    .every(100)
    .run(() -> database.heartbeat())
    .bindWith(this);
```

## Tick conversions

The `Ticks` utility converts between ticks and standard `TimeUnit` values.

| Method | Description |
|---|---|
| `Ticks.from(long duration, TimeUnit unit)` | Convert duration to ticks |
| `Ticks.to(long ticks, TimeUnit unit)` | Convert ticks to duration |

| Constant | Value |
|---|---|
| `Ticks.TICKS_PER_SECOND` | 20 |
| `Ticks.MILLISECONDS_PER_SECOND` | 1000 |
| `Ticks.MILLISECONDS_PER_TICK` | 50 |

## Thread safety

The Bukkit API is not thread-safe. Only call Bukkit API methods from the main thread.

```java
// WRONG: Bukkit API from async thread
Schedulers.async().run(() -> {
    player.teleport(location);
});

// CORRECT: async computation, sync application
Schedulers.async().supply(() -> {
    return database.loadData();
}).thenAcceptSync(data -> {
    player.teleport(location);
});
```

## Complete example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();
                UUID uuid = player.getUniqueId();

                Schedulers.async().supply(() -> loadPlayerData(uuid))
                    .thenAcceptSync(data -> {
                        applyPlayerData(player, data);
                        Text.tell(player, "&aData loaded!");
                    });
            })
            .bindWith(this);

        Schedulers.builder()
            .async()
            .afterAndEvery(5, TimeUnit.MINUTES)
            .run(() -> saveAllPlayerData())
            .bindWith(this);

        Events.subscribe(PlayerInteractEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();
                Text.tell(player, "&7Teleporting in 3 seconds...");

                Schedulers.sync().runLater(() -> {
                    player.teleport(spawn);
                    Text.tell(player, "&aTeleported!");
                }, 60L);
            })
            .bindWith(this);
    }
}
```
