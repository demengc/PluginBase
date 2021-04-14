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

package dev.demeng.pluginbase.menu;

import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.item.ItemBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A GUI menu (custom inventory) that will be displayed to a player.
 */
public abstract class Menu {

  /**
   * A map of all menus created within the plugin, with the key being the UUID of the menu, and the
   * value being the menu object.
   */
  @NotNull @Getter private static final Map<UUID, Menu> menus = new HashMap<>();

  /**
   * A map of the menu a player has open, with the key being the UUID of the player, and the value
   * being the UUID of the menu they have open.
   */
  @NotNull @Getter protected static final Map<UUID, UUID> openMenus = new HashMap<>();

  /**
   * The unique ID of this menu.
   */
  @NotNull @Getter private final UUID uuid = UUID.randomUUID();

  /**
   * The {@link Inventory} related to this menu.
   */
  @NotNull @Getter private final Inventory inventory;

  /**
   * A map of all of the actions for the menu buttons, with the key being the slot number, and the
   * value being the consumer for the click event if the slot clicked matches the key.
   */
  @NotNull @Getter private final Map<Integer, Consumer<InventoryClickEvent>> actions
      = new HashMap<>();

  /**
   * Creates a new menu with the specified size and inventory title.
   *
   * @param size  The size of the menu, in slots; must be >= 9 and <= 54, and a multiple of 9
   * @param title The menu title, colorized internally
   */
  protected Menu(final int size, @NotNull final String title) {
    this.inventory = Bukkit.createInventory(null, size, ChatUtils.colorize(title));
    menus.put(getUuid(), this);
  }

  /**
   * Creates and adds a new button to the menu.
   *
   * @param slot    The slot of the button
   * @param stack   The stack of the button
   * @param actions The actions of the button
   */
  public void setItem(final int slot, @NotNull final ItemStack stack,
      @Nullable final Consumer<InventoryClickEvent> actions) {
    setItem(new MenuButton(slot, stack, actions));
  }

  /**
   * Adds a new button to the menu.
   *
   * @param button The button to add
   */
  public void setItem(@Nullable final MenuButton button) {

    if (button == null || button.getSlot() < 0) {
      return;
    }

    inventory.setItem(button.getSlot(), button.getStack());

    if (button.getConsumer() != null) {
      actions.put(button.getSlot(), button.getConsumer());
    }
  }

  /**
   * Sets the background material of the menu.
   *
   * @param material The background material of the menu, usually stained glass panes
   */
  public void setBackground(@Nullable final Material material) {

    if (material == null || material == Material.AIR) {
      return;
    }

    for (int slot = 0; slot < inventory.getSize(); slot++) {
      final ItemStack current = inventory.getItem(slot);
      if (current == null || current.getType() == Material.AIR) {
        setItem(new MenuButton(slot, new ItemBuilder(material).name("&0").get(), null));
      }
    }
  }

  /**
   * Opens the menu for the provided player(s).
   *
   * @param players The players the menu should be opened to
   */
  public void open(final Player... players) {
    for (final Player player : players) {
      player.openInventory(inventory);
      openMenus.put(player.getUniqueId(), uuid);
    }
  }
}
