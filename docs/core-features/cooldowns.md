---
description: Track cooldowns for players, actions, or any key.
---

# Cooldowns

PluginBase provides `Cooldown` (a single timer) and `CooldownMap<T>` (a keyed collection of timers backed by an auto-expiring map).

## Creating a CooldownMap

`CooldownMap.create()` accepts a base `Cooldown` that defines the duration for all entries.

```java
import dev.demeng.pluginbase.cooldown.Cooldown;
import dev.demeng.pluginbase.cooldown.CooldownMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

CooldownMap<UUID> cooldowns = CooldownMap.create(
    Cooldown.of(30, TimeUnit.SECONDS)
);
```

`Cooldown.ofTicks(long)` is also available for tick-based durations (1 tick = 50ms).

```java
CooldownMap<UUID> cooldowns = CooldownMap.create(
    Cooldown.ofTicks(600) // 30 seconds
);
```

## Test and reset semantics

`test(key)` and `testSilently(key)` both return `true` when the cooldown for that key is **not active** (expired or never started). The difference is what happens on a `true` result:

| Method | Returns `true` when | Side effect on `true` |
|---|---|---|
| `test(key)` | Cooldown is not active | Resets the timer (starts the cooldown) |
| `testSilently(key)` | Cooldown is not active | None |

When the cooldown **is** active, both methods return `false` and neither resets the timer.

```java
UUID playerId = player.getUniqueId();

if (cooldowns.test(playerId)) {
    // Cooldown was not active; timer has now been reset.
    Text.tell(player, "&aAction performed!");
} else {
    // Cooldown is still active.
    long remaining = cooldowns.remainingTime(playerId, TimeUnit.SECONDS);
    Text.tell(player, "&cPlease wait " + remaining + " seconds!");
}
```

Use `testSilently()` when you need to check the cooldown state without consuming it (for example, to grey out a UI button).

## Querying remaining and elapsed time

| Method | Return type | Description |
|---|---|---|
| `remainingTime(key, TimeUnit)` | `long` | Time remaining until the cooldown expires, in the given unit. Returns `0` if not active. |
| `remainingMillis(key)` | `long` | Time remaining in milliseconds. Returns `0` if not active. |
| `elapsed(key)` | `long` | Milliseconds since the last `test()` or `reset()`. If the key was never tested, returns time since epoch. |

```java
long remainingSec = cooldowns.remainingTime(playerId, TimeUnit.SECONDS);
long remainingMs = cooldowns.remainingMillis(playerId);
long elapsedMs = cooldowns.elapsed(playerId);
```

## Manual management

```java
// Reset cooldown manually (starts the timer without checking)
cooldowns.reset(playerId);

// Access the underlying Cooldown instance for a key
Cooldown cd = cooldowns.get(playerId);

// Get/set the last-tested timestamp (useful for persistence/reconstruction)
OptionalLong lastTested = cooldowns.getLastTested(playerId);
cooldowns.setLastTested(playerId, System.currentTimeMillis());

// Insert a pre-built Cooldown (must match the base duration)
Cooldown restored = Cooldown.of(30, TimeUnit.SECONDS);
restored.setLastTested(savedTimestamp);
cooldowns.put(playerId, restored);

// Get all active cooldowns
Map<UUID, Cooldown> all = cooldowns.getAll();
```

## Complete example

```java
public class TeleportCommand {

    private final CooldownMap<UUID> cooldowns = CooldownMap.create(
        Cooldown.of(5, TimeUnit.MINUTES)
    );

    @Command("home")
    public void home(Player sender) {
        UUID uuid = sender.getUniqueId();

        if (!cooldowns.test(uuid)) {
            long remaining = cooldowns.remainingTime(uuid, TimeUnit.SECONDS);
            Text.tell(sender, "&cPlease wait " + remaining + " seconds!");
            return;
        }

        sender.teleport(getHome(sender));
        Text.tell(sender, "&aTeleported home!");
    }
}
```

## Using arbitrary key types

The type parameter on `CooldownMap<T>` can be any type with proper `equals`/`hashCode` semantics.

```java
CooldownMap<String> messageCooldowns = CooldownMap.create(
    Cooldown.of(1, TimeUnit.MINUTES)
);

String key = player.getName() + ":welcome";
if (messageCooldowns.test(key)) {
    Text.tell(player, "&aWelcome message!");
}
```

## Standalone Cooldown

`Cooldown` can be used on its own without a `CooldownMap` when you only need a single timer.

| Method | Return type | Description |
|---|---|---|
| `of(long, TimeUnit)` | `Cooldown` | Creates a cooldown with the given duration. |
| `ofTicks(long)` | `Cooldown` | Creates a cooldown measured in game ticks. |
| `test()` | `boolean` | Returns `true` if not active, then resets. |
| `testSilently()` | `boolean` | Returns `true` if not active, no reset. |
| `reset()` | `void` | Resets the timer to now. |
| `elapsed()` | `long` | Milliseconds since last reset. |
| `remainingMillis()` | `long` | Milliseconds until expiry (0 if not active). |
| `remainingTime(TimeUnit)` | `long` | Time until expiry in the given unit. |
| `getDuration()` | `long` | The cooldown duration in milliseconds. |
| `getLastTested()` | `OptionalLong` | Timestamp of last reset, or empty if never reset. |
| `setLastTested(long)` | `void` | Sets the last-tested timestamp directly. |
| `copy()` | `Cooldown` | Creates a new instance with the same duration. |

```java
Cooldown abilityCooldown = Cooldown.of(10, TimeUnit.SECONDS);

if (abilityCooldown.test()) {
    // Use ability
}
```
