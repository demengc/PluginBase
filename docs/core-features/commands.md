---
description: >-
  PluginBase uses Lamp, a powerful annotation-driven command framework for
  creating Bukkit/Spigot/Paper commands with minimal boilerplate.
---

# Commands

## Overview

Lamp provides:

* **Annotation-based commands** - Define commands with `@Command`, `@Description`, etc.
* **Automatic parameter parsing** - Players, numbers, enums, etc.
* **Tab completion** - Built-in suggestions for parameters
* **Permission handling** - Via `@CommandPermission`
* **Optional parameters** - Using `@Default` or `@Optional`
* **Flags and switches** - Unix-style flags like `--silent`
* **Cooldowns** - Built-in command cooldowns
* **Localized errors** - PluginBase provides automatic localized error messages

For complete Lamp documentation, see [**https://foxhut.gitbook.io/lamp-docs/**](https://foxhut.gitbook.io/lamp-docs/).

## Setup

### 1. Create Command Handler

```java
import dev.demeng.pluginbase.plugin.BasePlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        // Create Lamp handler with PluginBase's settings
        Lamp<BukkitCommandActor> lamp = createCommandHandler().build();
        
        // Register command classes
        lamp.register(new MyCommands());
    }
}
```

### 2. Create Command Class

```java
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import dev.demeng.pluginbase.text.Text;
import org.bukkit.entity.Player;

public class MyCommands {

    @Command("heal")
    @Description("Restore your health")
    @CommandPermission("myplugin.heal")
    public void heal(Player sender) {
        sender.setHealth(20.0);
        sender.setFoodLevel(20);
        Text.tell(sender, "&aYou have been healed!");
    }
}
```

## Command Parameters

Lamp automatically parses command parameters:

```java
@Command("give")
@CommandPermission("myplugin.give")
public void give(Player sender, Player target, Material material, int amount) {
    // Usage: /give Steve DIAMOND 64
    ItemStack item = new ItemStack(material, amount);
    target.getInventory().addItem(item);
    Text.tell(sender, "&aGave " + amount + "x " + material + " to " + target.getName());
}
```

### Supported Types

* `Player`, `OfflinePlayer`, `World`, `Entity`
* `int`, `double`, `float`, `long`, `short`, `byte`
* `String`, `boolean`, `UUID`
* `Material`, `EntityType`, and other Bukkit enums
* Custom types (see [Lamp docs](https://foxhut.gitbook.io/lamp-docs/how-to/custom-parameter-types))

## Optional Parameters

### Using @Default

```java
import revxrsal.commands.annotation.Default;

@Command("heal")
public void heal(Player sender, @Default("me") Player target) {
    // /heal        -> heals sender
    // /heal Steve  -> heals Steve
    Player toHeal = target;
    if (target.getName().equals("me")) {
        toHeal = sender;
    }
    
    toHeal.setHealth(20.0);
    Text.tell(sender, "&aHealed " + toHeal.getName());
}
```

### Using @Optional

```java
import revxrsal.commands.annotation.Optional;

@Command("teleport")
@CommandPermission("myplugin.teleport")
public void teleport(Player sender, Player target, @Optional Location location) {
    // /teleport Steve           -> teleports sender to Steve
    // /teleport Steve 100 64 100 -> teleports sender to coordinates
    
    if (location == null) {
        sender.teleport(target);
        Text.tell(sender, "&aTeleported to " + target.getName());
    } else {
        sender.teleport(location);
        Text.tell(sender, "&aTeleported to " + formatLocation(location));
    }
}
```

## Subcommands

Group related commands together:

```java
@Command("shop")
@CommandPermission("myplugin.shop")
public void shop(Player sender) {
    // /shop
    Text.tell(sender, "&6=== Shop Menu ===");
}

@Command("shop buy")
@CommandPermission("myplugin.shop.buy")
public void shopBuy(Player sender, Material item, @Default("1") int amount) {
    // /shop buy DIAMOND 64
    int cost = calculateCost(item, amount);
    Text.tell(sender, "&aPurchased " + amount + "x " + item + " for $" + cost);
}

@Command("shop sell")
@CommandPermission("myplugin.shop.sell")
public void shopSell(Player sender, Material item, @Default("1") int amount) {
    // /shop sell DIAMOND 64
    int price = calculatePrice(item, amount);
    Text.tell(sender, "&aSold " + amount + "x " + item + " for $" + price);
}
```

## Flags and Switches

Add Unix-style flags to commands:

```java
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Switch;

@Command("teleport")
@CommandPermission("myplugin.teleport")
public void teleport(
    Player sender,
    Player target,
    @Switch("silent") boolean silent,
    @Flag("message") String customMessage
) {
    // /teleport Steve --silent
    // /teleport Steve --message "Woosh!"
    
    sender.teleport(target.getLocation());
    
    if (!silent) {
        String message = customMessage != null 
            ? customMessage 
            : "&aTeleported to " + target.getName();
        Text.tell(sender, message);
    }
}
```

**Switch vs Flag:**

* `@Switch` - Boolean flag with no value (e.g., `--silent`)
* `@Flag` - Flag that accepts a value (e.g., `--message "text"`)

## Cooldowns

Add command cooldowns using `@Cooldown`:

```java
import revxrsal.commands.annotation.Cooldown;
import java.util.concurrent.TimeUnit;

@Command("heal")
@CommandPermission("myplugin.heal")
@Cooldown(value = 30, unit = TimeUnit.SECONDS)
public void heal(Player sender) {
    sender.setHealth(20.0);
    Text.tell(sender, "&aHealed!");
}
```

When on cooldown, players see a localized error message automatically.

## Suggestions

Provide tab completion suggestions:

```java
import revxrsal.commands.annotation.SuggestionProvider;

@Command("warp")
@CommandPermission("myplugin.warp")
public void warp(Player sender, @SuggestionProvider("warps") String warpName) {
    // Tab completion suggests warp names
    Location warpLocation = getWarpLocation(warpName);
    sender.teleport(warpLocation);
    Text.tell(sender, "&aTeleported to " + warpName);
}
```

Then register the suggestion provider:

```java
@Override
protected void enable() {
    Lamp<BukkitCommandActor> lamp = createCommandHandler()
        .suggestionProviders(providers -> {
            providers.put("warps", (context) -> {
                // Return list of warp names
                return getWarpNames();
            });
        })
        .build();
    
    lamp.register(new MyCommands());
}
```

For more on suggestions, see [Lamp's suggestions documentation](https://foxhut.gitbook.io/lamp-docs/how-to/suggestions-and-auto-completion).

## Parameter Validation

Validate parameter ranges using annotations:

```java
import revxrsal.commands.annotation.Range;

@Command("setlevel")
@CommandPermission("myplugin.setlevel")
public void setLevel(Player sender, Player target, @Range(min = 0, max = 100) int level) {
    // Only accepts levels between 0-100
    target.setLevel(level);
    Text.tell(sender, "&aSet " + target.getName() + "'s level to " + level);
}
```

For more validation options, see [Lamp's parameter validators documentation](https://foxhut.gitbook.io/lamp-docs/how-to/parameter-validators).

## Localized Error Messages

PluginBase automatically provides localized error messages for all common errors. Create a `pluginbase_<locale>.properties` file in your plugin's data folder (e.g., `pluginbase_en.properties`):

```properties
# Player not found
commands.invalid-player=&cPlayer not found: &e{0}&c.

# No permission
commands.no-permission=&cYou don't have permission!

# Missing argument
commands.missing-argument=&cMissing required argument: &e{0}&c. Usage: &e/{1}&c.

# Number out of range
commands.number-not-in-range.too-small=&c{0} must be at least &e{2}&c.
commands.number-not-in-range.too-large=&c{0} must be at most &e{2}&c.

# Command cooldown
commands.cooldown=&cPlease wait &e{0}&c!

# Invalid boolean
commands.invalid-boolean=&cExpected true or false, got &e{0}&c.

# Unknown command with suggestion
commands.failed-resolve=&cUnknown command: &e/{0}&c. Did you mean &e/{1}&c?
```

For a complete list of error keys, see the [sample properties file](../../samples/pluginbase_en.properties). For more information on localization (including how you can use your plugin's YAML configuration instead), see [Text and Localization](text-localization.md).

## Complete Example

```java
import dev.demeng.pluginbase.plugin.BasePlugin;
import dev.demeng.pluginbase.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.concurrent.TimeUnit;

// ===== Plugin Main Class =====
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        Lamp<BukkitCommandActor> lamp = createCommandHandler().build();
        lamp.register(new AdminCommands());
    }
}

// ===== Command Class =====
public class AdminCommands {

    @Command("heal")
    @Description("Restore health and hunger")
    @CommandPermission("admin.heal")
    @Cooldown(value = 10, unit = TimeUnit.SECONDS)
    public void heal(Player sender, @Default("me") Player target) {
        Player toHeal = target.getName().equals("me") ? sender : target;
        
        toHeal.setHealth(20.0);
        toHeal.setFoodLevel(20);
        toHeal.setSaturation(20.0f);
        
        Text.tell(sender, "&aHealed " + toHeal.getName() + "!");
        if (!toHeal.equals(sender)) {
            Text.tell(toHeal, "&aYou were healed by " + sender.getName());
        }
    }

    @Command("give")
    @Description("Give items to a player")
    @CommandPermission("admin.give")
    public void give(
        Player sender,
        Player target,
        Material material,
        @Default("1") @Range(min = 1, max = 2304) int amount
    ) {
        ItemStack item = new ItemStack(material, amount);
        target.getInventory().addItem(item);
        
        Text.tell(sender, "&aGave " + amount + "x " + material + " to " + target.getName());
        Text.tell(target, "&aYou received " + amount + "x " + material);
    }

    @Command("fly")
    @Description("Toggle flight mode")
    @CommandPermission("admin.fly")
    public void fly(
        Player sender,
        @Default("me") Player target,
        @Switch("silent") boolean silent
    ) {
        Player toToggle = target.getName().equals("me") ? sender : target;
        boolean flying = !toToggle.getAllowFlight();
        
        toToggle.setAllowFlight(flying);
        toToggle.setFlying(flying);
        
        if (!silent) {
            String status = flying ? "&aenabled" : "&cdisabled";
            Text.tell(sender, "&7Flight " + status + " for " + toToggle.getName());
        }
    }

    @Command("gamemode")
    @Description("Change game mode")
    @CommandPermission("admin.gamemode")
    public void gamemode(Player sender, org.bukkit.GameMode mode, @Default("me") Player target) {
        Player toChange = target.getName().equals("me") ? sender : target;
        toChange.setGameMode(mode);
        
        Text.tell(sender, "&aSet " + toChange.getName() + "'s game mode to " + mode);
    }
}
```

## Advanced Features

For more advanced features, see the official [Lamp documentation](https://foxhut.gitbook.io/lamp-docs/):

* [**Custom Parameter Types**](https://foxhut.gitbook.io/lamp-docs/how-to/custom-parameter-types) - Parse custom objects
* [**Context Parameters**](https://foxhut.gitbook.io/lamp-docs/how-to/context-parameters) - Auto-inject contextual data
* [**Command Conditions**](https://foxhut.gitbook.io/lamp-docs/how-to/command-conditions) - Add pre-execution checks
* [**Response Handlers**](https://foxhut.gitbook.io/lamp-docs/how-to/response-handlers) - Handle command return values
* [**Help Commands**](https://foxhut.gitbook.io/lamp-docs/how-to/help-commands) - Auto-generate help menus
* [**Hooks**](https://foxhut.gitbook.io/lamp-docs/how-to/hooks) - Execute code before/after commands
* [**Visitors**](https://foxhut.gitbook.io/lamp-docs/how-to/visitors) - Modify command structure dynamically
