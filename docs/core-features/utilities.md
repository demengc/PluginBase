---
description: Helper utilities for common operations.
---

# Utilities

## Common

General-purpose helpers for plugin metadata, version checks, number parsing, and error reporting.

### Plugin and server information

| Method | Return type | Description |
|---|---|---|
| `getName()` | `String` | Plugin name from plugin.yml. |
| `getVersion()` | `String` | Plugin version from plugin.yml. |
| `getServerMajorVersion()` | `int` | Major MC version (e.g. `20` for 1.20.x). |
| `isServerVersionAtLeast(int)` | `boolean` | `true` if the server's major version >= the argument. |

```java
if (Common.isServerVersionAtLeast(16)) {
    // Use 1.16+ features
}
```

### Number parsing

Each method returns `null` if the string is not a valid number.

| Method | Return type |
|---|---|
| `checkInt(String)` | `Integer` |
| `checkLong(String)` | `Long` |
| `checkFloat(String)` | `Float` |
| `checkDouble(String)` | `Double` |

```java
Integer level = Common.checkInt("42");
if (level != null) {
    player.setLevel(level);
}
```

### Null handling

| Method | Return type | Description |
|---|---|---|
| `getOrDefault(T, T)` | `T` | Returns the first argument if non-null, otherwise the second. |
| `getOrError(T, String, boolean)` | `T` | Returns the first argument if non-null, otherwise throws `PluginErrorException`. The boolean controls whether the plugin is disabled. |

### Other helpers

| Method | Description |
|---|---|
| `formatDecimal(double)` | Formats a double to 2 decimal places (e.g. `"3.14"`). |
| `hasPermission(CommandSender, String)` | Returns `true` if the sender has the permission, or if the permission is null/empty/`"none"`. |
| `checkClass(String)` | Returns the `Class<?>` if it exists on the classpath, otherwise `null`. |
| `forEachInt(String, IntConsumer)` | Parses an integer sequence (`"1"`, `"1-5"`, or `"1,3,7"`) and runs the consumer for each value. |
| `error(Throwable, String, boolean, CommandSender...)` | Logs an error to console with a formatted block. Optionally disables the plugin and notifies players. |

```java
Common.error(ex, "Failed to load data", true);
Common.error(ex, "Failed to save data", false, player);
```

## Players

Bulk operations on online players.

| Method | Return type | Description |
|---|---|---|
| `all()` | `Collection<Player>` | All online players. |
| `stream()` | `Stream<Player>` | Stream of all online players. |
| `forEach(Consumer<Player>)` | `void` | Applies an action to every online player. |
| `streamInRange(Location, double)` | `Stream<Player>` | Stream of players within the given radius of a location. |
| `forEachInRange(Location, double, Consumer<Player>)` | `void` | Applies an action to every player within the radius. |

```java
Players.forEach(p -> p.sendMessage("Server restarting!"));

Players.forEachInRange(location, 50.0, p -> {
    Text.tell(p, "&aYou are near spawn!");
});
```

## Locations

Location manipulation helpers.

| Method | Return type | Description |
|---|---|---|
| `center(Location)` | `Location` | Rounds to the center of the block (+0.5 on X and Z), preserving yaw/pitch. |
| `toBlockLocation(Location)` | `Location` | Converts to integer coordinates with yaw/pitch zeroed. |

```java
Location centered = Locations.center(loc);
Location blockLoc = Locations.toBlockLocation(loc);
```

## Sounds

Play vanilla or custom (resource pack) sounds. All `playTo*` methods silently no-op if the sound name is `null` or `"none"`.

### By name (auto-detecting custom sounds)

Pass a `Sound` enum name for vanilla sounds. Prefix with `custom:` for resource pack sounds.

| Method | Parameters | Description |
|---|---|---|
| `playToPlayer` | `Player, String, float, float` | Plays to a player by sound name, volume, pitch. |
| `playToPlayer` | `Player, ConfigurationSection` | Reads `sound`, `volume`, `pitch` keys from the section. |
| `playToLocation` | `Location, String, float, float` | Plays at a location by sound name, volume, pitch. |
| `playToLocation` | `Location, ConfigurationSection` | Reads `sound`, `volume`, `pitch` keys from the section. |

```java
Sounds.playToPlayer(player, "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f);
Sounds.playToPlayer(player, "custom:myplugin.reward", 1.0f, 1.0f);
Sounds.playToLocation(location, "BLOCK_NOTE_BLOCK_PLING", 1.0f, 2.0f);
```

Config section format:

```yaml
sounds:
  levelup:
    sound: "ENTITY_PLAYER_LEVELUP"
    volume: 1.0
    pitch: 1.0
```

```java
Sounds.playToPlayer(player, config.getConfigurationSection("sounds.levelup"));
```

### Direct vanilla/custom methods

These skip the name-based dispatch and accept typed arguments directly.

| Method | Parameters |
|---|---|
| `playVanillaToPlayer` | `Player, Sound, float, float` |
| `playVanillaToLocation` | `Location, Sound, float, float` |
| `playCustomToPlayer` | `Player, String, float, float` |
| `playCustomToLocation` | `Location, String, float, float` |

```java
Sounds.playVanillaToPlayer(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
Sounds.playCustomToPlayer(player, "myplugin.reward", 1.0f, 1.0f);
```

Note: the direct custom methods take the raw sound name without the `custom:` prefix.

## UpdateChecker

Checks for plugin updates against the SpigotMC API. The constructor performs a blocking HTTP request, so it should be called asynchronously.

| Method | Return type | Description |
|---|---|---|
| `new UpdateChecker(int)` | `UpdateChecker` | Fetches the latest version for the given SpigotMC resource ID. |
| `getResult()` | `UpdateChecker.Result` | `UP_TO_DATE`, `OUTDATED`, or `ERROR`. |
| `getLatestVersion()` | `String` | The version string from SpigotMC (null on error). |
| `getResourceId()` | `int` | The resource ID passed to the constructor. |
| `notifyResult(CommandSender)` | `void` | Sends an update notification if outdated. Pass `null` for console. |

```java
Schedulers.async().run(() -> {
    UpdateChecker checker = new UpdateChecker(spigotResourceId);
    checker.notifyResult(null);
});
```

## Time

Duration parsing, formatting, and timestamp utilities.

### Duration parsing

`Time.parse(String)` accepts human-readable duration strings. Supported units: `y` (years), `mo` (months), `w` (weeks), `d` (days), `h` (hours), `m` (minutes), `s` (seconds). Throws `IllegalArgumentException` on invalid input.

```java
Duration d1 = Time.parse("1h 30m");
Duration d2 = Time.parse("3d 2h");
Duration d3 = Time.parse("1y 2mo 3w 4d 5h 6m 7s");
```

`Time.parseSafely(String)` returns `Optional<Duration>` instead of throwing.

### Duration formatting

| Formatter | Example output for 2h 15m |
|---|---|
| `DurationFormatter.LONG` | `2 hours 15 minutes` |
| `DurationFormatter.CONCISE` | `2h 15m` |
| `DurationFormatter.CONCISE_LOW_ACCURACY` | `2h 15m` (max 3 units) |

```java
long millis = Duration.ofHours(2).plusMinutes(15).toMillis();
String formatted = Time.formatDuration(Time.DurationFormatter.LONG, millis);
```

### Timestamp formatting and conversion

| Method | Description |
|---|---|
| `formatDateTime(long)` | Formats using the configured date-time pattern. |
| `formatDate(long)` | Formats using the configured date pattern. |
| `toSqlTimestamp(long)` | Converts epoch millis to `java.sql.Timestamp`. |
| `fromSqlTimestamp(String)` | Parses an SQL timestamp string to epoch millis. |

```java
String dateTime = Time.formatDateTime(System.currentTimeMillis());
Timestamp sqlTime = Time.toSqlTimestamp(System.currentTimeMillis());
long millis = Time.fromSqlTimestamp("2025-12-13 10:30:00");
```

## Services

Wrapper around Bukkit's `ServicesManager`.

| Method | Return type | Description |
|---|---|---|
| `provide(Class<T>, T)` | `T` | Registers a service at `Normal` priority using the base plugin. |
| `provide(Class<T>, T, ServicePriority)` | `T` | Registers a service at the given priority using the base plugin. |
| `provide(Class<T>, T, Plugin, ServicePriority)` | `T` | Registers a service with an explicit plugin and priority. |
| `get(Class<T>)` | `Optional<T>` | Retrieves a registered service, or empty. |
| `load(Class<T>)` | `T` | Retrieves a registered service, or throws `IllegalStateException`. |

```java
Services.provide(MyService.class, new MyServiceImpl());

Optional<Economy> economy = Services.get(Economy.class);

Economy econ = Services.load(Economy.class);
```
