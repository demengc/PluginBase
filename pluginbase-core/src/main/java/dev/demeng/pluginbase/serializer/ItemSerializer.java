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

package dev.demeng.pluginbase.serializer;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.placeholders.DynamicPlaceholders;
import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.item.ItemBuilder;
import dev.demeng.pluginbase.serializer.type.YamlSerializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The serializer for {@link org.bukkit.inventory.ItemStack}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemSerializer implements YamlSerializable<ItemStack> {

  private static final ItemSerializer NOOP = new ItemSerializer();

  /**
   * Gets the singular instance of this serializer.
   *
   * @return The serializer instance
   */
  public static ItemSerializer get() {
    return NOOP;
  }

  @Override
  public void serialize(
      @NotNull final ItemStack obj,
      @NotNull final YamlConfig configFile,
      @NotNull final String path) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public ItemStack deserialize(@NotNull final ConfigurationSection section) {
    return deserialize(section, null);
  }

  /**
   * Gets an item stack from a configuration section and sets applicable placeholders to string
   * values such as material names, display names, and lores.
   *
   * @param section      The section to deserialize
   * @param placeholders The placeholders to set
   * @return The item stack deserialized from the configuration section
   */
  @NotNull
  public ItemStack deserialize(
      @NotNull final ConfigurationSection section,
      @Nullable final DynamicPlaceholders placeholders) {

    final String configMaterial = section.getString("material");

    if (configMaterial == null) {
      return new ItemStack(Material.AIR);
    }

    final ItemBuilder builder = ItemBuilder.create(
        ItemBuilder.getMaterial(replace(placeholders, configMaterial)));

    final String texture = section.getString("skull-texture");

    if (texture != null) {
      builder.skullTexture(texture);
    }

    builder.amount(section.getInt("amount", 1));

    final String configName = section.getString("display-name");

    if (configName != null) {
      builder.name(replace(placeholders, configName));
    }

    final List<String> lore = new ArrayList<>();

    for (final String line : section.getStringList("lore")) {
      lore.add(replace(placeholders, line));
    }

    builder.lore(lore);

    final int configModelData = section.getInt("model-data", -1);

    if (configModelData >= 0) {
      builder.modelData(configModelData);
    }

    final boolean configGlow = section.getBoolean("glow");

    if (configGlow) {
      builder.glow(true);
    }

    final ConfigurationSection enchantmentsSection = section
        .getConfigurationSection("enchantments");

    if (enchantmentsSection != null) {
      for (final String strEnchantment : enchantmentsSection.getKeys(false)) {
        final Enchantment enchantment = parseEnchantment(strEnchantment);

        if (enchantment == null) {
          Common.error(null, "Invalid enchantment: " + strEnchantment, false);
          continue;
        }

        builder.enchant(enchantment, enchantmentsSection.getInt(strEnchantment, 1), false);
      }
    }

    return builder.get();
  }

  private Enchantment parseEnchantment(final String str) {

    final Enchantment enchantment;

    try {
      enchantment = Enchantment.getByKey(NamespacedKey.minecraft(str.toLowerCase()));
    } catch (final IllegalArgumentException ex) {
      return null;
    }

    return enchantment;
  }

  private String replace(final DynamicPlaceholders placeholders, final String str) {

    if (str == null) {
      return null;
    }

    return placeholders == null ? str : placeholders.setPlaceholders(str);
  }
}
