# Cooldowns

Track cooldowns for players, actions, or any key.

## Basic Usage

```java
import dev.demeng.pluginbase.cooldown.Cooldown;
import dev.demeng.pluginbase.cooldown.CooldownMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Create cooldown map
CooldownMap<UUID> cooldowns = CooldownMap.create(
    Cooldown.of(30, TimeUnit.SECONDS)
);

// Test cooldown
UUID playerId = player.getUniqueId();
if (cooldowns.test(playerId)) {
    // Player can perform action
    Text.tell(player, "&aAction performed!");
} else {
    // Player is on cooldown
    long remaining = cooldowns.remainingTime(playerId, TimeUnit.SECONDS);
    Text.tell(player, "&cPlease wait " + remaining + " seconds!");
}
```

## Complete Example

```java
public class TeleportCommand {
    private final CooldownMap<UUID> cooldowns = CooldownMap.create(
        Cooldown.of(5, TimeUnit.MINUTES)
    );

    @Command("home")
    public void home(Player sender) {
        UUID uuid = sender.getUniqueId();

        // Check cooldown
        if (!cooldowns.test(uuid)) {
            long remaining = cooldowns.remainingTime(uuid, TimeUnit.SECONDS);
            Text.tell(sender, "&cPlease wait " + remaining + " seconds!");
            return;
        }

        // Perform action
        Location home = getHome(sender);
        sender.teleport(home);
        Text.tell(sender, "&aTeleported home!");
    }
}
```

## Other Key Types

```java
// Cooldown by string key
CooldownMap<String> messageCooldowns = CooldownMap.create(
    Cooldown.of(1, TimeUnit.MINUTES)
);

// Per-message cooldown
String key = player.getName() + ":welcome";
if (messageCooldowns.test(key)) {
    Text.tell(player, "&aWelcome message!");
}
```

## Manual Management

```java
CooldownMap<UUID> cooldowns = CooldownMap.create(
    Cooldown.of(30, TimeUnit.SECONDS)
);

UUID uuid = player.getUniqueId();

// Reset cooldown (starts the cooldown timer)
cooldowns.reset(uuid);

// Get remaining time
long remaining = cooldowns.remainingTime(uuid, TimeUnit.SECONDS);

// Get elapsed time in milliseconds
long elapsedMillis = cooldowns.elapsed(uuid);

// Get the cooldown instance for advanced operations
Cooldown cooldown = cooldowns.get(uuid);

// Put a specific cooldown instance (must have same duration as base)
cooldowns.put(uuid, Cooldown.of(30, TimeUnit.SECONDS));
```
