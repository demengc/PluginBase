---
description: >-
  Annotation-driven commands using Lamp 4.x, with built-in parameter parsing,
  tab completion, permissions, cooldowns, and localized error messages.
---

# Commands

PluginBase integrates [Lamp 4.x](https://github.com/Revxrsal/Lamp), an annotation-driven command framework for Bukkit. `BasePlugin.createCommandHandler()` returns a pre-configured `Lamp.Builder<BukkitCommandActor>` with localized error handling and a "did you mean?" failure handler.

| Capability               | How                                     |
| ------------------------ | --------------------------------------- |
| Define commands          | `@Command`, `@Subcommand`               |
| Permissions              | `@CommandPermission`                    |
| Parameter parsing        | Automatic for Player, Material, int, etc. |
| Tab completion           | `@Suggest` (literals), `@SuggestWith` (dynamic) |
| Optional parameters      | `@Optional`, `@Default`                 |
| Flags and switches       | `@Flag`, `@Switch`                      |
| Cooldowns                | `@Cooldown`                             |
| Validation               | `@Range`                                |

Full Lamp documentation: [https://github.com/Revxrsal/Lamp/wiki](https://github.com/Revxrsal/Lamp/wiki)

## Creating the Command Handler

`createCommandHandler()` returns a `Lamp.Builder<BukkitCommandActor>`. Call `.build()` to produce the `Lamp` instance, then register command classes on it.

```java
import dev.demeng.pluginbase.plugin.BasePlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        Lamp<BukkitCommandActor> lamp = createCommandHandler().build();
        lamp.register(new MyCommands());
    }
}
```

You can customize the builder before calling `.build()`, for example to add suggestion providers or parameter types.

## Defining a Command

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

Lamp parses parameters from the command input automatically.

```java
@Command("give")
@CommandPermission("myplugin.give")
public void give(Player sender, Player target, Material material, int amount) {
    ItemStack item = new ItemStack(material, amount);
    target.getInventory().addItem(item);
    Text.tell(sender, "&aGave " + amount + "x " + material + " to " + target.getName());
}
```

### Built-in Parameter Types

| Category  | Types                                                    |
| --------- | -------------------------------------------------------- |
| Bukkit    | `Player`, `OfflinePlayer`, `World`, `Entity`             |
| Numbers   | `int`, `double`, `float`, `long`, `short`, `byte`        |
| General   | `String`, `boolean`, `UUID`                              |
| Enums     | `Material`, `EntityType`, and other Bukkit enums          |

Custom parameter types can be registered through the builder. See Lamp's documentation on custom parameter types.

## Optional Parameters with @Default and @Optional

`@Default` supplies a fallback value when the argument is omitted. `@Optional` makes the parameter nullable instead.

```java
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;

@Command("heal")
public void heal(Player sender, @Default("me") Player target) {
    Player toHeal = target.getName().equals("me") ? sender : target;
    toHeal.setHealth(20.0);
    Text.tell(sender, "&aHealed " + toHeal.getName());
}

@Command("teleport")
@CommandPermission("myplugin.teleport")
public void teleport(Player sender, Player target, @Optional Location location) {
    if (location == null) {
        sender.teleport(target);
        Text.tell(sender, "&aTeleported to " + target.getName());
    } else {
        sender.teleport(location);
        Text.tell(sender, "&aTeleported to location");
    }
}
```

## Subcommands via @Command Paths

Subcommands are defined by using space-separated paths in `@Command`.

```java
@Command("shop")
@CommandPermission("myplugin.shop")
public void shop(Player sender) {
    Text.tell(sender, "&6=== Shop Menu ===");
}

@Command("shop buy")
@CommandPermission("myplugin.shop.buy")
public void shopBuy(Player sender, Material item, @Default("1") int amount) {
    int cost = calculateCost(item, amount);
    Text.tell(sender, "&aPurchased " + amount + "x " + item + " for $" + cost);
}

@Command("shop sell")
@CommandPermission("myplugin.shop.sell")
public void shopSell(Player sender, Material item, @Default("1") int amount) {
    int price = calculatePrice(item, amount);
    Text.tell(sender, "&aSold " + amount + "x " + item + " for $" + price);
}
```

## Flags and Switches

| Annotation | Purpose                                   | Example usage            |
| ---------- | ----------------------------------------- | ------------------------ |
| `@Switch`  | Boolean toggle, no value                  | `--silent`               |
| `@Flag`    | Named parameter that accepts a value      | `--message "Woosh!"`     |

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
    sender.teleport(target.getLocation());

    if (!silent) {
        String message = customMessage != null
            ? customMessage
            : "&aTeleported to " + target.getName();
        Text.tell(sender, message);
    }
}
```

## Cooldowns with @Cooldown

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

When a player is on cooldown, PluginBase's `BaseExceptionHandler` sends a localized error message with the remaining time.

## Tab Completion with @Suggest and @SuggestWith

### Static suggestions with @Suggest

`@Suggest` provides a fixed list of tab-completion values.

```java
import revxrsal.commands.annotation.Suggest;

@Command("difficulty")
@CommandPermission("myplugin.difficulty")
public void setDifficulty(Player sender, @Suggest({"easy", "normal", "hard"}) String difficulty) {
    Text.tell(sender, "&aSet difficulty to " + difficulty);
}
```

### Dynamic suggestions with @SuggestWith

`@SuggestWith` references a class that implements `SuggestionProvider<BukkitCommandActor>`.

```java
import revxrsal.commands.annotation.SuggestWith;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;

public class WarpSuggestionProvider implements SuggestionProvider<BukkitCommandActor> {

    @Override
    public Collection<String> getSuggestions(ExecutionContext<BukkitCommandActor> context) {
        return getWarpNames();
    }
}
```

```java
@Command("warp")
@CommandPermission("myplugin.warp")
public void warp(Player sender, @SuggestWith(WarpSuggestionProvider.class) String warpName) {
    Location warpLocation = getWarpLocation(warpName);
    sender.teleport(warpLocation);
    Text.tell(sender, "&aTeleported to " + warpName);
}
```

### Programmatic registration via the builder

You can also register suggestion providers by type through the builder:

```java
@Override
protected void enable() {
    Lamp<BukkitCommandActor> lamp = createCommandHandler()
        .suggestionProviders(providers -> {
            providers.addProvider(String.class, context -> getWarpNames());
        })
        .build();

    lamp.register(new MyCommands());
}
```

## Parameter Validation with @Range

```java
import revxrsal.commands.annotation.Range;

@Command("setlevel")
@CommandPermission("myplugin.setlevel")
public void setLevel(Player sender, Player target, @Range(min = 0, max = 100) int level) {
    target.setLevel(level);
    Text.tell(sender, "&aSet " + target.getName() + "'s level to " + level);
}
```

## Localized Error Messages

PluginBase's `BaseExceptionHandler` and `BaseFailureHandler` handle all common command errors with localized messages. These cover invalid players, missing arguments, permission denials, cooldowns, number range violations, and more.

For localization configuration, see [Text and Localization](text-and-localization.md). Sample locale files (`.properties` and `.yml`) are available in the [samples directory](https://github.com/demengc/PluginBase/tree/main/samples).

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

public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        Lamp<BukkitCommandActor> lamp = createCommandHandler().build();
        lamp.register(new AdminCommands());
    }
}

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

## Further Reading

For features beyond what PluginBase configures, see the [Lamp documentation](https://github.com/Revxrsal/Lamp/wiki):

* **Custom Parameter Types** - Parse custom objects from command input
* **Context Parameters** - Auto-inject contextual data into command methods
* **Command Conditions** - Add pre-execution checks
* **Response Handlers** - Handle command return values
* **Help Commands** - Auto-generate help menus
* **Hooks** - Execute code before/after commands
