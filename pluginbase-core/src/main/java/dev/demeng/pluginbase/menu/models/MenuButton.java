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

package dev.demeng.pluginbase.menu.models;

import dev.demeng.pluginbase.item.ItemBuilder;
import dev.demeng.pluginbase.menu.layouts.Menu;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;

/**
 * Represents a "button", or rather item stack, that will be put inside a {@link Menu}. Each button
 * is assigned with a nullable consumer for {@link InventoryClickEvent}, which will be accepted on
 * click.
 */
@AllArgsConstructor
public class MenuButton {

  /**
   * The slot the button should show up in. Must be less than the inventory size minus 1. Note that
   * inventory slots start at 0. This button will be ignored and not be placed inside the menu if
   * the slot is negative.
   */
  @Getter @Setter public int slot;

  /**
   * The item stack of the button.
   */
  @NotNull @Getter @Setter public ItemStack stack;

  /**
   * The consumer for the {@link InventoryClickEvent}, which will be accepted when this button is
   * clicked.
   */
  @Getter @Setter public Consumer<InventoryClickEvent> consumer;

  /**
   * Creates a new menu button from a configuration section. The slot value must be an integer named
   * {@code slot}. See {@link ItemBuilder#fromConfig(ConfigurationSection)} for the format of item
   * stacks. The slot will always be subtracted by 1.
   *
   * @param section  The configuration section containing the button information
   * @param consumer The consumer for the button
   * @return The button from config
   */
  public static MenuButton fromConfig(final ConfigurationSection section,
      @Nullable final Consumer<InventoryClickEvent> consumer) {
    return new MenuButton(section.getInt("slot", 0) - 1, ItemBuilder.fromConfig(section), consumer);
  }
}