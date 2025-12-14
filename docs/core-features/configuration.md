---
description: YAML configuration with automatic saving and type-safe getters.
---

# Configuration

## YamlConfig

### Create Config

```java
// Creates config.yml in plugin data folder
// Throws IOException and InvalidConfigurationException
try {
    YamlConfig config = new YamlConfig("config.yml");
} catch (IOException | InvalidConfigurationException e) {
    e.printStackTrace();
}

// Loads existing or creates from resources/config.yml
```

### Reading Values

```java
// YamlConfig wraps FileConfiguration - use getConfig() to access
String name = config.getConfig().getString("server.name");
int maxPlayers = config.getConfig().getInt("server.max-players");
boolean enabled = config.getConfig().getBoolean("features.chat-filter");
List<String> worlds = config.getConfig().getStringList("worlds");

// With defaults
String prefix = config.getConfig().getString("prefix", "&8[&bServer&8]");
int port = config.getConfig().getInt("port", 25565);
```

### Writing Values

```java
config.getConfig().set("server.name", "My Server");
config.getConfig().set("server.max-players", 100);

// Save to file (throws IOException)
try {
    config.save();
} catch (IOException e) {
    e.printStackTrace();
}
```

### Reload Config

```java
// Reload from file (throws IOException and InvalidConfigurationException)
try {
    config.reload();
} catch (IOException | InvalidConfigurationException e) {
    e.printStackTrace();
}
```

## Default Config File

Create `resources/config.yml`:

```yaml
# Server settings
server:
  name: "My Server"
  max-players: 100
  motd: "&aWelcome to the server!"

# Features
features:
  chat-filter: true
  auto-save: true
  auto-save-interval: 300

# Worlds
worlds:
  - world
  - world_nether
  - world_the_end

# Database
database:
  enabled: true
  host: localhost
  port: 3306
  database: minecraft
  username: root
  password: password
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    private YamlConfig config;

    @Override
    protected void enable() {
        // Load config
        try {
            this.config = new YamlConfig("config.yml");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }

        // Read settings
        String serverName = config.getConfig().getString("server.name");
        int maxPlayers = config.getConfig().getInt("server.max-players");
        boolean chatFilter = config.getConfig().getBoolean("features.chat-filter");

        Text.console("&aLoaded config:");
        Text.console("&7Server: " + serverName);
        Text.console("&7Max players: " + maxPlayers);
        Text.console("&7Chat filter: " + chatFilter);

        // Auto-save if enabled
        if (config.getConfig().getBoolean("features.auto-save")) {
            int interval = config.getConfig().getInt("features.auto-save-interval");
            Schedulers.sync().runRepeating(() -> {
                saveData();
                Text.console("&aAuto-saved!");
            }, 0L, interval * 20L).bindWith(this);
        }
    }

    public void reloadConfig() {
        try {
            config.reload();
            Text.console("&aConfig reloaded!");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
```

## BaseSettings

Configure plugin-wide settings:

```java
@Override
protected void enable() {
    setBaseSettings(new BaseSettings() {
        @Override
        public String prefix() {
            // Load from config
            return config.getConfig().getString("prefix", "&8[&bMyPlugin&8]&r ");
        }

        @Override
        public ColorScheme colorScheme() {
            String primary = config.getConfig().getString("colors.primary", "&6");
            String secondary = config.getConfig().getString("colors.secondary", "&7");
            String tertiary = config.getConfig().getString("colors.tertiary", "&f");
            return new ColorScheme(primary, secondary, tertiary);
        }

        @Override
        public boolean includePrefixOnEachLine() {
            return config.getConfig().getBoolean("prefix-on-each-line", true);
        }
    });
}
```

## Reload Command

```java
@Command("myplugin reload")
@CommandPermission("myplugin.reload")
public void reload(Player sender) {
    try {
        config.reload();
        // Reapply settings
        setBaseSettings(/* ... */);
        Text.tell(sender, "&aConfiguration reloaded!");
    } catch (IOException | InvalidConfigurationException e) {
        e.printStackTrace();
        Text.tell(sender, "&cFailed to reload configuration!");
    }
}
```

## Nested Configuration

```yaml
database:
  enabled: true
  credentials:
    host: localhost
    port: 3306
    database: minecraft
    username: root
    password: password
```

```java
boolean enabled = config.getConfig().getBoolean("database.enabled");
String host = config.getConfig().getString("database.credentials.host");
int port = config.getConfig().getInt("database.credentials.port");
```
