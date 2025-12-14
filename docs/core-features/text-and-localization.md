---
description: Text formatting with HEX colors, MiniMessage, Adventure API, and i18n support.
---

# Text & Localization

## Color Formatting

### Legacy Colors

```java
Text.tell(player, "&aGreen &cRed &bBlue");
Text.console("&7[Server] &aStarted successfully!");
```

### HEX Colors (1.16+)

```java
Text.tell(player, "<#FF5733>Custom orange text!");
Text.tell(player, "<#33FF57>Custom green <#3357FF>and blue!");
```

### Color Schemes

```java
// Configure color scheme in your main class
setBaseSettings(new BaseSettings() {
    @Override
    public ColorScheme colorScheme() {
        return new ColorScheme("&6", "&7", "&f");  // Gold, Gray, White
    }
});

// Use placeholders
Text.tell(player, "&pPrimary");    // Gold
Text.tell(player, "&sSecondary");  // Gray
Text.tell(player, "&tTertiary");   // White
```

## Sending Messages

### To Players

```java
// With prefix
Text.tell(player, "&aWelcome!");

// Without prefix
Text.coloredTell(player, "&aHello!");

// Send Component (for advanced formatting)
Component component = Text.parseMini("<gradient:red:blue>Rainbow!</gradient>");
Text.tellComponent(player, component);

// Centered in chat
Text.tellCentered(player, "&6&lWELCOME");
```

### To Console

```java
// Formatted console message (with prefix and colors)
Text.console("&aPlugin loaded successfully!");

// Colored console message (colors but no prefix)
Text.coloredConsole("&7Running background task...");

// Plain logger output (no colors, no prefix)
Text.log("Starting initialization");
Text.log(Level.WARNING, "This is a warning");
```

### Broadcasting

```java
// Broadcast to all players (with prefix)
Text.broadcast(null, "&aServer event started!");

// Broadcast to players with permission
Text.broadcast("myplugin.admin", "&cAdmin announcement!");

// Broadcast without prefix
Text.broadcastColored(null, "&eGlobal message!");
```

## Titles

```java
// Simple title (default timing)
Text.sendTitle(player, "&6Welcome!", "&eEnjoy your stay");

// With custom timing (fadeIn, stay, fadeOut in ticks)
Text.sendTitle(player, "&6Welcome!", "&eEnjoy your stay", 10, 70, 20);

// Only title (no subtitle)
Text.sendTitle(player, "&6&lHELLO", null);

// Only subtitle (no title)
Text.sendTitle(player, null, "&7Welcome back");

// Clear current title/subtitle
Text.clearTitle(player);
```

## Localization

### Setup Language Files

PluginBase uses Java ResourceBundles for localization. Create language files in your plugin's resources:

Create `src/main/resources/messages_en.properties`:

```properties
welcome=Welcome, {0}!
goodbye=Goodbye, {0}!
error.permission=You don't have permission!
```

Create `src/main/resources/messages_es.properties`:

```properties
welcome=¡Bienvenido, {0}!
goodbye=¡Adiós, {0}!
error.permission=¡No tienes permiso!
```

Then register the resource bundle in your plugin:

```java
@Override
protected void enable() {
    // Load the "messages" resource bundle for all locales
    BaseManager.getTranslator().addResourceBundle("messages");
}
```

### Usage

```java
// Best practice: Use tellLocalized to send directly
Text.tellLocalized(player, "welcome", player.getName());
// English client: "Welcome, Steve!"
// Spanish client: "¡Bienvenido, Steve!"

// Without prefix
Text.coloredTellLocalized(player, "goodbye", player.getName());

// Manual approach (if you need the string for other purposes)
String message = Text.localized("welcome", player, player.getName());
Text.tell(player, message);

// Get localized string with default locale
String defaultMessage = Text.localizedDef("welcome", "Steve");

// Localized string with fallback
String msg = Text.localizedOrDefault(player, "missing.key", "&cDefault message");

// Shorthand for localized
String shorthand = Text.tl("welcome", player, player.getName());
```

## MiniMessage

[MiniMessage](https://docs.papermc.io/adventure/minimessage/) support is enabled by prefixing your message with `"mini:"`. When detected, the message will be parsed using MiniMessage instead of legacy color codes.

```java
// Gradient text
Text.tell(player, "mini:<gradient:red:blue>Rainbow text!</gradient>");

// Click actions
Text.tell(player, "mini:<click:run_command:/help>Click for help!</click>");

// Hover text
Text.tell(player, "mini:<hover:show_text:'More info'>Hover me!</hover>");

// Combined
String msg = "mini:<gradient:gold:yellow><click:run_command:/shop>" +
             "<hover:show_text:'Open the shop'>Shop</hover></click></gradient>";
Text.tell(player, msg);

// You can also parse MiniMessage directly to a Component
Component component = Text.parseMini("<gradient:red:blue>Rainbow text!</gradient>");
Text.tellComponent(player, component);
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
        return true;  // Add prefix to each line in multi-line messages
    }
});
```

## Utility Methods

### Formatting

```java
// Colorize without sending
String colored = Text.colorize("&aGreen text");
List<String> coloredList = Text.colorize(Arrays.asList("&aLine 1", "&bLine 2"));

// Format with prefix
String formatted = Text.format("This will have the plugin prefix");

// Format as error (prefix + red)
String errorMsg = Text.error("Something went wrong!");

// Strip all colors
String plain = Text.strip("&aGreen text");  // "Green text"
List<String> plainList = Text.strip(coloredList);
```

### Text Manipulation

```java
// Capitalize first letter
String capitalized = Text.capitalizeFirst("hello");  // "Hello"

// Title case with delimiter
String title = Text.titleCase("hello_world_test", "_");  // "Hello World Test"

// Convert string with \n to list
List<String> lines = Text.toList("Line 1\nLine 2\nLine 3");

// Get appropriate separator line
String line = Text.line(player);  // CHAT_LINE for players, CONSOLE_LINE for console
```

### MiniMessage Utilities

```java
// Parse MiniMessage to Component
Component comp = Text.parseMini("<gradient:red:blue>Text</gradient>");

// Parse MiniMessage and convert to legacy string (for item names, etc.)
String legacy = Text.legacyParseMini("<gradient:red:blue>Text</gradient>");

// Serialize Component to legacy format
String serialized = Text.legacySerialize(component);
```

### Helper Getters

```java
// Get current prefix
String prefix = Text.getPrefix();

// Get player's locale
Locale locale = Text.getLocale(player);

// Constants
String chatLine = Text.CHAT_LINE;       // Player chat separator
String consoleLine = Text.CONSOLE_LINE; // Console separator
```

## Special Features

### Ignoring Messages

Any message with the value `"ignore"` (case-insensitive) will not be sent. This is useful for configuration files where you want to disable certain messages:

```java
// This will not send anything
Text.tell(player, "ignore");
Text.console("IGNORE");  // Case-insensitive

// Useful in config.yml:
// join-message: "ignore"  # Disables the message
```

### Localization Placeholders

You can embed localized keys within strings using `#{key}` syntax:

```java
// Suppose you have: greeting=Hello, welcome=Welcome back
String message = "#{greeting} player! #{welcome}";
String localized = Text.localizePlaceholders(message, player);
// Result: "Hello player! Welcome back"
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        // Configure settings
        setBaseSettings(new BaseSettings() {
            @Override
            public String prefix() {
                return "&8[&bMyPlugin&8]&r ";
            }

            @Override
            public ColorScheme colorScheme() {
                return new ColorScheme("&6", "&7", "&e");  // Gold, Gray, Yellow
            }
        });

        // Load localization files
        BaseManager.getTranslator().addResourceBundle("messages");

        // Log to console
        Text.console("&aPlugin initialized!");

        // Join event with localized messages
        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();

                // Best practice: Use tellLocalized for direct sending
                Text.tellLocalized(player, "welcome.message", player.getName());

                // Title with timing
                Text.sendTitle(player, "&pWelcome!", "&s" + player.getName(), 10, 70, 20);

                // MiniMessage with mini: prefix
                Text.tell(player, "mini:<gradient:gold:yellow>Enjoy your stay!</gradient>");

                // Broadcast to everyone
                Text.broadcast(null, "&a" + player.getName() + " joined the server!");
            })
            .bindWith(this);

        // Quit event
        Events.subscribe(PlayerQuitEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();

                // Localized without prefix
                Text.coloredTellLocalized(player, "goodbye.message", player.getName());

                // Admin-only broadcast
                Text.broadcast("myplugin.admin", "&7[Admin] &e" + player.getName() + " left");
            })
            .bindWith(this);
    }

    @Override
    protected void disable() {
        Text.console("&cPlugin shutting down...");
    }
}
```

Example `messages_en.properties`:

```properties
welcome.message=&pWelcome back, &s{0}&p!
goodbye.message=&sSee you later, &t{0}&s!
error.no-permission=&cYou don't have permission to do that!
```
