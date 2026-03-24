---
description: Color formatting, message sending, MiniMessage, titles, and ResourceBundle-based i18n.
---

# Text and Localization

The `Text` utility class handles all message formatting and sending. It supports legacy `&` color codes, HEX colors (`<#RRGGBB>`), MiniMessage markup, and a built-in localization system backed by Java `ResourceBundle` files.

## Color Formats

| Format | Syntax | Example | Requires |
|---|---|---|---|
| Legacy codes | `&` + code | `&a` (green), `&l` (bold) | Any version |
| HEX colors | `<#RRGGBB>` | `<#FF5733>` | Spigot 1.16+ |
| MiniMessage | `mini:` prefix | `mini:<gradient:red:blue>text</gradient>` | Adventure |
| Color scheme | `&p`, `&s`, `&t` | `&pPrimary color` | `ColorScheme` configured |

### Legacy and HEX Colors

`Text.colorize()` translates both `&` color codes and `<#RRGGBB>` hex colors. HEX requires Spigot (or a fork) on 1.16+.

```java
String colored = Text.colorize("&aGreen &cRed &bBlue");
String hex = Text.colorize("<#FF5733>Custom orange text!");
```

### Color Schemes

Define a three-color palette (`&p` primary, `&s` secondary, `&t` tertiary) through `BaseSettings`:

```java
setBaseSettings(new BaseSettings() {
    @Override
    public ColorScheme colorScheme() {
        return new ColorScheme("&6", "&7", "&f");
    }
});

Text.tell(player, "&pPrimary &sSecondary &tTertiary");
```

`ColorScheme.fromConfig(ConfigurationSection)` can load colors from a YAML config section with `primary`, `secondary`, and `tertiary` keys.

## Sending Messages

### Player Messages

| Method | Prefix | Color | Notes |
|---|---|---|---|
| `tell(CommandSender, String)` | Yes | Yes | Standard message |
| `coloredTell(CommandSender, String)` | No | Yes | Colors only, no prefix |
| `tellComponent(Player, Component)` | No | No | Sends an Adventure `Component` directly |
| `tellCentered(Player, String)` | No | Yes | Centered in chat; may not work with custom fonts or HEX colors |

```java
Text.tell(player, "&aWelcome!");
Text.coloredTell(player, "&aHello!");
Text.tellCentered(player, "&6&lWELCOME");

Component component = Text.parseMini("<gradient:red:blue>Rainbow!</gradient>");
Text.tellComponent(player, component);
```

### Console Messages

| Method | Prefix | Color | Notes |
|---|---|---|---|
| `console(String)` | Yes | Yes | Formatted console output |
| `coloredConsole(String)` | No | Yes | Colors only, no prefix |
| `log(String)` | No | No | Plain logger output at INFO level |
| `log(Level, String)` | No | No | Plain logger output at specified level |

```java
Text.console("&aPlugin loaded successfully!");
Text.coloredConsole("&7Running background task...");
Text.log("Starting initialization");
Text.log(Level.WARNING, "This is a warning");
```

### Broadcasting

| Method | Prefix | Color |
|---|---|---|
| `broadcast(String permission, String)` | Yes | Yes |
| `broadcastColored(String permission, String)` | No | Yes |

Pass `null` as the permission to broadcast to all players.

```java
Text.broadcast(null, "&aServer event started!");
Text.broadcast("myplugin.admin", "&cAdmin announcement!");
Text.broadcastColored(null, "&eGlobal message!");
```

## Titles

```java
Text.sendTitle(player, "&6Welcome!", "&eEnjoy your stay");
Text.sendTitle(player, "&6Welcome!", "&eEnjoy your stay", 10, 70, 20);
Text.sendTitle(player, "&6&lHELLO", null);
Text.sendTitle(player, null, "&7Welcome back");
Text.clearTitle(player);
```

| Method | Parameters |
|---|---|
| `sendTitle(Player, String title, String subtitle)` | Default fade timing |
| `sendTitle(Player, String title, String subtitle, int fadeIn, int stay, int fadeOut)` | Timing in ticks |
| `clearTitle(Player)` | Clears current title and subtitle |

Pass `null` for either `title` or `subtitle` to send only one.

## MiniMessage

Prefix any message string with `"mini:"` to parse it as [MiniMessage](https://docs.papermc.io/adventure/minimessage/) instead of legacy color codes. This works with `tell`, `coloredTell`, `tellLocalized`, and `coloredTellLocalized`.

```java
Text.tell(player, "mini:<gradient:red:blue>Rainbow text!</gradient>");
Text.tell(player, "mini:<click:run_command:/help>Click for help!</click>");
Text.tell(player, "mini:<hover:show_text:'More info'>Hover me!</hover>");
```

To parse MiniMessage directly to a `Component`:

```java
Component comp = Text.parseMini("<gradient:red:blue>Text</gradient>");
Text.tellComponent(player, comp);
```

The `MINI_MESSAGE` field on `Text` exposes the underlying `MiniMessage` instance if you need direct access.

## Formatting Utilities

| Method | Returns | Description |
|---|---|---|
| `colorize(String)` | `String` | Translates `&` codes and `<#HEX>` colors |
| `colorize(List<String>)` | `List<String>` | Batch colorize |
| `format(String)` | `String` | Prepends prefix, then colorizes |
| `error(String)` | `String` | Prepends prefix + `&c`, then colorizes |
| `strip(String)` | `String` | Removes all color codes |
| `strip(List<String>)` | `List<String>` | Batch strip |
| `parseMini(String)` | `Component` | Parses MiniMessage to an Adventure Component |
| `legacyParseMini(String)` | `String` | Parses MiniMessage, serializes to legacy string |
| `legacySerialize(Component)` | `String` | Serializes a Component to legacy format |

## Text Manipulation

| Method | Returns | Example |
|---|---|---|
| `capitalizeFirst(String)` | `String` | `"hello"` -> `"Hello"` |
| `titleCase(String, String)` | `String` | `"hello_world", "_"` -> `"Hello World"` |
| `toList(String)` | `List<String>` | Splits on `\n` |
| `line(CommandSender)` | `String` | `CHAT_LINE` for players, `CONSOLE_LINE` for console |
| `getPrefix()` | `String` | Current plugin prefix |
| `getLocale(CommandSender)` | `Locale` | Player's client locale, or default locale for console/null |

Constants: `Text.CHAT_LINE` (player chat separator), `Text.CONSOLE_LINE` (console separator).

## Ignoring Messages

Any message with the value `"ignore"` (case-insensitive) is silently discarded. This applies to `tell`, `coloredTell`, `console`, `coloredConsole`, `broadcast`, `broadcastColored`, `tellCentered`, `tellLocalized`, and `coloredTellLocalized`.

Useful for letting users disable messages in config files:

```yaml
# config.yml
join-message: "ignore"
```

```java
String msg = config.getString("join-message");
Text.tell(player, msg);
```

## Prefix Configuration

```java
setBaseSettings(new BaseSettings() {
    @Override
    public String prefix() {
        return "&8[&bMyPlugin&8]&r ";
    }

    @Override
    public boolean includePrefixOnEachLine() {
        return true;
    }
});
```

When `includePrefixOnEachLine()` returns `true` (the default), multi-line messages split on `\n` will have the prefix prepended to each line.

## Localization (i18n)

PluginBase uses Java `ResourceBundle` files (`.properties`) for localization. The player's Minecraft client locale is automatically detected and matched against registered bundles.

### Setup

Create `.properties` files in `src/main/resources/`:

`messages_en.properties`:
```properties
welcome=Welcome, {0}!
goodbye=Goodbye, {0}!
error.permission=You don''t have permission!
```

`messages_es.properties`:
```properties
welcome=Bienvenido, {0}!
goodbye=Adios, {0}!
error.permission=No tienes permiso!
```

Register the bundle in your plugin's `enable()`:

```java
@Override
protected void enable() {
    BaseManager.getTranslator().addResourceBundle("messages");
}
```

`addResourceBundle(String)` automatically scans for all locales defined in `Locales`. To register only specific locales, pass them explicitly:

```java
BaseManager.getTranslator().addResourceBundle("messages", Locales.ENGLISH, Locales.SPANISH);
```

### Sending Localized Messages

| Method | Prefix | Description |
|---|---|---|
| `tellLocalized(CommandSender, String key, Object... args)` | Yes | Sends a localized, formatted message |
| `coloredTellLocalized(CommandSender, String key, Object... args)` | No | Sends a localized, colorized message without prefix |

```java
Text.tellLocalized(player, "welcome", player.getName());
Text.coloredTellLocalized(player, "goodbye", player.getName());
```

### Retrieving Localized Strings

| Method | Locale source | Description |
|---|---|---|
| `localized(String key, CommandSender, Object... args)` | Sender's client locale | Primary lookup method |
| `localized(String key, Locale, Object... args)` | Explicit `Locale` | For non-player contexts |
| `localizedDef(String key, Object... args)` | Default translator locale | Ignores sender locale |
| `localizedOrDefault(CommandSender, String key, String default, Object... args)` | Sender's client locale | Falls back to `default` if key is missing |
| `tl(String key, CommandSender, Object... args)` | Sender's client locale | Alias for `localized` |
| `tl(String key, Locale, Object... args)` | Explicit `Locale` | Alias for `localized` |

```java
String msg = Text.localized("welcome", player, player.getName());
String def = Text.localizedDef("welcome", "Steve");
String safe = Text.localizedOrDefault(player, "missing.key", "&cDefault message");
```

Placeholders use `java.text.MessageFormat` syntax: `{0}`, `{1}`, etc.

### Inline Localization Placeholders

Embed localized keys within strings using `#{key}` syntax:

```java
String message = "#{greeting} player! #{welcome}";
String result = Text.localizePlaceholders(message, player);
```

| Method | Locale source |
|---|---|
| `localizePlaceholders(String, CommandSender, Object... args)` | Sender's locale |
| `localizePlaceholders(String, Locale, Object... args)` | Explicit locale |
| `localizePlaceholdersDef(String, Object... args)` | Default locale |

The same `args` are applied to every placeholder in the string.

### Loading Bundles from the Data Folder

`addResourceBundleFromFolder(String)` loads `.properties` files from the `plugins/<YourPlugin>/locales/` directory instead of from the JAR's classpath. This lets server administrators add or override translations at runtime.

```java
BaseManager.getTranslator().addResourceBundleFromFolder("messages");
```

### Translator API

`BaseManager.getTranslator()` returns the `Translator` instance. Key methods beyond `addResourceBundle`:

| Method | Description |
|---|---|
| `get(String key)` | Gets message for key using default locale |
| `get(String key, Locale)` | Gets message for key using specified locale |
| `containsKey(String key)` | Checks if key exists in default locale |
| `containsKey(String key, Locale)` | Checks if key exists in specified locale |
| `setLocale(Locale)` | Changes the default locale |
| `getLocale()` | Returns the current default locale |
| `add(LocaleReader)` | Registers a custom `LocaleReader` |
| `add(ResourceBundle)` | Registers a `ResourceBundle` directly |
| `clear()` | Removes all registered locale data |

### ConfigLocaleReader (YAML-based Localization)

For plugins that prefer a single YAML file over `.properties` bundles, `ConfigLocaleReader` wraps a Bukkit `FileConfiguration` as a `LocaleReader`:

```java
FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
LocaleReader reader = new ConfigLocaleReader(langConfig, Locales.ENGLISH);
BaseManager.getTranslator().add(reader);
```

This is useful when your plugin already uses YAML for all configuration and you want a consistent `messages.yml` approach rather than `.properties` files.

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        setBaseSettings(new BaseSettings() {
            @Override
            public String prefix() {
                return "&8[&bMyPlugin&8]&r ";
            }

            @Override
            public ColorScheme colorScheme() {
                return new ColorScheme("&6", "&7", "&e");
            }
        });

        BaseManager.getTranslator().addResourceBundle("messages");

        Text.console("&aPlugin initialized!");

        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();
                Text.tellLocalized(player, "welcome.message", player.getName());
                Text.sendTitle(player, "&pWelcome!", "&s" + player.getName(), 10, 70, 20);
                Text.tell(player, "mini:<gradient:gold:yellow>Enjoy your stay!</gradient>");
                Text.broadcast(null, "&a" + player.getName() + " joined the server!");
            })
            .bindWith(this);
    }

    @Override
    protected void disable() {
        Text.console("&cPlugin shutting down...");
    }
}
```

`messages_en.properties`:

```properties
welcome.message=&pWelcome back, &s{0}&p!
goodbye.message=&sSee you later, &t{0}&s!
error.no-permission=&cYou don''t have permission to do that!
```
