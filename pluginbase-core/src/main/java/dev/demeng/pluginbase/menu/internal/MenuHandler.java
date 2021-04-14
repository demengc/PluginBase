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

package dev.demeng.pluginbase.menu.internal;

import dev.demeng.pluginbase.menu.Menu;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Internal listener for handling menu interactions.
 */
public class MenuHandler implements Listener {

  /**
   * Handles button interaction within menus.
   */
  @EventHandler(priority = EventPriority.HIGH)
  public void onInventoryClick(InventoryClickEvent event) {

    if (event.getClickedInventory() == null
        || event.getClickedInventory().getType() == InventoryType.PLAYER) {
      return;
    }

    final Player p = (Player) event.getWhoClicked();
    final UUID inventoryUuid = Menu.getOpenMenus().get(p.getUniqueId());

    if (inventoryUuid != null) {
      event.setCancelled(true);

      final Consumer<InventoryClickEvent> actions =
          Menu.getMenus().get(inventoryUuid).getActions().get(event.getSlot());

      if (actions != null) {
        actions.accept(event);
      }
    }
  }

  /**
   * Handles cleanup when a menu is closed.
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onInventoryClose(InventoryCloseEvent event) {
    Menu.getOpenMenus().remove(event.getPlayer().getUniqueId());
  }

  /**
   * Handles cleanup when a player leaves.
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    Menu.getOpenMenus().remove(event.getPlayer().getUniqueId());
  }
}
