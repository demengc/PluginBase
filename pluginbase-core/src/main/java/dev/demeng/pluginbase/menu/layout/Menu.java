/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.pluginbase.menu.layout;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.DynamicPlaceholders;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.item.ItemBuilder;
import dev.demeng.pluginbase.menu.IMenu;
import dev.demeng.pluginbase.menu.MenuManager;
import dev.demeng.pluginbase.menu.model.MenuButton;
import dev.demeng.pluginbase.serializer.ItemSerializer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A GUI menu (custom inventory) that will be displayed to a player.
 */
public abstract class Menu implements IMenu {

  /**
   * The unique ID of this menu.
   */
  @NotNull @Getter private final UUID uuid = UUID.randomUUID();

  /**
   * The {@link Inventory} related to this menu.
   */
  @NotNull @Getter private final Inventory inventory;

  /**
   * A map of all the actions for the menu buttons, with the key being the slot number, and the
   * value being the consumer for the click event if the slot clicked matches the key.
   */
  @NotNull @Getter private final Map<Integer, Consumer<InventoryClickEvent>> actions
      = new HashMap<>();

  private static final String EMPTY_NAME = "&0";

  /**
   * Creates a new menu with the specified size and inventory title.
   *
   * @param size  The size of the menu, in slots; must be greater than or equal to 9 and less than
   *              or equal to 54, and a multiple of 9
   * @param title The menu title, colorized internally
   */
  protected Menu(final int size, @NotNull final String title) {
    this.inventory = Bukkit.createInventory(null, size, ChatUtils.colorize(title));
    MenuManager.getMenus().put(getUuid(), this);
  }

  /**
   * Adds a new button to the menu.
   *
   * @param button The button to add
   */
  public void addButton(@Nullable final MenuButton button) {

    if (button == null || button.getSlot() < 0) {
      return;
    }

    inventory.setItem(button.getSlot(), button.getStack());

    if (button.getConsumer() != null) {
      actions.put(button.getSlot(), button.getConsumer());
    }
  }

  /**
   * Creates and adds a new button to the menu.
   *
   * @param slot    The slot of the button
   * @param stack   The stack of the button
   * @param actions The actions of the button
   */
  public void addButton(final int slot, @NotNull final ItemStack stack,
      @Nullable final Consumer<InventoryClickEvent> actions) {
    addButton(new MenuButton(slot, stack, actions));
  }

  /**
   * Applies a background filler, which sets all empty slots in the menu with the dummy button.
   *
   * @param stack The background material
   */
  public void setBackground(@Nullable final ItemStack stack) {

    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }

    for (int slot = 0; slot < inventory.getSize(); slot++) {
      final ItemStack current = inventory.getItem(slot);
      if (current == null || current.getType() == Material.AIR) {
        addButton(new MenuButton(slot,
            new ItemBuilder(stack).name(EMPTY_NAME).build(), null));
      }
    }
  }

  /**
   * Applies a row filler, which sets all empty slots in a row with the dummy button.
   *
   * @param row   The row to fill (top to bottom)
   * @param stack The fill material
   */
  public void setRow(final int row, @Nullable final ItemStack stack) {

    if (stack == null || stack.getType() == Material.AIR
        || row <= 0 || row > inventory.getSize() / 9) {
      return;
    }

    for (int i = 0; i < 9; i++) {

      final int slot = ((row - 1) * 9) + i;
      final ItemStack current = inventory.getItem(slot);

      if (current == null || current.getType() == Material.AIR) {
        addButton(
            new MenuButton(slot,
                new ItemBuilder(stack).name(EMPTY_NAME).build(), null));
      }
    }
  }

  /**
   * Applies a column filler, which sets all empty slots in a column with the dummy button.
   *
   * @param column The column to fill (left to right)
   * @param stack  The fill material
   */
  public void setColumn(final int column, @Nullable final ItemStack stack) {

    if (stack == null || stack.getType() == Material.AIR || column <= 0 || column > 9) {
      return;
    }

    for (int i = 0; i < (inventory.getSize() / 9); i++) {

      final int slot = (i * 9) + (column - 1);
      final ItemStack current = inventory.getItem(slot);

      if (current == null || current.getType() == Material.AIR) {
        addButton(
            new MenuButton(slot,
                new ItemBuilder(stack).name(EMPTY_NAME).build(), null));
      }
    }
  }

  /**
   * Applies a border filler, which sets all empty slots on the edges of the menu with a dummy
   * button.
   *
   * @param stack The fill material
   */
  public void setBorder(@Nullable final ItemStack stack) {

    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }

    for (int i = 0; i < inventory.getSize(); i++) {
      if (i < 9 || i >= inventory.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {

        final ItemStack current = inventory.getItem(i);

        if (current == null || current.getType() == Material.AIR) {
          addButton(
              new MenuButton(i,
                  new ItemBuilder(stack).name(EMPTY_NAME).build(), null));
        }
      }
    }
  }

  /**
   * Applies all menu fillers that are declared in a configuration section.
   *
   * @param section The configuration containing the fillers to set
   */
  public void applyFillersFromConfig(@NotNull final ConfigurationSection section) {
    applyFillersFromConfig(section, null);
  }

  /**
   * Applies all menu fillers that are declared in a configuration section.
   *
   * @param section      The configuration containing the fillers to set
   * @param placeholders The placeholders to use for custom fillers
   */
  public void applyFillersFromConfig(
      @NotNull final ConfigurationSection section,
      @Nullable final DynamicPlaceholders placeholders) {

    for (final String fillerType : section.getKeys(false)) {

      switch (fillerType) {
        case "background":
          setBackground(ItemBuilder.getMaterial(section.getString("background")));
          break;

        case "row":
          applyRowFillerFromConfig(section.getConfigurationSection("row"));
          break;

        case "column":
          applyColumnFillerFromConfig(section.getConfigurationSection("column"));
          break;

        case "border":
          setBorder(ItemBuilder.getMaterial(section.getString("border")));
          break;

        case "custom":
          applyCustomFillerFromConfig(section.getConfigurationSection("custom"), placeholders);
          break;

        default:
          break;
      }
    }
  }

  private void applyRowFillerFromConfig(final ConfigurationSection rowSection) {

    if (rowSection == null) {
      return;
    }

    for (final String strRow : rowSection.getKeys(false)) {
      try {
        Common.forEachInt(strRow, row ->
            setRow(row, ItemBuilder.getMaterial(rowSection.getString(strRow))));
      } catch (final IllegalArgumentException ex) {
        Common.error(ex, "Failed to apply row filler.", false);
        return;
      }
    }
  }

  private void applyColumnFillerFromConfig(final ConfigurationSection columnSection) {

    if (columnSection == null) {
      return;
    }

    for (final String strColumn : columnSection.getKeys(false)) {
      try {
        Common.forEachInt(strColumn, column ->
            setColumn(column, ItemBuilder.getMaterial(columnSection.getString(strColumn))));
      } catch (final IllegalArgumentException ex) {
        Common.error(ex, "Failed to apply column filler.", false);
        return;
      }
    }
  }

  private void applyCustomFillerFromConfig(
      final ConfigurationSection customSection,
      final DynamicPlaceholders placeholders) {

    if (customSection == null) {
      return;
    }

    for (final String strSlot : customSection.getKeys(false)) {

      try {
        Common.forEachInt(strSlot, slot -> {
          final ConfigurationSection slotSection = customSection.getConfigurationSection(strSlot);

          if (slotSection == null) {
            return;
          }

          addButton(slot - 1, ItemSerializer.get().deserialize(slotSection, placeholders), null);
        });

      } catch (final IllegalArgumentException ex) {
        Common.error(ex, "Failed to apply custom filler.", false);
        return;
      }
    }
  }

  @Override
  public void open(final Player... players) {
    for (final Player player : players) {
      player.openInventory(inventory);
      MenuManager.getOpenedMenus().put(player.getUniqueId(), uuid);
    }
  }
}
