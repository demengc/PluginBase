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

import dev.demeng.pluginbase.menu.IMenu;
import dev.demeng.pluginbase.menu.MenuManager;
import dev.demeng.pluginbase.menu.model.MenuButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
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

    int currentSlot = settings.getStartingSlot();

    Menu page = new Page(pageSize, title.replace(CURRENT_PAGE_PLACEHOLDER, "" + 1), settings);
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

      if (currentSlot > settings.getEndingSlot()
          || page.getInventory().firstEmpty() > settings.getEndingSlot()
          || page.getInventory().firstEmpty() == -1) {

        page = new Page(pageSize,
            title.replace(CURRENT_PAGE_PLACEHOLDER, String.valueOf(pages.size() + 1)), settings);
        pages.add(page);

        currentSlot = settings.getStartingSlot();
      }

      button.setSlot(currentSlot);
      page.addButton(button);
      currentSlot++;
    }

    pages.get(0).addButton(settings.getDummyPreviousButton());
    pages.get(pages.size() - 1).addButton(settings.getDummyNextButton());
  }

  /**
   * Adds a static item that will be displayed on all pages. Must be done AFTER the {@link
   * #fill(List)} method.
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
      page.addButton(new MenuButton(slot, stack, actions));
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

    public Page(final int pageSize, final String title, final Settings settings) {
      super(pageSize, title);

      super.addButton(
          new MenuButton(
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
          new MenuButton(
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
     * The slot of which the contents will start being listed.
     *
     * @return The starting slot for the buttons
     */
    int getStartingSlot();

    /**
     * The slot of which the contents will stop being listed.
     *
     * @return The ending slot for the buttons
     */
    int getEndingSlot();

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
          return MenuButton
              .fromConfig(Objects.requireNonNull(section.getConfigurationSection("previous-page")),
                  null);
        }

        @Override
        public @NotNull MenuButton getDummyPreviousButton() {
          return MenuButton.fromConfig(getPreviousButton().getSlot(), Objects.requireNonNull(
              section.getConfigurationSection("previous-page.no-more-pages")), null);
        }

        @Override
        public @NotNull MenuButton getNextButton() {
          return MenuButton
              .fromConfig(Objects.requireNonNull(section.getConfigurationSection("next-page")),
                  null);
        }

        @Override
        public @NotNull MenuButton getDummyNextButton() {
          return MenuButton.fromConfig(getNextButton().getSlot(), Objects.requireNonNull(
              section.getConfigurationSection("next-page.no-more-pages")), null);
        }

        @Override
        public int getStartingSlot() {
          return section.getInt("listing-range.start") - 1;
        }

        @Override
        public int getEndingSlot() {
          return section.getInt("listing-range.end") - 1;
        }
      };
    }
  }
}
