---
description: Interactive inventory GUIs with clickable items.
---

# Menus

PluginBase provides `Menu` for single-page inventories and `PagedMenu` for paginated inventories. Both extend `IMenu`, which handles opening and close callbacks. `MenuManager` is registered automatically by `BasePlugin`.

## Single-page menu

Extend `Menu`, call `super(size, title)`, and populate buttons in the constructor or a helper method.

```java
import dev.demeng.pluginbase.menu.layout.Menu;
import dev.demeng.pluginbase.menu.model.MenuButton;
import dev.demeng.pluginbase.item.ItemBuilder;
import dev.demeng.pluginbase.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShopMenu extends Menu {

    public ShopMenu() {
        super(27, "&6Shop");
        populate();
    }

    private void populate() {
        addButton(10, ItemBuilder.create(Material.DIAMOND)
            .name("&bDiamonds")
            .lore("&7Price: $100")
            .get(), click -> {
                Player player = (Player) click.getWhoClicked();
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

        setBackground(ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .get());
    }
}
```

Open the menu:

```java
new ShopMenu().open(player);
```

`open(Player... players)` accepts varargs, so you can open for multiple players at once:

```java
menu.open(player1, player2, player3);
```

## Paginated menu

Extend `PagedMenu` and implement a `Settings` object that controls navigation buttons and available slots.

Buttons with slot `-1` are auto-assigned across pages. Buttons with a slot `>= 0` are placed on the first page at that fixed position.

```java
import dev.demeng.pluginbase.menu.layout.PagedMenu;
import dev.demeng.pluginbase.menu.model.MenuButton;
import dev.demeng.pluginbase.item.ItemBuilder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerListMenu extends PagedMenu {

    public PlayerListMenu(List<Player> players) {
        super(54, "&bPlayers - Page %current-page%", createSettings());

        List<MenuButton> buttons = new ArrayList<>();
        for (Player player : players) {
            buttons.add(MenuButton.create(
                -1,
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

        fill(buttons);
    }

    private static Settings createSettings() {
        return new Settings() {
            @Override
            public MenuButton getPreviousButton() {
                return MenuButton.create(45,
                    ItemBuilder.create(Material.ARROW).name("&aPrevious Page").get(), null);
            }

            @Override
            public MenuButton getDummyPreviousButton() {
                return MenuButton.create(45,
                    ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                        .name("&cNo previous page").get(), null);
            }

            @Override
            public MenuButton getNextButton() {
                return MenuButton.create(53,
                    ItemBuilder.create(Material.ARROW).name("&aNext Page").get(), null);
            }

            @Override
            public MenuButton getDummyNextButton() {
                return MenuButton.create(53,
                    ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                        .name("&cNo next page").get(), null);
            }

            @Override
            public List<Integer> getAvailableSlots() {
                return IntStream.range(0, 45).boxed().collect(Collectors.toList());
            }
        };
    }

    @Override
    public boolean onClose(InventoryCloseEvent event) {
        return false;
    }
}
```

### Loading settings from config

`Settings.fromConfig(ConfigurationSection)` parses navigation buttons and available slots from YAML. Config slots are 1-indexed and converted automatically.

```java
public class SimplePagedMenu extends PagedMenu {

    public SimplePagedMenu(ConfigurationSection config, List<ItemStack> items) {
        super(54, "&6Items - Page %current-page%", Settings.fromConfig(config));

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

### Static buttons on all pages

Use `addStaticButton(MenuButton)` or `addButton(int slot, ItemStack, Consumer)` after calling `fill()` to place a button on every page.

## MenuButton factory methods

`MenuButton.create(...)` has several overloads:

| Signature | Slot handling |
|---|---|
| `create(int slot, ItemStack stack, Consumer action)` | Uses slot as-is (0-indexed) |
| `create(ConfigurationSection section, Consumer action)` | Reads `slot` from config, subtracts 1 |
| `create(ConfigurationSection section, UnaryOperator translator, Consumer action)` | Same as above, with string translator |
| `create(int slot, ConfigurationSection section, Consumer action)` | Overrides config slot, no subtraction |
| `create(int slot, ConfigurationSection section, UnaryOperator translator, Consumer action)` | Same as above, with string translator |

## Layout fill methods

All fill methods only affect empty slots. The item's display name is set to `"&0"` (invisible) automatically.

| Method | Parameters | Indexing |
|---|---|---|
| `setBackground(ItemStack)` | Fill material | Fills all empty slots |
| `setBorder(ItemStack)` | Fill material | Fills the outer edge (top row, bottom row, left column, right column) |
| `setRow(int row, ItemStack)` | Row number (1-based, top to bottom), fill material | Row 1 = top, row 6 = bottom of a 54-slot inventory |
| `setColumn(int col, ItemStack)` | Column number (1-based, left to right), fill material | Column 1 = leftmost, column 9 = rightmost |
| `applyFillersFromConfig(ConfigurationSection)` | Config section | Reads `background`, `border`, `row`, `column`, `custom` keys |

## Inventory layout and slot indexing

A 54-slot (6-row) inventory uses 0-based slot indices in code:

```
Row 1:  [ 0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]
Row 2:  [ 9][10][11][12][13][14][15][16][17]
Row 3:  [18][19][20][21][22][23][24][25][26]
Row 4:  [27][28][29][30][31][32][33][34][35]
Row 5:  [36][37][38][39][40][41][42][43][44]
Row 6:  [45][46][47][48][49][50][51][52][53]
```

| Context | Slots | Rows / Columns |
|---|---|---|
| Java code (`addButton`, constructor) | 0-indexed (0-53) | `setRow` and `setColumn` are 1-indexed |
| Config files (`slot` key, `custom` filler slots, `available-slots`) | 1-indexed (1-54), auto-converted | `row` and `column` filler keys are 1-indexed |

## `onClose` callback

Override `onClose(InventoryCloseEvent)` in your menu subclass. Return `true` to re-open the menu (cancels close), `false` to allow it to close normally.

## Full example with command registration

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        Lamp<BukkitCommandActor> handler = createCommandHandler().build();
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
        super(36, "&6&lShop");
        populate();
    }

    private void populate() {
        addButton(10, ItemBuilder.create(Material.DIAMOND_SWORD)
            .name("&bDiamond Sword")
            .lore("&7Price: $500", "", "&eClick to purchase")
            .get(), this::purchaseWeapon);

        addButton(12, ItemBuilder.create(Material.DIAMOND_CHESTPLATE)
            .name("&bDiamond Armor")
            .lore("&7Price: $1000", "", "&eClick to purchase")
            .get(), this::purchaseArmor);

        addButton(14, ItemBuilder.create(Material.COOKED_BEEF)
            .name("&6Food Pack")
            .lore("&7Price: $50", "", "&eClick to purchase")
            .get(), this::purchaseFood);

        addButton(31, ItemBuilder.create(Material.BARRIER)
            .name("&cClose")
            .get(), click -> ((Player) click.getWhoClicked()).closeInventory());

        setBorder(ItemBuilder.create(Material.BLACK_STAINED_GLASS_PANE)
            .name(" ")
            .get());
    }

    private void purchaseWeapon(InventoryClickEvent click) {
        Player player = (Player) click.getWhoClicked();
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
        Text.tell(player, "&aPurchased Diamond Sword!");
        player.closeInventory();
    }

    private void purchaseArmor(InventoryClickEvent click) {
        // Purchase logic
    }

    private void purchaseFood(InventoryClickEvent click) {
        // Purchase logic
    }
}
```
