# Redis

Redis pub/sub for cross-server messaging.

## Installation

```xml
<dependency>
    <groupId>com.github.demengc.PluginBase</groupId>
    <artifactId>pluginbase-redis</artifactId>
    <version>VERSION</version>
</dependency>
```

## Setup

```java
import dev.demeng.pluginbase.redis.Redis;
import dev.demeng.pluginbase.redis.RedisCredentials;

// Create credentials
RedisCredentials credentials = RedisCredentials.of(
    "localhost",
    6379,
    null,           // username (optional)
    "password",     // password (optional)
    false           // SSL
);

// Or for localhost without auth
RedisCredentials credentials = RedisCredentials.of(
    "localhost",
    6379,
    null,
    null,
    false
);

// Create Redis instance with server ID
Redis redis = new Redis("server-1", credentials);
```

## Pub/Sub Messaging

### Publishing Messages

```java
// Publish string message
redis.publishString("chat", "Hello from server 1!");

// Publish object (serialized to JSON)
PlayerData data = new PlayerData(uuid, name, coins);
redis.publishObject("player-data", data);
```

### Receiving Messages

Use Events to receive published messages:

```java
import dev.demeng.pluginbase.redis.event.AsyncRedisMessageReceiveEvent;

// Subscribe to channel
redis.subscribe("chat");

// Listen for messages
Events.subscribe(AsyncRedisMessageReceiveEvent.class)
    .filter(e -> e.getChannel().equals("chat"))
    .handler(e -> {
        String message = e.getMessageObject(String.class).orElse("");
        Bukkit.broadcastMessage("§7[Network] " + message);
    })
    .bindWith(this);
```

## Cross-Server Messaging

```java
public class MyPlugin extends BasePlugin {

    private Redis redis;
    private String serverName;

    @Override
    protected DependencyContainer configureDependencies() {
        RedisCredentials credentials = RedisCredentials.of(
            getConfig().getString("redis.host", "localhost"),
            getConfig().getInt("redis.port", 6379),
            null,
            getConfig().getString("redis.password"),
            false
        );

        this.redis = new Redis("my-server", credentials);

        return DependencyInjection.builder()
            .register(this)
            .register(Redis.class, redis)
            .build();
    }

    @Override
    protected void enable() {
        this.serverName = getConfig().getString("server-name", "Server");

        // Subscribe to channels
        redis.subscribe("global-chat", "player-join", "player-quit");

        // Listen for global chat
        Events.subscribe(AsyncRedisMessageReceiveEvent.class)
            .filter(e -> e.getChannel().equals("global-chat"))
            .handler(e -> {
                String message = e.getMessageObject(String.class).orElse("");
                Bukkit.broadcastMessage("§7[Global] " + message);
            })
            .bindWith(this);

        // Listen for player joins
        Events.subscribe(AsyncRedisMessageReceiveEvent.class)
            .filter(e -> e.getChannel().equals("player-join"))
            .handler(e -> {
                String playerName = e.getMessageObject(String.class).orElse("");
                Bukkit.broadcastMessage("§e" + playerName + " joined the network!");
            })
            .bindWith(this);

        // Publish on events
        Events.subscribe(AsyncPlayerChatEvent.class)
            .handler(e -> {
                String message = e.getPlayer().getName() + ": " + e.getMessage();
                redis.publishString("global-chat", message);
            })
            .bindWith(this);

        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> {
                redis.publishString("player-join", e.getPlayer().getName());
            })
            .bindWith(this);
    }
}
```

## Commands Across Servers

```java
// Send command to all servers
@Command("globalkick")
@CommandPermission("admin.globalkick")
public void globalKick(Player sender, String targetName) {
    // Publish kick command
    redis.publishString("kick-player", targetName);
    Text.tell(sender, "&aKick command sent to all servers!");
}

// In enable():
redis.subscribe("kick-player");

Events.subscribe(AsyncRedisMessageReceiveEvent.class)
    .filter(e -> e.getChannel().equals("kick-player"))
    .handler(e -> {
        String playerName = e.getMessageObject(String.class).orElse("");
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null) {
            target.kickPlayer("§cYou have been kicked from the network!");
        }
    })
    .bindWith(this);
```

## Complete Example

```java
public class CrossServerPlugin extends BasePlugin {

    private Redis redis;

    @Override
    protected DependencyContainer configureDependencies() {
        RedisCredentials credentials = RedisCredentials.of(
            "localhost",
            6379,
            null,
            null,
            false
        );

        this.redis = new Redis("server-1", credentials);

        return DependencyInjection.builder()
            .register(this)
            .register(Redis.class, redis)
            .build();
    }

    @Override
    protected void enable() {
        // Subscribe to channels
        redis.subscribe("chat", "teleport-request");

        // Global chat
        Events.subscribe(AsyncRedisMessageReceiveEvent.class)
            .filter(e -> e.getChannel().equals("chat"))
            .handler(e -> {
                String message = e.getMessageObject(String.class).orElse("");
                Bukkit.broadcastMessage("§7[Network] §f" + message);
            })
            .bindWith(this);

        Events.subscribe(AsyncPlayerChatEvent.class)
            .handler(e -> {
                String msg = e.getPlayer().getName() + ": " + e.getMessage();
                redis.publishString("chat", msg);
                e.setCancelled(true);  // Handle locally via redis
            })
            .bindWith(this);

        // Server-to-server teleport
        Events.subscribe(AsyncRedisMessageReceiveEvent.class)
            .filter(e -> e.getChannel().equals("teleport-request"))
            .handler(e -> {
                String data = e.getMessageObject(String.class).orElse("");
                String[] parts = data.split(":");
                if (parts.length < 2) return;

                String playerName = parts[0];
                String serverName = parts[1];

                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    // Connect player to target server (BungeeCord)
                    sendToServer(player, serverName);
                }
            })
            .bindWith(this);

        // Commands
        Lamp<BukkitCommandActor> handler = createCommandHandler();
        handler.register(new NetworkCommands(redis));
    }
}

public class NetworkCommands {
    private final Redis redis;

    public NetworkCommands(Redis redis) {
        this.redis = redis;
    }

    @Command("alert")
    @CommandPermission("admin.alert")
    public void alert(Player sender, String message) {
        redis.publishString("alert", message);
        Text.tell(sender, "&aAlert sent to all servers!");
    }

    @Command("tpserver")
    @CommandPermission("admin.tpserver")
    public void tpServer(Player sender, Player target, String server) {
        redis.publishString("teleport-request", target.getName() + ":" + server);
        Text.tell(sender, "&aTeleport request sent!");
    }
}
```

## Cleanup

Redis connections are automatically closed when plugin disables (implements AutoCloseable).
