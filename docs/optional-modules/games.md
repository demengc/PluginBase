# Games

Game state management system for phased minigames.

## Installation

```xml
<dependency>
    <groupId>com.github.demengc.PluginBase</groupId>
    <artifactId>pluginbase-games</artifactId>
    <version>VERSION</version>
</dependency>
```

## GameState Class

`GameState` represents a single phase of a minigame (e.g., lobby, active game, post-game).

```java
import dev.demeng.pluginbase.games.GameState;

public class ActiveGameState extends GameState {

    private final Arena arena;
    private final Set<UUID> players;

    public ActiveGameState(Arena arena, Set<UUID> players) {
        this.arena = arena;
        this.players = players;
    }

    @Override
    protected void onStart() {
        // Called when state begins
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_SHOVEL));
                Text.tell(player, "&aGame started!");
            }
        });
    }

    @Override
    protected void onUpdate() {
        // Called periodically during the state
        // Check win conditions, etc.
    }

    @Override
    protected void onEnd() {
        // Called when state ends
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getInventory().clear();
            }
        });
    }

    @Override
    protected long getDuration() {
        // Duration in milliseconds (0 for infinite)
        return 300000; // 5 minutes
    }
}
```

## Using GameState

```java
public class SpleefPlugin extends BasePlugin {

    private GameState currentState;
    private final Set<UUID> players = new HashSet<>();

    @Override
    protected void enable() {
        // Start in lobby state
        currentState = new LobbyState(players);
        currentState.start();

        // Update state periodically
        Schedulers.sync().runRepeating(() -> {
            if (currentState != null && currentState.isStarted() && !currentState.isEnded()) {
                currentState.update();
            }
        }, 0L, 20L).bindWith(this);

        // Join command
        Lamp<BukkitCommandActor> handler = createCommandHandler();
        handler.register(new SpleefCommands(this));
    }

    public void startGame() {
        // End current state
        if (currentState != null) {
            currentState.end();
        }

        // Start active game state
        currentState = new ActiveGameState(arena, players);
        currentState.start();
    }

    public void endGame() {
        // End active state
        if (currentState != null) {
            currentState.end();
        }

        // Return to lobby
        currentState = new LobbyState(players);
        currentState.start();
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
    protected void onUpdate() {
        // Check if enough players to start
    }

    @Override
    protected void onEnd() {
        // Cleanup
    }

    @Override
    protected long getDuration() {
        return 0; // Infinite - manual transition
    }
}
```

## Binding Resources

Bind resources to the state so they're automatically cleaned up when the state ends:

```java
public class ActiveGameState extends GameState {

    @Override
    protected void onStart() {
        // Events bound to this state
        Events.subscribe(BlockBreakEvent.class)
            .handler(this::handleBreak)
            .bindWith(this);

        // Tasks bound to this state
        Schedulers.sync().runRepeating(() -> {
            updateScoreboard();
        }, 0L, 20L).bindWith(this);
    }

    @Override
    protected void onEnd() {
        // All bound resources are automatically cleaned up
    }

    // ...
}
```

## State Lifecycle

```java
GameState state = new MyGameState();

// Start the state
state.start();        // Calls onStart()
state.isStarted();    // true

// Update state
state.update();       // Calls onUpdate()

// Check if ready to end
if (state.isReadyToEnd()) {
    state.end();      // Calls onEnd()
}

state.isEnded();      // true
```

## Benefits

* **Lifecycle management** - Clear start, update, end phases
* **Automatic cleanup** - Bound resources cleaned when state ends
* **Duration control** - States can have fixed durations
* **Resource binding** - Events and tasks automatically terminate with state
