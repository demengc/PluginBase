# Utilities

Helper utilities for common operations.

## Common

Version checking and plugin utilities.

```java
import dev.demeng.pluginbase.Common;

// Check server version (takes only major version number)
if (Common.isServerVersionAtLeast(16)) {
    // Use 1.16+ features (like HEX colors)
}

// Get server's major version as integer
int majorVersion = Common.getServerMajorVersion();  // Returns 20 for 1.20.x

// Get plugin version string
String version = Common.getVersion();  // Returns plugin version from plugin.yml

// Get plugin name
String name = Common.getName();  // Returns plugin name from plugin.yml

// Log errors
try {
    // Code that might fail
} catch (Exception ex) {
    Common.error(ex, "Failed to load data", true);  // Prints to console and optionally disables plugin
}

// Log errors and notify players
try {
    // Code that might fail
} catch (Exception ex) {
    Common.error(ex, "Failed to save data", false, player);  // Notifies player about error
}
```

## Players

Player-related utilities.

```java
import dev.demeng.pluginbase.Players;

// Get all online players
Collection<Player> players = Players.all();

// Stream all online players
Players.stream().forEach(player -> {
    // Do something with each player
});

// Apply action to all players
Players.forEach(player -> {
    player.sendMessage("Broadcast message!");
});

// Get players within radius of a location
Players.streamInRange(location, 50.0).forEach(player -> {
    // Do something with nearby players
});

// Apply action to players within radius
Players.forEachInRange(location, 50.0, player -> {
    player.sendMessage("You are near the spawn!");
});
```

## Locations

Location utilities.

```java
import dev.demeng.pluginbase.Locations;

// Center location to block (adds 0.5 to X and Z)
Location centered = Locations.center(loc);

// Convert to block location (integer coordinates)
Location blockLoc = Locations.toBlockLocation(loc);
```

## Sounds

Play sounds easily.

```java
import dev.demeng.pluginbase.Sounds;
import org.bukkit.Sound;

// Play vanilla sound to player
Sounds.playVanillaToPlayer(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

// Play vanilla sound at location
Sounds.playVanillaToLocation(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);

// Play custom sound (from resource pack) to player
Sounds.playCustomToPlayer(player, "custom.sound.name", 1.0f, 1.0f);

// Play custom sound at location
Sounds.playCustomToLocation(location, "custom.sound.name", 1.0f, 1.0f);

// Play sound by name (supports both vanilla and custom sounds)
Sounds.playToPlayer(player, "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f);
Sounds.playToPlayer(player, "custom:mysound", 1.0f, 1.0f);  // Custom sounds use "custom:" prefix

// Play sound from config section
ConfigurationSection soundConfig = config.getConfigurationSection("sounds.levelup");
Sounds.playToPlayer(player, soundConfig);
Sounds.playToLocation(location, soundConfig);
```

## UpdateChecker

Check for plugin updates from SpigotMC.

```java
import dev.demeng.pluginbase.UpdateChecker;
import dev.demeng.pluginbase.Schedulers;

// Check for updates asynchronously
Schedulers.async().run(() -> {
    UpdateChecker checker = new UpdateChecker(spigotResourceId);
    
    // Get results
    UpdateChecker.Result result = checker.getResult();
    String latestVersion = checker.getLatestVersion();
    
    // Notify console or player
    checker.notifyResult(null);  // Notifies console
    checker.notifyResult(player);  // Notifies specific player
});
```

## Error Handling

```java
try {
    // Database operation
    database.save(data);
} catch (Exception ex) {
    // Log with stack trace
    Common.error(ex, "Failed to save data", true);

    // Notify player
    Text.tell(player, "&cFailed to save data!");
}
```

## Version-Specific Code

```java
if (Common.isServerVersionAtLeast(16)) {
    // Use HEX colors (1.16+)
    Text.tell(player, "<#FF5733>Custom color!");
} else {
    // Fallback to legacy colors
    Text.tell(player, "&cRed color");
}

if (Common.isServerVersionAtLeast(19)) {
    // Use 1.19+ features
} else {
    // Fallback for older versions
}

// Get major version number
int majorVersion = Common.getServerMajorVersion();
Text.tell(player, "&7Server version: 1." + majorVersion);
```

## Player Selection

```java
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;

// Get player by name using Bukkit
Player target = Bukkit.getPlayer("Steve");
if (target == null) {
    Text.tell(sender, "&cPlayer not found!");
    return;
}

// Get online VIPs using stream filtering
List<Player> vips = Players.stream()
    .filter(p -> p.hasPermission("vip"))
    .collect(Collectors.toList());

for (Player vip : vips) {
    Text.tell(vip, "&6VIP announcement!");
}

// Or use forEach directly
Players.stream()
    .filter(p -> p.hasPermission("vip"))
    .forEach(vip -> Text.tell(vip, "&6VIP announcement!"));

// Get players in specific world using stream filtering
World world = Bukkit.getWorld("world");
List<Player> survivalPlayers = Players.stream()
    .filter(p -> p.getWorld().equals(world))
    .collect(Collectors.toList());

// Get players near a location
Location spawn = new Location(world, 0, 64, 0);
Players.forEachInRange(spawn, 50.0, player -> {
    Text.tell(player, "&aYou are near spawn!");
});
```

## Time

Parse and format durations and timestamps.

```java
import dev.demeng.pluginbase.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Optional;

// Parse duration strings
Duration duration = Time.parse("1h 30m");  // 1 hour 30 minutes
Duration duration2 = Time.parse("3d 2h");  // 3 days 2 hours
Duration duration3 = Time.parse("1y 2mo 3w 4d 5h 6m 7s");  // All units

// Parse safely (returns Optional)
Optional<Duration> optional = Time.parseSafely("invalid");

// Format durations
long millis = Duration.ofHours(2).toMillis();
String formatted = Time.formatDuration(Time.DurationFormatter.LONG, millis);
// Output: "2 hours"

String concise = Time.formatDuration(Time.DurationFormatter.CONCISE, millis);
// Output: "2h"

// Format dates and timestamps
String dateTime = Time.formatDateTime(System.currentTimeMillis());
String date = Time.formatDate(System.currentTimeMillis());

// Convert to/from SQL timestamps
Timestamp sqlTime = Time.toSqlTimestamp(System.currentTimeMillis());
long timestamp = Time.fromSqlTimestamp("2025-12-13 10:30:00");
```

## Services

Work with Bukkit's service manager.

```java
import dev.demeng.pluginbase.Services;
import org.bukkit.plugin.ServicePriority;

// Register a service
MyService service = new MyService();
Services.provide(MyService.class, service);

// Register with specific priority
Services.provide(MyService.class, service, ServicePriority.High);

// Get a service (returns Optional)
Optional<Economy> economy = Services.get(Economy.class);
if (economy.isPresent()) {
    // Use economy service
}

// Load a service (throws if not found)
try {
    Economy economy = Services.load(Economy.class);
    // Use economy service
} catch (IllegalStateException ex) {
    Text.console("&cEconomy service not found!");
}
```
