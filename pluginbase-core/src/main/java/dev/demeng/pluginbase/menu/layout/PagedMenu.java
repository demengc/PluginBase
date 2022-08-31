/*
 * MIT License
 *
 * Copyright (c) 2022 Demeng Chen
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
import dev.demeng.pluginbase.exceptions.BaseException;
import dev.demeng.pluginbase.menu.IMenu;
import dev.demeng.pluginbase.menu.MenuManager;
import dev.demeng.pluginbase.menu.model.MenuButton;
import dev.demeng.pluginbase.serialize.ItemSerializer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A paginated GUI menu that will be displayed to a player.
 */
public abstract class PagedMenu implements IMenu {

  /**
   * The placeholder that can be used in the inventory title for the current page.
   */
  private static final String CURRENT_PAGE_PLACEHOLDER = "%current-page%";

  /**
   * The unique ID of this menu.
   */
  @NotNull @Getter private final UUID uuid = UUID.randomUUID();

  /**
   * The single-paged menus (pages) related to this paged menu.
   */
  @NotNull @Getter private final List<Menu> pages = new ArrayList<>();

  private final int pageSize;
  private final String title;
  private final Settings settings;

  /**
   * Creates a new paged menu with the specified size per page and inventory title.
   *
   * @param pageSize The size of each page, in slots; must be greater than or equal to 9 and less
   *                 than or equal to 54, and a multiple of 9
   * @param title    The menu title, colorized internally and permits page number placeholders
   * @param settings The settings for the paged menu
   */
  protected PagedMenu(final int pageSize, @NotNull final String title, final Settings settings) {
    this.pageSize = pageSize;
    this.title = title;
    this.settings = settings;
  }

  /**
   * Fills the pages with the list of buttons.
   *
   * @param buttons The buttons that the menu should be filled with
   */
  public void fill(final List<MenuButton> buttons) {

    Page page = new Page(pageSize, title.replace(CURRENT_PAGE_PLACEHOLDER, "" + 1), settings);
    pages.add(page);

    for (final MenuButton button : buttons) {

      // Slot must be -1 for proper pagination.
      if (button.getSlot() != -1) {

        // Set in the specified slot on first page if slot is higher than -1.
        if (button.getSlot() > -1) {
          pages.get(0).addButton(button);
        }

        continue;
      }

      if (page.availableSlots.isEmpty()
          || page.availableSlots.peek() >= page.getInventory().getSize()) {

        page = new Page(pageSize,
            title.replace(CURRENT_PAGE_PLACEHOLDER, String.valueOf(pages.size() + 1)), settings);
        pages.add(page);
      }

      final Integer slot = page.availableSlots.poll();

      if (slot != null) {
        button.setSlot(slot);
      }

      page.addButton(button);
    }

    pages.get(0).addButton(settings.getDummyPreviousButton());
    pages.get(pages.size() - 1).addButton(settings.getDummyNextButton());
  }

  /**
   * Adds a static item that will be displayed on all pages. Must be done AFTER the
   * {@link #fill(List)} method.
   *
   * @param button The button to set on all pages
   */
  protected void addStaticButton(final MenuButton button) {
    for (final Menu page : pages) {
      page.addButton(button);
    }
  }

  /**
   * Creates and adds a static item that will be displayed on all pages. Must be done AFTER the
   * {@link #fill(List)} method.
   *
   * @param slot    The slot of the button
   * @param stack   The stack of the button
   * @param actions The actions of the button
   */
  public void addButton(final int slot, @NotNull final ItemStack stack,
      @Nullable final Consumer<InventoryClickEvent> actions) {
    for (final Menu page : pages) {
      page.addButton(MenuButton.create(slot, stack, actions));
    }
  }

  @Override
  public void open(final Player... players) {
    for (final Player player : players) {
      pages.get(0).open(player);
      MenuManager.getOpenedPagedMenus().put(player.getUniqueId(), uuid);
      MenuManager.getOpenedPages().put(player.getUniqueId(), 0);
    }
  }

  /**
   * Opens a specific page of the menu to the players.
   *
   * @param index   The index of the page (starts at 0)
   * @param players THe players to open the page to
   */
  public void open(final int index, final Player... players) {
    for (final Player player : players) {
      pages.get(index).open(player);
      MenuManager.getOpenedPagedMenus().put(player.getUniqueId(), uuid);
      MenuManager.getOpenedPages().put(player.getUniqueId(), index);
    }
  }

  private class Page extends Menu {

    private final Queue<Integer> availableSlots;

    public Page(final int pageSize, final String title, final Settings settings) {
      super(pageSize, title);

      super.addButton(
          MenuButton.create(
              settings.getPreviousButton().getSlot(),
              settings.getPreviousButton().getStack(),
              event -> {
                final Player p = (Player) event.getWhoClicked();
                final int playerCurrentPage = MenuManager.getOpenedPages().get(p.getUniqueId());
                if (playerCurrentPage - 1 >= 0 && pages.get(playerCurrentPage - 1) != null) {
                  PagedMenu.this.open(playerCurrentPage - 1, p);
                }
              }));

      super.addButton(
          MenuButton.create(
              settings.getNextButton().getSlot(),
              settings.getNextButton().getStack(),
              event -> {
                final Player p = (Player) event.getWhoClicked();
                final int playerCurrentPage = MenuManager.getOpenedPages().get(p.getUniqueId());
                if (playerCurrentPage + 1 < pages.size()
                    && pages.get(playerCurrentPage + 1) != null) {
                  PagedMenu.this.open(playerCurrentPage + 1, p);
                }
              }));

      this.availableSlots = settings.getAvailableSlots().stream()
          .filter(Objects::nonNull)
          .sorted()
          .collect(Collectors.toCollection(LinkedList::new));

      if (availableSlots.isEmpty()) {
        throw new BaseException("Available slots cannot be empty");
      }
    }

    @Override
    public boolean onClose(@NotNull final InventoryCloseEvent event) {
      return PagedMenu.this.onClose(event);
    }
  }

  /**
   * Settings for the paged menu.
   */
  public interface Settings {

    /**
     * The previous page button. The provided consumer will be ignored.
     *
     * @return The previous page button
     */
    @NotNull MenuButton getPreviousButton();

    /**
     * The previous page button if there are no more pages to go back to. The provided consumer will
     * be ignored.
     *
     * @return The dummy previous page button
     */
    @NotNull MenuButton getDummyPreviousButton();

    /**
     * The next page button. The provided consumer will be ignored.
     *
     * @return The next page button
     */
    @NotNull MenuButton getNextButton();

    /**
     * The next page button if there are no more pages to go to. The provided consumer will be
     * ignored.
     *
     * @return The dummy next page button
     */
    @NotNull MenuButton getDummyNextButton();

    /**
     * The list of slots that buttons can use. If none of the slots in this list are empty, a new
     * page will be created. Otherwise, the first available slot will be used. For a simple range of
     * integers, use {@link IntStream#range(int, int)}.
     *
     * @return The list of slots that buttons can use
     */
    @NotNull List<Integer> getAvailableSlots();

    /**
     * Gets the paged menu settings from a configuration section.
     *
     * @param section The configuration section to get the settings from
     * @return The paged settings as defined in the configuration section
     */
    @NotNull
    static Settings fromConfig(@NotNull final ConfigurationSection section) {
      return new Settings() {
        @Override
        public @NotNull MenuButton getPreviousButton() {
          return MenuButton.create(Objects.requireNonNull(
              section.getConfigurationSection("previous-page"),
              "'Previous page' button is null"), null);
        }

        @Override
        public @NotNull MenuButton getDummyPreviousButton() {
          return MenuButton.create(getPreviousButton().getSlot(),
              ItemSerializer.deserialize(Objects.requireNonNull(
                  section.getConfigurationSection("previous-page.no-more-pages"),
                  "'No more previous pages' button is null")), null);
        }

        @Override
        public @NotNull MenuButton getNextButton() {
          return MenuButton.create(Objects.requireNonNull(
              section.getConfigurationSection("next-page"),
              "'Next page' button is null"), null);
        }

        @Override
        public @NotNull MenuButton getDummyNextButton() {
          return MenuButton.create(getNextButton().getSlot(),
              ItemSerializer.deserialize(Objects.requireNonNull(
                  section.getConfigurationSection("next-page.no-more-pages"),
                  "'No more next pages' button is null")), null);
        }

        @Override
        public @NotNull List<Integer> getAvailableSlots() {

          if (section.isConfigurationSection("listing-range")) {
            // Old config format support.
            return IntStream.range(
                    section.getInt("listing-range.start") - 1,
                    section.getInt("listing-range.end") - 1)
                .boxed().collect(Collectors.toList());
          }

          final List<Integer> available = new ArrayList<>();

          try {
            Common.forEachInt(section.getString("available-slots"),
                slot -> available.add(slot - 1));
          } catch (final IllegalArgumentException ex) {
            Common.error(ex, "Failed to parse listing range.", false);
          }

          return available;
        }
      };
    }
  }
}
