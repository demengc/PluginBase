---
description: Redis pub/sub for cross-server messaging.
---

# Redis

The `pluginbase-redis` module wraps Jedis (v7.0.0, shaded and relocated) to provide pub/sub messaging between Spigot servers. Messages are wrapped in a `MessageTransferObject` that carries a server ID, a JSON payload, and a timestamp.

## Dependency

Add `pluginbase-redis` as a dependency. Jedis and commons-pool2 are shaded into the module jar.

```xml
<dependency>
  <groupId>dev.demeng</groupId>
  <artifactId>pluginbase-redis</artifactId>
  <version>1.36.1-SNAPSHOT</version>
</dependency>
```

## Credentials

`RedisCredentials` holds connection details for a Redis instance.

| Field      | Type      | Nullable | Description                              |
|------------|-----------|----------|------------------------------------------|
| `host`     | `String`  | No       | Hostname or IP address                   |
| `port`     | `int`     | --       | Port number (typically 6379)             |
| `user`     | `String`  | Yes      | Username for ACL auth (null to skip)     |
| `password` | `String`  | Yes      | Password (null to skip)                  |
| `ssl`      | `boolean` | --       | Whether to use SSL                       |

Empty or blank strings for `user` and `password` are normalized to `null`.

There are two ways to create credentials:

```java
import dev.demeng.pluginbase.redis.RedisCredentials;

// From explicit values
RedisCredentials credentials = RedisCredentials.of(
    "localhost",
    6379,
    null,       // user
    "password", // password
    false       // ssl
);

// From a Bukkit ConfigurationSection (keys: host, port, user, password, ssl)
RedisCredentials credentials = RedisCredentials.of(
    getConfig().getConfigurationSection("redis")
);
```

The `ConfigurationSection` overload defaults to `localhost:6379`, no auth, SSL off.

## Creating a Connection

The `Redis` constructor takes a server ID (used as the sender ID in messages), credentials, and an optional varargs list of channels to subscribe to immediately.

```java
import dev.demeng.pluginbase.redis.Redis;

// Subscribe to channels at construction time
Redis redis = new Redis("server-1", credentials, "chat", "alerts");

// Or create without initial subscriptions, then subscribe later
Redis redis = new Redis("server-1", credentials);
redis.subscribe("chat", "alerts");
```

## API Reference

`Redis` implements `IRedis`, which extends `Terminable`.

| Method                                          | Return Type | Description                                                        |
|-------------------------------------------------|-------------|--------------------------------------------------------------------|
| `getServerId()`                                 | `String`    | The server ID passed to the constructor                            |
| `getJedisPool()`                                | `JedisPool` | The underlying Jedis connection pool                               |
| `subscribe(String... channels)`                 | `boolean`   | Subscribes to channels not already subscribed. Returns true if at least one new channel was added. |
| `unsubscribe(String... channels)`               | `void`      | Unsubscribes from the given channels                               |
| `isSubscribed(String channel)`                  | `boolean`   | Whether the given channel is currently subscribed                  |
| `publishObject(String channel, Object obj)`     | `boolean`   | Publishes an object (serialized to JSON). Returns false if closing or serialization fails. |
| `publishString(String channel, String str)`     | `boolean`   | Publishes a plain string. Returns false if closing or serialization fails. |
| `close()`                                       | `void`      | Unsubscribes from all channels and destroys the connection pool    |

Publishing happens asynchronously on the PluginBase async scheduler.

## Receiving Messages

When a message arrives on a subscribed channel, two Bukkit events are fired:

1. **`AsyncRedisMessageReceiveEvent`** -- fired on the async thread where the message was received.
2. **`RedisMessageReceiveEvent`** -- fired on the main server thread (scheduled via `Schedulers.sync()`).

Both events implement `IRedisMessageReceiveEvent` and expose the same properties.

### Event Properties

| Method                                    | Return Type  | Description                                              |
|-------------------------------------------|--------------|----------------------------------------------------------|
| `getChannel()`                            | `String`     | The channel the message arrived on                       |
| `getSenderId()`                           | `String`     | The server ID of the sender                              |
| `getMessage()`                            | `String`     | The raw JSON string payload                              |
| `getMessageObject(Class<T> objectClass)`  | `Optional<T>`| The payload deserialized to the given type, or empty if parsing fails |
| `getTimestamp()`                          | `long`       | Epoch millis when the message was published              |

Use the async event when you do not need to interact with the Bukkit API (file I/O, logging, forwarding). Use the sync event when you need to modify world state, send packets, or call Bukkit methods that require the main thread.

```java
import dev.demeng.pluginbase.redis.event.AsyncRedisMessageReceiveEvent;
import dev.demeng.pluginbase.redis.event.RedisMessageReceiveEvent;

// Async: good for logging or forwarding
Events.subscribe(AsyncRedisMessageReceiveEvent.class)
    .filter(e -> e.getChannel().equals("chat"))
    .handler(e -> {
        String message = e.getMessageObject(String.class).orElse("");
        getLogger().info("[Network] " + message);
    })
    .bindWith(this);

// Sync: safe to call Bukkit API
Events.subscribe(RedisMessageReceiveEvent.class)
    .filter(e -> e.getChannel().equals("chat"))
    .handler(e -> {
        String message = e.getMessageObject(String.class).orElse("");
        Bukkit.broadcastMessage("§7[Network] " + message);
    })
    .bindWith(this);
```

## Publishing Messages

```java
// Publish a plain string
redis.publishString("chat", "Hello from server 1!");

// Publish an object (serialized to JSON via Gson)
PlayerData data = new PlayerData(uuid, name, coins);
redis.publishObject("player-data", data);
```

Both methods return `boolean`: `true` if the message was queued for publishing, `false` if the connection is closing or serialization failed.

## Complete Example

```java
public class CrossServerPlugin extends BasePlugin {

    private Redis redis;

    @Override
    protected DependencyContainer configureDependencies() {
        RedisCredentials credentials = RedisCredentials.of(
            getConfig().getConfigurationSection("redis")
        );

        this.redis = new Redis(
            getConfig().getString("server-id", "server-1"),
            credentials,
            "global-chat", "player-join"
        );

        return DependencyInjection.builder()
            .register(this)
            .register(Redis.class, redis)
            .build();
    }

    @Override
    protected void enable() {
        // Sync event: broadcasts to online players (requires main thread)
        Events.subscribe(RedisMessageReceiveEvent.class)
            .filter(e -> e.getChannel().equals("global-chat"))
            .handler(e -> {
                String message = e.getMessageObject(String.class).orElse("");
                Bukkit.broadcastMessage("§7[Global] " + message);
            })
            .bindWith(this);

        Events.subscribe(RedisMessageReceiveEvent.class)
            .filter(e -> e.getChannel().equals("player-join"))
            .handler(e -> {
                String playerName = e.getMessageObject(String.class).orElse("");
                Bukkit.broadcastMessage("§e" + playerName + " joined the network!");
            })
            .bindWith(this);

        // Publish local events to the network
        Events.subscribe(AsyncPlayerChatEvent.class)
            .handler(e -> {
                String msg = e.getPlayer().getName() + ": " + e.getMessage();
                redis.publishString("global-chat", msg);
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

## Cleanup

`Redis` implements `Terminable`. All subscriptions are cancelled and the Jedis pool is destroyed automatically when the plugin disables or when `close()` is called manually.
