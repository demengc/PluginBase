---
description: Fluent API for creating ItemStacks.
---

# ItemBuilder

## Basic Usage

```java
import dev.demeng.pluginbase.item.ItemBuilder;

ItemStack item = ItemBuilder.create(Material.DIAMOND_SWORD)
    .name("&bLegendary Sword")
    .lore("&7Line 1", "&7Line 2")
    .amount(1)
    .durability((short) 100)
    .enchant(Enchantment.DAMAGE_ALL, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
    .unbreakable(true)
    .glow(true)  // Adds enchantment glow without visible enchants
    .get();
```

## Player Heads

```java
// Player head by name
ItemStack head = ItemBuilder.create(Material.PLAYER_HEAD)
    .skullOwner(player.getName())
    .name("&e" + player.getName())
    .get();

// Player head by UUID
ItemStack head2 = ItemBuilder.create(Material.PLAYER_HEAD)
    .skullOwner(player.getUniqueId())
    .name("&e" + player.getName())
    .get();

// Custom texture (base64)
ItemStack customHead = ItemBuilder.create(Material.PLAYER_HEAD)
    .skullTexture("texture_base64_here")
    .name("&6Custom Head")
    .get();
```

## Clone and Modify Existing Item

```java
ItemStack existing = player.getInventory().getItemInMainHand();
ItemStack modified = ItemBuilder.create(existing)
    .name("&aModified Item")
    .lore("&7Added lore")
    .get();
```

## Lore Management

```java
ItemBuilder builder = ItemBuilder.create(Material.DIAMOND)
    .name("&bDiamond");

// Set lore
builder.lore("&7Line 1", "&7Line 2");

// Add to lore
builder.addLore("&7Additional line");

// Clear lore
builder.clearLore();

ItemStack item = builder.get();
```

## Enchantments

```java
ItemBuilder.create(Material.DIAMOND_SWORD)
    .enchant(Enchantment.DAMAGE_ALL, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .enchant(Enchantment.KNOCKBACK, 2)
    .get();

// Remove enchantment
builder.unenchant(Enchantment.DAMAGE_ALL);

// Clear all enchantments
builder.clearEnchants();
```

## Item Flags

```java
// Add multiple flags at once
ItemBuilder.create(Material.DIAMOND_SWORD)
    .flags(
        ItemFlag.HIDE_ENCHANTS,
        ItemFlag.HIDE_ATTRIBUTES,
        ItemFlag.HIDE_UNBREAKABLE
    )
    .get();
```

## Custom Model Data

```java
ItemBuilder.create(Material.STICK)
    .modelData(1)  // For custom resource packs
    .get();
```

## Glow Effect

```java
// Add glow without visible enchants
ItemBuilder.create(Material.DIAMOND)
    .glow(true)
    .flags(ItemFlag.HIDE_ENCHANTS)
    .get();
```

## Leather Armor Color

```java
ItemBuilder.create(Material.LEATHER_CHESTPLATE)
    .name("&cRed Armor")
    .armorColor(Color.RED)
    .get();
```

## Complete Example

```java
public class ItemFactory {

    public static ItemStack createSword(int level) {
        return ItemBuilder.create(Material.DIAMOND_SWORD)
            .name("&b&lLegendary Sword &7(Level " + level + ")")
            .lore(
                "&7A powerful weapon forged by",
                "&7ancient blacksmiths.",
                "",
                "&eStats:",
                "&7▸ Damage: &c+" + (10 * level),
                "&7▸ Attack Speed: &a+15%",
                "",
                "&6&lLEGENDARY"
            )
            .enchant(Enchantment.DAMAGE_ALL, level)
            .enchant(Enchantment.FIRE_ASPECT, 2)
            .enchant(Enchantment.KNOCKBACK, 1)
            .flags(ItemFlag.HIDE_ATTRIBUTES)
            .unbreakable(true)
            .glow(true)
            .get();
    }

    public static ItemStack createArmor(Player player) {
        return ItemBuilder.create(Material.DIAMOND_CHESTPLATE)
            .name("&b" + player.getName() + "'s Armor")
            .lore(
                "&7Soulbound to " + player.getName(),
                "&7Cannot be traded or dropped"
            )
            .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
            .enchant(Enchantment.DURABILITY, 3)
            .flags(ItemFlag.HIDE_ENCHANTS)
            .unbreakable(true)
            .get();
    }

    public static ItemStack createToken(int amount) {
        return ItemBuilder.create(Material.SUNFLOWER)
            .name("&6&lServer Token")
            .lore(
                "&7A valuable currency",
                "&7Amount: &e" + amount
            )
            .amount(amount)
            .glow(true)
            .flags(ItemFlag.HIDE_ENCHANTS)
            .get();
    }

    public static ItemStack createMenuButton(String name, Material icon) {
        return ItemBuilder.create(icon)
            .name(name)
            .lore("&eClick to select")
            .get();
    }
}

// Usage
Events.subscribe(PlayerJoinEvent.class)
    .handler(e -> {
        Player player = e.getPlayer();
        player.getInventory().addItem(
            ItemFactory.createSword(1),
            ItemFactory.createArmor(player),
            ItemFactory.createToken(10)
        );
    })
    .bindWith(this);
```

## Comparison: Traditional vs ItemBuilder

### Traditional

```java
ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = item.getItemMeta();
meta.setDisplayName(ChatColor.BLUE + "Legendary Sword");
List<String> lore = new ArrayList<>();
lore.add(ChatColor.GRAY + "A powerful weapon");
meta.setLore(lore);
meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
meta.setUnbreakable(true);
item.setItemMeta(meta);
```

### ItemBuilder

```java
ItemStack item = ItemBuilder.create(Material.DIAMOND_SWORD)
    .name("&bLegendary Sword")
    .lore("&7A powerful weapon")
    .enchant(Enchantment.DAMAGE_ALL, 5)
    .flags(ItemFlag.HIDE_ENCHANTS)
    .unbreakable(true)
    .get();
```

## Cross-Version Compatibility

PluginBase uses [XSeries](https://github.com/cryptomorin/xseries) for cross-version compatibility. To learn more about cross-version materials (`XMaterial`), refer to XSeries' documentation.
