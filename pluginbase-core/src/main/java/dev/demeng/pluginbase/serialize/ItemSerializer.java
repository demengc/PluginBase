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

package dev.demeng.pluginbase.serialize;

import com.cryptomorin.xseries.XItemStack;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.plugin.BaseManager;
import dev.demeng.pluginbase.text.Text;
import java.util.Locale;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The YAML serializer for Bukkit {@link ItemStack}s.
 *
 * @see XItemStack
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ItemSerializer {

  /**
   * Serializes the item stack into the configuration section. The configuration file is not saved
   * after serialization.
   *
   * @param section The section the item stack will be serialized into
   */
  public static void serialize(
      @NotNull final ItemStack stack,
      @NotNull final ConfigurationSection section) {
    XItemStack.serialize(stack, section);
  }

  /**
   * Deserializes the configuration section into an item stack.
   *
   * @param section     The configuration section to deserialize
   * @param transformer The transformer for strings in the item
   * @return The deserialized item stack
   */
  @NotNull
  public static ItemStack deserialize(
      @NotNull final ConfigurationSection section,
      @Nullable final UnaryOperator<String> transformer) {
    return XItemStack.deserialize(section,
        str -> Text.colorize(Common.applyOperator(str, transformer)));
  }

  /**
   * Deserializes the configuration section into an item stack.
   *
   * @param section The configuration section to deserialize
   * @return The deserialized item stack
   */
  @NotNull
  public static ItemStack deserialize(@NotNull final ConfigurationSection section) {
    return XItemStack.deserialize(section, Text::colorize);
  }

  /**
   * Deserializes the configuration section into an item stack with localization.
   *
   * @param section     The configuration section to deserialize
   * @param locale      The locale to use for localization
   * @param transformer The transformer for strings in the item
   * @return The deserialized item stack
   */
  @NotNull
  public static ItemStack deserializeLocalized(
      @NotNull final ConfigurationSection section,
      @Nullable final Locale locale,
      @Nullable final UnaryOperator<String> transformer) {

    if (locale == null) {
      return deserializeLocalized(section, BaseManager.getTranslator().getLocale(), transformer);
    }

    return XItemStack.deserialize(section,
        str -> Text.colorize(
            Common.applyOperator(Text.localizePlaceholders(str, locale), transformer)));
  }

  @NotNull
  public static ItemStack deserializeLocalized(
      @NotNull final ConfigurationSection section,
      @Nullable final Locale locale) {
    return deserializeLocalized(section, locale, Text::colorize);
  }

  @NotNull
  public static ItemStack deserializeLocalized(
      @NotNull final ConfigurationSection section,
      @NotNull final CommandSender sender,
      @Nullable final UnaryOperator<String> transformer) {
    return deserializeLocalized(section, Text.getLocale(sender), transformer);
  }

  @NotNull
  public static ItemStack deserializeLocalized(
      @NotNull final ConfigurationSection section,
      @NotNull final CommandSender sender) {
    return deserializeLocalized(section, Text.getLocale(sender), Text::colorize);
  }

  @NotNull
  public static ItemStack deserializeLocalizedDef(
      @NotNull final ConfigurationSection section,
      @Nullable final UnaryOperator<String> transformer) {
    return deserializeLocalized(section, (Locale) null, transformer);
  }

  @NotNull
  public static ItemStack deserializeLocalizedDef(@NotNull final ConfigurationSection section) {
    return deserializeLocalized(section, (Locale) null, Text::colorize);
  }
}
