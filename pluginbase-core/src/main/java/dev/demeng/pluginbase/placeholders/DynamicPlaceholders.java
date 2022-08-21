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

package dev.demeng.pluginbase.placeholders;

import dev.demeng.pluginbase.text.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
   * Sets the placeholders in the string.
   *
   * @param str The string to replace
   * @return The replaced string, or an empty string if the provided string is null
   */
  @NotNull
  default String replace(@Nullable final String str) {

    if (str == null) {
      return "";
    }

    return setPlaceholders(str);
  }

  /**
   * Sets the placeholders in a string list.
   *
   * @param list The list to replace
   * @return The replaced list, or an empty list if the provided list is null
   */
  @NotNull
  default List<String> replace(@Nullable final List<String> list) {

    if (list == null) {
      return Collections.emptyList();
    }

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
   * @return The replaced item stack, or air if the provided stack is null
   */
  @NotNull
  default ItemStack replace(@Nullable final ItemStack stack) {

    if (stack == null || stack.getType() == Material.AIR) {
      return new ItemStack(Material.AIR);
    }

    final ItemStack replaced = new ItemStack(stack);
    final ItemMeta meta = replaced.getItemMeta();
    Objects.requireNonNull(meta, "Item meta is null");

    meta.setDisplayName(Text.colorize(setPlaceholders(meta.getDisplayName())));

    if (meta.getLore() != null && !meta.getLore().isEmpty()) {
      meta.setLore(Text.colorize(replace(meta.getLore())));
    }

    replaced.setItemMeta(meta);

    return replaced;
  }

  /**
   * Gets the function for setting the placeholders.
   *
   * @return The replacing function
   */
  @NotNull
  default Function<String, String> toFunction() {
    return this::replace;
  }
}
