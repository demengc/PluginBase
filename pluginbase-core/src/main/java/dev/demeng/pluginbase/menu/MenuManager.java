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

import dev.demeng.pluginbase.menu.layouts.Menu;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Internal listener for handling menu interactions.
 */
public class MenuManager implements Listener {

  /**
   * A map of all menus created within the plugin, with the key being the UUID of the menu, and the
   * value being the menu object.
   */
  @NotNull @Getter private static final Map<UUID, Menu> menus = new HashMap<>();

  /**
   * A map of the menu a player has open, with the key being the UUID of the player, and the value
   * being the UUID of the menu they have open.
   */
  @NotNull @Getter private static final Map<UUID, UUID> openedMenus = new HashMap<>();

  /**
   * A map of the paged menu a player has open, with the key being the UUID of the player, and the
   * value being the UUID of the paged menu they have open.
   */
  @NotNull @Getter private static final Map<UUID, UUID> openedPagedMenus = new HashMap<>();

  /**
   * The current page of the menu each player is on, with the key being the player and the value
   * being the page index. Used for paged menus.
   */
  @NotNull @Getter private static final Map<UUID, Integer> openedPages = new HashMap<>();

  /**
   * Handles button interaction within menus.
   */
  @EventHandler(priority = EventPriority.HIGH)
  public void onInventoryClick(final InventoryClickEvent event) {

    if (event.getClickedInventory() == null) {
      return;
    }

    final Player p = (Player) event.getWhoClicked();
    final UUID inventoryUuid = openedMenus.get(p.getUniqueId());

    if (inventoryUuid != null) {
      event.setCancelled(true);

      if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
        return;
      }

      final Consumer<InventoryClickEvent> actions =
          menus.get(inventoryUuid).getActions().get(event.getSlot());

      if (actions != null) {
        actions.accept(event);
      }
    }
  }

  /**
   * Handles cleanup when a menu is closed.
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onInventoryClose(final InventoryCloseEvent event) {
    cleanup((Player) event.getPlayer());
  }

  /**
   * Handles cleanup when a player leaves.
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    cleanup(event.getPlayer());
  }

  private void cleanup(final Player p) {
    final UUID uuid = p.getUniqueId();
    openedMenus.remove(uuid);
    openedPages.remove(uuid);
  }
}
