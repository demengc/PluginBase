/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

package dev.demeng.pluginbase;

import dev.demeng.pluginbase.chat.ChatUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * An easy way to provides methods for setting placeholders in lists and item stacks, simply by
 * defining how to set the placeholders in a string.
 */
public interface DynamicPlaceholders {

  /**
   * Sets the placeholders in the string.
   *
   * @param str The string to replace
   * @return The replaced string
   */
  @NotNull
  String setPlaceholders(@NotNull String str);

  /**
   * Sets the placeholders in a string list.
   *
   * @param list The list to replace
   * @return The replaced list
   */
  @NotNull
  default List<String> setPlaceholders(@NotNull final List<String> list) {

    final List<String> replaced = new ArrayList<>();

    for (final String str : list) {
      replaced.add(setPlaceholders(str));
    }

    return replaced;
  }

  /**
   * Sets the placeholders in an item stack. The placeholders will only be applied to the display
   * name and lore.
   *
   * @param stack The item stack to replace
   * @return The replaced item stack
   */
  @NotNull
  default ItemStack setPlaceholders(@NotNull final ItemStack stack) {

    final ItemStack replaced = new ItemStack(stack);
    final ItemMeta meta = replaced.getItemMeta();
    Objects.requireNonNull(meta, "Item meta is null");

    meta.setDisplayName(ChatUtils.colorize(setPlaceholders(meta.getDisplayName())));

    if (meta.getLore() != null && !meta.getLore().isEmpty()) {
      meta.setLore(ChatUtils.colorize(setPlaceholders(meta.getLore())));
    }

    replaced.setItemMeta(meta);

    return replaced;
  }
}
