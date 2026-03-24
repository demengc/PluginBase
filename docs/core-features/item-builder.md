---
description: Fluent API for creating ItemStacks.
---

# ItemBuilder

`ItemBuilder` wraps Bukkit's `ItemStack` and `ItemMeta` manipulation into a chainable builder. Call `.get()` to retrieve the final `ItemStack`.

## Creating a builder

| Factory method | Description |
|---|---|
| `ItemBuilder.create(ItemStack stack)` | Clones an existing item stack (null defaults to `STONE`) |
| `ItemBuilder.create(Material material)` | Creates a new stack with amount 1 |
| `ItemBuilder.create(Material material, int amount)` | Creates a new stack with the given amount |
| `ItemBuilder.create(Material material, int amount, short damage)` | Creates a new stack with amount and damage value |

All `Material` parameters default to `STONE` if null.

```java
ItemBuilder.create(Material.DIAMOND_SWORD)
    .name("&bLegendary Sword")
    .lore("&7A powerful weapon")
    .enchant(Enchantment.DAMAGE_ALL, 5)
    .flags(ItemFlag.HIDE_ATTRIBUTES)
    .unbreakable(true)
    .glow(true)
    .get();
```

## Builder methods

### General

| Method | Parameters | Description |
|---|---|---|
| `name(String)` | Display name (color codes auto-applied) | Sets the display name |
| `amount(int)` | Stack size | Sets the stack amount |
| `durability(short)` | Durability value | Sets the item durability |
| `lore(String...)` | Lore lines (color codes auto-applied) | Replaces lore; each string is one line |
| `lore(List<String>)` | Lore lines | Same as above, from a list |
| `addLore(String)` | Single line | Appends one line to existing lore |
| `clearLore()` | | Removes all lore |
| `enchant(Enchantment, int)` | Enchantment, level | Adds a safe enchantment |
| `enchant(Enchantment, int, boolean)` | Enchantment, level, safe | Adds an enchantment; `safe=false` allows unsafe levels |
| `enchant(Map<Enchantment, Integer>)` | Enchantment map | Adds all enchantments from the map |
| `unenchant(Enchantment)` | Enchantment to remove | Removes a specific enchantment |
| `clearEnchants()` | | Removes all enchantments |
| `flags(ItemFlag...)` | Flags to add | Adds item flags |
| `clearFlags()` | | Removes all item flags |
| `unbreakable(boolean)` | true/false | Sets unbreakable state |
| `glow(boolean)` | true/false | Adds Unbreaking I + `HIDE_ENCHANTS` for a visual glow effect |
| `modelData(Integer)` | Custom model data (1.14+, ignored on older versions) | Sets custom model data for resource packs |

### Item-specific

| Method | Parameters | Description |
|---|---|---|
| `skullOwner(String)` | Player name | Sets skull owner by name (player heads only) |
| `skullOwner(UUID)` | Player UUID | Sets skull owner by UUID (player heads only) |
| `skullTexture(String)` | Base64-encoded texture | Sets a custom skull texture (player heads only) |
| `armorColor(Color)` | Bukkit `Color` | Sets leather armor color (leather armor only) |

### Utilities

| Method | Returns | Description |
|---|---|---|
| `get()` | `ItemStack` | Returns the built item stack |
| `copy()` | `ItemBuilder` | Returns a new builder sharing the same `ItemStack` reference (not a deep clone) |

## Player heads

```java
ItemBuilder.create(Material.PLAYER_HEAD)
    .skullOwner("Notch")
    .name("&eNotch's Head")
    .get();

ItemBuilder.create(Material.PLAYER_HEAD)
    .skullOwner(player.getUniqueId())
    .name("&e" + player.getName())
    .get();

ItemBuilder.create(Material.PLAYER_HEAD)
    .skullTexture("eyJ0ZXh0dXJlcyI6ey...")
    .name("&6Custom Head")
    .get();
```

## Modifying an existing item

`create(ItemStack)` clones the input, so the original is not modified.

```java
ItemStack existing = player.getInventory().getItemInMainHand();
ItemStack modified = ItemBuilder.create(existing)
    .name("&aModified Item")
    .addLore("&7New line")
    .get();
```

## Leather armor color

```java
ItemBuilder.create(Material.LEATHER_CHESTPLATE)
    .name("&cRed Armor")
    .armorColor(Color.RED)
    .get();
```

## Custom model data

Requires Minecraft 1.14+. On older versions, the call is silently ignored.

```java
ItemBuilder.create(Material.STICK)
    .modelData(1)
    .get();
```

## Glow effect

`glow(true)` adds Unbreaking I and the `HIDE_ENCHANTS` flag. No need to add the flag separately. Best used for GUI items where the enchantment is purely visual.

```java
ItemBuilder.create(Material.DIAMOND)
    .glow(true)
    .get();
```

## Static material utilities

These methods parse a material string (using XMaterial for cross-version support) into an `ItemStack` with no meta:

| Method | Returns | On invalid input |
|---|---|---|
| `getMaterialSafe(String)` | `Optional<ItemStack>` | Returns empty optional |
| `getMaterial(String)` | `ItemStack` | Logs error, returns `STONE` |
| `getMaterialOrDef(String, ItemStack)` | `ItemStack` | Returns the provided default |

## Cross-version compatibility

PluginBase uses [XSeries](https://github.com/cryptomorin/xseries) for cross-version material and enchantment support. Refer to XSeries documentation for `XMaterial` mappings.
