/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

import com.cryptomorin.xseries.messages.Titles;
import dev.demeng.pluginbase.Events;
import dev.demeng.pluginbase.Schedulers;
import dev.demeng.pluginbase.menu.layout.Menu;
import dev.demeng.pluginbase.terminable.TerminableConsumer;
import dev.demeng.pluginbase.terminable.module.TerminableModule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal listener for handling menu interactions.
 */
public class MenuManager implements TerminableModule {

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
   * The current page of the menu each player is on, with the key being the player and the value
   * being the page index. Used for paged menus.
   */
  @NotNull @Getter private static final Map<UUID, Integer> openedPages = new HashMap<>();

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {

    Events.subscribe(InventoryClickEvent.class, EventPriority.HIGH)
        .filter(event -> event.getClickedInventory() != null)
        .handler(event -> {
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
        })
        .bindWith(consumer);

    Events.subscribe(InventoryCloseEvent.class, EventPriority.MONITOR)
        .handler(event -> {
          final Player p = (Player) event.getPlayer();
          final UUID inventoryUuid = openedMenus.get(p.getUniqueId());

          if (inventoryUuid != null) {
            final Menu menu = menus.get(inventoryUuid);

            if (menu != null && menu.onClose(event)) {
              Schedulers.sync().runLater(() -> menu.open(p), 1L);
              return;
            }
          }

          cleanup(p, inventoryUuid);
        })
        .bindWith(consumer);

    Events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR)
        .handler(event -> Titles.clearTitle(event.getPlayer()))
        .bindWith(consumer);

    Events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR)
        .handler(event -> cleanup(event.getPlayer()))
        .bindWith(consumer);
  }

  private void cleanup(final Player p) {
    cleanup(p, openedMenus.get(p.getUniqueId()));
  }

  private void cleanup(final Player p, @Nullable final UUID menuUuid) {
    final UUID playerUuid = p.getUniqueId();
    openedMenus.remove(playerUuid);
    openedPages.remove(playerUuid);

    if (menuUuid != null && !openedMenus.containsValue(menuUuid)) {
      menus.remove(menuUuid);
    }
  }
}
