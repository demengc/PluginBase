# Menus

Interactive inventory GUIs with clickable items.

## Basic Menu

```java
import dev.demeng.pluginbase.menu.layout.Menu;
import dev.demeng.pluginbase.item.ItemBuilder;

public class ShopMenu extends Menu {

    public ShopMenu() {
        super(27, "&6Shop");  // Size (slots), title
        populateItems();
    }

    private void populateItems() {
        // Add items
        addButton(10, ItemBuilder.create(Material.DIAMOND)
            .name("&bDiamonds")
            .lore("&7Price: $100")
            .get(), click -> {
                Player player = (Player) click.getWhoClicked();
                // Handle purchase
                Text.tell(player, "&aPurchased diamonds!");
                player.closeInventory();
            });

        addButton(12, ItemBuilder.create(Material.GOLD_INGOT)
            .name("&6Gold")
            .lore("&7Price: $50")
            .get(), click -> {
                Player player = (Player) click.getWhoClicked();
                Text.tell(player, "&aPurchased gold!");
                player.closeInventory();
            });

        // Fill empty slots with glass
        setBackground(ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .get());
    }
}
```

## Open Menu

```java
ShopMenu menu = new ShopMenu();
menu.open(player);
```

## Paged Menu

```java
import dev.demeng.pluginbase.menu.layout.PagedMenu;
import dev.demeng.pluginbase.menu.model.MenuButton;
import java.util.stream.IntStream;

public class PlayerListMenu extends PagedMenu {

    public PlayerListMenu(List<Player> players) {
        super(54, "&bPlayers - Page %current-page%", createSettings());

        // Create buttons for each player
        List<MenuButton> buttons = new ArrayList<>();
        for (Player player : players) {
            buttons.add(MenuButton.create(
                -1,  // Auto-assign slot
                ItemBuilder.create(Material.PLAYER_HEAD)
                    .skullOwner(player.getName())
                    .name("&e" + player.getName())
                    .lore("&7Click to teleport")
                    .get(),
                click -> {
                    Player viewer = (Player) click.getWhoClicked();
                    viewer.teleport(player.getLocation());
                    viewer.closeInventory();
                }
            ));
        }

        // Fill pages with buttons
        fill(buttons);
    }

    private static Settings createSettings() {
        return new Settings() {
            @Override
            public MenuButton getPreviousButton() {
                return MenuButton.create(
                    45,
                    ItemBuilder.create(Material.ARROW)
                        .name("&aPrevious Page")
                        .get(),
                    null
                );
            }

            @Override
            public MenuButton getDummyPreviousButton() {
                return MenuButton.create(
                    45,
                    ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                        .name("&cNo previous page")
                        .get(),
                    null
                );
            }

            @Override
            public MenuButton getNextButton() {
                return MenuButton.create(
                    53,
                    ItemBuilder.create(Material.ARROW)
                        .name("&aNext Page")
                        .get(),
                    null
                );
            }

            @Override
            public MenuButton getDummyNextButton() {
                return MenuButton.create(
                    53,
                    ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                        .name("&cNo next page")
                        .get(),
                    null
                );
            }

            @Override
            public List<Integer> getAvailableSlots() {
                // Slots 0-44 (excluding navigation buttons at 45 and 53)
                return IntStream.range(0, 45).boxed().collect(Collectors.toList());
            }
        };
    }

    @Override
    public boolean onClose(InventoryCloseEvent event) {
        // Cleanup when menu closes
        return false;
    }
}
```

### Simpler PagedMenu Example

You can also load Settings from config:

```java
public class SimplePagedMenu extends PagedMenu {

    public SimplePagedMenu(ConfigurationSection config, List<ItemStack> items) {
        super(
            54,
            "&6Items - Page %current-page%",
            Settings.fromConfig(config)  // Load from config
        );

        List<MenuButton> buttons = items.stream()
            .map(item -> MenuButton.create(-1, item, null))
            .collect(Collectors.toList());

        fill(buttons);
    }

    @Override
    public boolean onClose(InventoryCloseEvent event) {
        return false;
    }
}
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        // Command to open shop
        Lamp<BukkitCommandActor> handler = createCommandHandler();
        handler.register(new ShopCommands());
    }
}

public class ShopCommands {

    @Command("shop")
    public void shop(Player sender) {
        new ShopMenu().open(sender);
    }
}

public class ShopMenu extends Menu {

    public ShopMenu() {
        super(36, "&6&lShop");  // 4 rows = 36 slots
        populateItems();
    }

    private void populateItems() {
        // Weapons
        addButton(10, ItemBuilder.create(Material.DIAMOND_SWORD)
            .name("&bDiamond Sword")
            .lore("&7Price: $500", "", "&eClick to purchase")
            .get(), this::purchaseWeapon);

        // Armor
        addButton(12, ItemBuilder.create(Material.DIAMOND_CHESTPLATE)
            .name("&bDiamond Armor")
            .lore("&7Price: $1000", "", "&eClick to purchase")
            .get(), this::purchaseArmor);

        // Food
        addButton(14, ItemBuilder.create(Material.COOKED_BEEF)
            .name("&6Food Pack")
            .lore("&7Price: $50", "", "&eClick to purchase")
            .get(), this::purchaseFood);

        // Close button
        addButton(31, ItemBuilder.create(Material.BARRIER)
            .name("&cClose")
            .get(), click -> {
                Player player = (Player) click.getWhoClicked();
                player.closeInventory();
            });

        // Decorative borders
        setBorder(ItemBuilder.create(Material.BLACK_STAINED_GLASS_PANE)
            .name(" ")
            .get());
    }

    private void purchaseWeapon(InventoryClickEvent click) {
        Player player = (Player) click.getWhoClicked();
        if (hasEnoughMoney(player, 500)) {
            takeMoney(player, 500);
            player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
            Text.tell(player, "&aPurchased Diamond Sword!");
            player.closeInventory();
        } else {
            Text.tell(player, "&cNot enough money!");
        }
    }

    private void purchaseArmor(InventoryClickEvent click) {
        Player player = (Player) click.getWhoClicked();
        // Purchase logic
    }

    private void purchaseFood(InventoryClickEvent click) {
        Player player = (Player) click.getWhoClicked();
        // Purchase logic
    }
}
```

## Layout Methods

### Fill Background

```java
// Fill all empty slots
setBackground(ItemStack item);
```

### Fill Border

```java
// Fill border slots (outer edge)
setBorder(ItemStack item);
```

### Fill Row

```java
// Fill entire row (1-6 for 6-row inventory)
setRow(int row, ItemStack item);
```

### Fill Column

```java
// Fill entire column (1-9)
setColumn(int column, ItemStack item);
```

## Menu State

```java
// Open for multiple players
menu.open(player1, player2, player3);
```

## Adding Buttons

```java
// Add button at specific slot (0-indexed: 0-53 for 54-slot inventory)
addButton(int slot, ItemStack item, Consumer<InventoryClickEvent> handler);

// Add button to first empty slot
addButton(ItemStack item, Consumer<InventoryClickEvent> handler);

// Add button object
MenuButton button = MenuButton.create(slot, itemStack, clickHandler);
addButton(button);
```

## Slot Indexing

**Important:** Slot indexing differs between code and configuration files:

- **In Java code:** Slots are **0-indexed** (0-53 for a 54-slot inventory)
  ```java
  addButton(0, item, handler);  // First slot (top-left)
  addButton(53, item, handler); // Last slot (bottom-right in 6-row GUI)
  ```

- **In config files:** Slots are **1-indexed** (1-54 for a 54-slot inventory)
  ```yaml
  slot: 1   # First slot (automatically converted to 0 internally)
  slot: 54  # Last slot (automatically converted to 53 internally)
  ```

The framework automatically converts config slots by subtracting 1. This makes config files more user-friendly while keeping code consistent with Bukkit's 0-indexed inventory API.
