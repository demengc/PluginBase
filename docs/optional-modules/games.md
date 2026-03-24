---
description: Game state management system for phased minigames.
---

# Games

The `pluginbase-games` module provides a state machine for minigame phases. Add it as a dependency alongside `pluginbase-core`:

```xml
<dependency>
    <groupId>dev.demeng</groupId>
    <artifactId>pluginbase-games</artifactId>
    <version>1.36.1-SNAPSHOT</version>
</dependency>
```

## GameState

`GameState` is an abstract class representing a single phase of a minigame (lobby, active round, post-game, etc.). Each state has a lifecycle driven by four abstract methods:

| Method | Visibility | Description |
|---|---|---|
| `onStart()` | `protected` | Called once when `start()` is invoked. |
| `onUpdate()` | `protected` | Called on each tick while the state is active. |
| `onEnd()` | `protected` | Called once when `end()` is invoked. Bound resources are closed before this runs. |
| `getDuration()` | `protected` | Returns the state duration in milliseconds. |

Public control methods:

| Method | Returns | Description |
|---|---|---|
| `start()` | `void` | Starts the state. No-op if already started or ended. |
| `update()` | `void` | Ticks the state. Calls `end()` automatically if `isReadyToEnd()` returns true, otherwise calls `onUpdate()`. |
| `end()` | `void` | Ends the state: closes bound resources, then calls `onEnd()`. No-op if not started or already ended. |
| `isStarted()` | `boolean` | Whether `start()` has been called. |
| `isEnded()` | `boolean` | Whether `end()` has been called. |
| `getStartTime()` | `long` | Epoch millis when the state started, or 0 if not started. |
| `getRemainingDuration()` | `long` | Milliseconds remaining based on `getDuration()`, minimum 0. |
| `bind(AutoCloseable)` | `<T>` | Binds a resource for automatic cleanup when the state ends. Returns the same object. |
| `bindModule(TerminableModule)` | `<T>` | Binds a `TerminableModule` for automatic cleanup. Returns the same module. |

### Duration and end conditions

`isReadyToEnd()` is a `protected` method that `update()` calls each tick to decide whether to end the state. The default implementation returns true when `getRemainingDuration() <= 0`, which means the elapsed time since start has exceeded `getDuration()`.

If `getDuration()` returns 0, the state will end on its first `update()` call. There is no built-in "infinite duration" sentinel. To create a state that runs indefinitely until manually ended, override `isReadyToEnd()`:

```java
@Override
protected boolean isReadyToEnd() {
    return false;
}

@Override
protected long getDuration() {
    return 0;
}
```

For custom end conditions (player count, score threshold, etc.), override `isReadyToEnd()`:

```java
@Override
protected boolean isReadyToEnd() {
    return alivePlayers.size() <= 1;
}
```

### Minimal example

```java
public class ActiveGameState extends GameState {

    private final Arena arena;
    private final Set<UUID> players;

    public ActiveGameState(Arena arena, Set<UUID> players) {
        this.arena = arena;
        this.players = players;
    }

    @Override
    protected void onStart() {
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_SHOVEL));
                Text.tell(player, "&aGame started!");
            }
        });
    }

    @Override
    protected void onUpdate() {}

    @Override
    protected void onEnd() {
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getInventory().clear();
            }
        });
    }

    @Override
    protected long getDuration() {
        return 300_000; // 5 minutes
    }
}
```

## Binding resources to a state

`bind(AutoCloseable)` registers a resource (event subscription, scheduled task, etc.) for automatic cleanup when the state ends. All bound resources are closed before `onEnd()` is called.

`GameState` does **not** implement `TerminableConsumer`, so you cannot use `.bindWith(this)` inside a state. Instead, wrap the resource with `bind()`:

```java
public class ActiveGameState extends GameState {

    @Override
    protected void onStart() {
        bind(Events.subscribe(BlockBreakEvent.class)
            .handler(this::handleBreak));

        bind(Schedulers.sync().runRepeating(() -> {
            updateScoreboard();
        }, 0L, 20L));
    }

    @Override
    protected void onUpdate() {}

    @Override
    protected void onEnd() {}

    @Override
    protected long getDuration() {
        return 300_000;
    }

    private void handleBreak(BlockBreakEvent event) {
        // ...
    }
}
```

## ScheduledStateSeries

`ScheduledStateSeries` extends `GameState` and runs a sequence of states one after another. Each child state's `update()` is called on a fixed tick interval. When a child state's `isReadyToEnd()` returns true, it is ended and the next state starts.

Constructors:

| Constructor | Description |
|---|---|
| `ScheduledStateSeries(GameState...)` | States in order, 1-tick update interval. |
| `ScheduledStateSeries(List<GameState>)` | States from a list, 1-tick update interval. |
| `ScheduledStateSeries(long, GameState...)` | Custom interval (in ticks), states in order. |
| `ScheduledStateSeries(long, List<GameState>)` | Custom interval (in ticks), states from a list. |
| `ScheduledStateSeries()` | Empty series, 1-tick interval. |
| `ScheduledStateSeries(long)` | Empty series, custom interval. |

The `addNext(GameState...)` and `addNext(List<GameState>)` methods insert states immediately after the current one, pushing later states back. Useful for dynamic phase injection (overtime, tiebreaker, etc.).

### Full example

```java
public class SpleefPlugin extends BasePlugin {

    private final Set<UUID> players = new HashSet<>();

    @Override
    protected void enable() {
        LobbyState lobby = new LobbyState(players);
        ActiveGameState active = new ActiveGameState(arena, players);
        PostGameState postGame = new PostGameState();

        ScheduledStateSeries series = new ScheduledStateSeries(lobby, active, postGame);
        series.start();
    }
}

public class LobbyState extends GameState {

    private final Set<UUID> players;

    public LobbyState(Set<UUID> players) {
        this.players = players;
    }

    @Override
    protected void onStart() {
        Text.broadcast(null, "&aWaiting for players...");
    }

    @Override
    protected void onUpdate() {}

    @Override
    protected void onEnd() {}

    @Override
    protected long getDuration() {
        return 30_000; // 30 second lobby countdown
    }
}
```

### Manual state management (without ScheduledStateSeries)

If you need full control over transitions, drive `update()` yourself:

```java
public class SpleefPlugin extends BasePlugin {

    private GameState currentState;

    @Override
    protected void enable() {
        currentState = new LobbyState(players);
        currentState.start();

        Schedulers.sync().runRepeating(() -> {
            if (currentState != null && currentState.isStarted() && !currentState.isEnded()) {
                currentState.update();
            }
        }, 0L, 20L).bindWith(this);
    }

    public void transition(GameState next) {
        if (currentState != null) {
            currentState.end();
        }
        currentState = next;
        currentState.start();
    }
}
```

Note: `.bindWith(this)` is valid here because `BasePlugin` implements `TerminableConsumer`. Inside a `GameState` subclass, use `bind()` instead.
