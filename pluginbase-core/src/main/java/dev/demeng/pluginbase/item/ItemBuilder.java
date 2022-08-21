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

package dev.demeng.pluginbase.item;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.text.Text;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility for quickly creating {@link ItemStack}s.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemBuilder {

  private static final Material DEFAULT_MATERIAL = Material.STONE;

  /**
   * The current item stack.
   */
  @NotNull private final ItemStack stack;

  // ---------------------------------------------------------------------------------
  // STATIC FACTORY
  // ---------------------------------------------------------------------------------

  /**
   * Creates a new builder from an existing item stack.
   *
   * @param stack The item stack to clone, replaced with STONE item stack if null
   * @return The builder
   */
  @NotNull
  public static ItemBuilder create(@Nullable final ItemStack stack) {
    return new ItemBuilder(stack == null ? new ItemStack(DEFAULT_MATERIAL) : stack.clone());
  }

  /**
   * Creates a new builder from a material, amount, and damage.
   *
   * @param material The material of the stack, replaced with STONE if null
   * @param amount   The amount of the stack
   * @param damage   The damage (a.k.a. the durability) of the stack
   * @return The builder
   */
  @NotNull
  public static ItemBuilder create(
      @Nullable final Material material,
      final int amount,
      final short damage) {
    //noinspection deprecation
    return create(new ItemStack(Common.getOrDefault(material, DEFAULT_MATERIAL), amount, damage));
  }

  /**
   * Creates a new builder from a material and an amount.
   *
   * @param material The material of the stack, replaced with STONE if null
   * @param amount   The amount of the stack
   * @return The builder
   */
  @NotNull
  public static ItemBuilder create(@Nullable final Material material, final int amount) {
    return create(material, amount, (short) 0);
  }

  /**
   * Creates a new builder from a simple material.
   *
   * @param material The material of the stack, replaced with STONE if null
   * @return The builder
   */
  @NotNull
  public static ItemBuilder create(@Nullable final Material material) {
    return create(material, 1);
  }

  // ---------------------------------------------------------------------------------
  // GENERAL OPTIONS
  // ---------------------------------------------------------------------------------

  /**
   * Changes the amount of the item.
   *
   * @param amount The new amount
   * @return this
   */
  public ItemBuilder amount(final int amount) {
    stack.setAmount(amount);
    return this;
  }

  /**
   * Changes the durability of the item.
   *
   * @param durability The new durability
   * @return this
   */
  public ItemBuilder durability(final short durability) {
    //noinspection deprecation
    stack.setDurability(durability);
    return this;
  }

  /**
   * Sets the display name of the item.
   *
   * @param name The new display name, colorized internally
   * @return this
   */
  public ItemBuilder name(@NotNull final String name) {
    updateMeta(meta -> meta.setDisplayName(Text.colorize(name)));
    return this;
  }

  /**
   * Adds an enchantment with customizable safety.
   *
   * @param enchant The enchantment to add
   * @param level   The level of the enchantment
   * @param safe    If the enchantment should be safe
   * @return this
   */
  public ItemBuilder enchant(@NotNull final Enchantment enchant, final int level,
      final boolean safe) {
    updateMeta(meta -> meta.addEnchant(enchant, level, !safe));
    return this;
  }

  /**
   * Adds a safe enchantment.
   *
   * @param enchant The enchantment to add
   * @param level   The level of the enchantment
   * @return this
   */
  public ItemBuilder enchant(@NotNull final Enchantment enchant, final int level) {
    return enchant(enchant, level, true);
  }

  /**
   * Adds all the enchants specified in the map, with the key being the enchantment and the value
   * being the level.
   *
   * @param enchants The enchants to add
   * @return this
   */
  public ItemBuilder enchant(@NotNull final Map<Enchantment, Integer> enchants) {
    stack.addEnchantments(enchants);
    return this;
  }

  /**
   * Removes the specified enchantment.
   *
   * @param enchant The enchantment to remove
   * @return this
   */
  public ItemBuilder unenchant(@NotNull final Enchantment enchant) {
    updateMeta(meta -> meta.removeEnchant(enchant));
    return this;
  }

  /**
   * Clears any enchantments that have been applied.
   *
   * @return this
   */
  public ItemBuilder clearEnchants() {

    updateMeta(meta -> {
      for (final Enchantment enchant : meta.getEnchants().keySet()) {
        meta.removeEnchant(enchant);
      }
    });

    return this;
  }

  /**
   * Sets the lore to the specified list.
   *
   * @param lore The lore lines, colorized internally
   * @return this
   */
  public ItemBuilder lore(final List<String> lore) {
    updateMeta(meta -> meta.setLore(Text.colorize(lore)));
    return this;
  }

  /**
   * Sets the lore to the specified string(s). Each string represents a new line.
   *
   * @param lore The lore line(s), colorized internally
   * @return this
   */
  public ItemBuilder lore(@NotNull final String... lore) {
    return lore(Arrays.asList(lore));
  }

  /**
   * Adds a single line of lore on top of the current lore.
   *
   * @param line The lore line to add, colorized internally
   * @return this
   */
  public ItemBuilder addLore(@NotNull final String line) {

    updateMeta(meta -> {
      final List<String> lore = new ArrayList<>(
          Common.getOrDefault(meta.getLore(), Collections.emptyList()));
      lore.add(Text.colorize(line));
      meta.setLore(lore);
    });

    return this;
  }

  /**
   * Clears the item lore.
   *
   * @return this
   */
  public ItemBuilder clearLore() {
    updateMeta(meta -> meta.setLore(Collections.emptyList()));
    return this;
  }

  /**
   * Sets if the item should be unbreakable.
   *
   * @param unbreakable If the item should be unbreakable
   * @return this
   */
  public ItemBuilder unbreakable(final boolean unbreakable) {
    updateMeta(meta -> meta.setUnbreakable(unbreakable));
    return this;
  }

  /**
   * Sets the item flags.
   *
   * @param flags The flags to set
   * @return this
   */
  public ItemBuilder flags(@NotNull final ItemFlag... flags) {
    updateMeta(meta -> meta.addItemFlags(flags));
    return this;
  }

  /**
   * Clears all current item flags.
   *
   * @return this
   */
  public ItemBuilder clearFlags() {

    updateMeta(meta -> {
      for (final ItemFlag flag : meta.getItemFlags()) {
        meta.removeItemFlags(flag);
      }
    });

    return this;
  }

  /**
   * Sets if the item should be given a durability enchantment to make it appear glowing and add a
   * hide enchantment item flag. Setting this to true will also hide all of your other enchantments
   * in the lore. This should only be enabled for GUI aesthetic purposes.
   *
   * <p><b>Note:</b> When the glow is applied, it should be permanent. It is not recommended that
   * you call this method again to remove the glow since it can effect enchantments and item flags
   * from other sources.
   *
   * @param glow If the item should have a glowing effect applied
   * @return this
   */
  public ItemBuilder glow(final boolean glow) {

    if (glow) {
      enchant(Enchantment.DURABILITY, 1);
      flags(ItemFlag.HIDE_ENCHANTS);

    } else {
      stack.removeEnchantment(Enchantment.DURABILITY);
      updateMeta(meta -> meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS));
    }

    return this;
  }

  /**
   * Sets the custom model data of the item, which is only supported in 1.14+. If the server version
   * does not support custom model data, this option is simply ignored.
   *
   * @param modelData The custom model data
   * @return this
   */
  public ItemBuilder modelData(@Nullable final Integer modelData) {

    if (Common.isServerVersionAtLeast(14)) {
      updateMeta(meta -> meta.setCustomModelData(modelData));
    }

    return this;
  }

  // ---------------------------------------------------------------------------------
  // ITEM-SPECIFIC OPTIONS
  // ---------------------------------------------------------------------------------

  /**
   * Sets the skull owner of a skull item. Ignored if the item type is not a player skull.
   *
   * @param owner The username of the skull owner
   * @return this
   */
  public ItemBuilder skullOwner(@NotNull final String owner) {

    try {
      final SkullMeta meta = Objects.requireNonNull((SkullMeta) stack.getItemMeta());
      //noinspection deprecation
      meta.setOwner(owner);
      stack.setItemMeta(meta);

    } catch (final ClassCastException ignored) {
      // Ignore if not skull.
    }

    return this;
  }

  /**
   * Sets the skull owner of a skull item. Ignored if the item type is not a player skull.
   *
   * @param owner The UUId of the skull owner
   * @return this
   */
  public ItemBuilder skullOwner(@NotNull final UUID owner) {

    try {
      final SkullMeta meta = Objects.requireNonNull((SkullMeta) stack.getItemMeta());
      final OfflinePlayer p = Bukkit.getOfflinePlayer(owner);

      if (Common.isServerVersionAtLeast(12)) {
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));

      } else {
        if (p.getName() != null) {
          return skullOwner(p.getName());
        }
      }

      stack.setItemMeta(meta);

    } catch (final ClassCastException ignored) {
      // Ignore if not skull.
    }

    return this;
  }

  /**
   * Sets the skull texture to the provided base64 encoded string. Ignored if the item type is not a
   * player skull.
   *
   * @param texture The base64 encoded texture
   * @return this
   */
  public ItemBuilder skullTexture(@NotNull final String texture) {

    try {
      final SkullMeta skullMeta = Objects.requireNonNull((SkullMeta) stack.getItemMeta());
      final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
      profile.getProperties().put("textures", new Property("textures", texture));

      try {
        final Field profileField = skullMeta.getClass().getDeclaredField("profile");
        profileField.setAccessible(true);
        profileField.set(skullMeta, profile);
      } catch (final NoSuchFieldException | IllegalAccessException ex) {
        ex.printStackTrace();
      }

      stack.setItemMeta(skullMeta);

    } catch (final ClassCastException ignored) {
      // Ignore if not skull.
    }

    return this;
  }

  /**
   * Sets the color of leather armor. Ignored if the item type is not leather armor.
   *
   * @param color The color to change the armor to
   * @return this
   */
  public ItemBuilder armorColor(@Nullable final Color color) {

    try {
      final LeatherArmorMeta meta = Objects.requireNonNull((LeatherArmorMeta) stack.getItemMeta());
      meta.setColor(color);
      stack.setItemMeta(meta);
    } catch (final ClassCastException ignored) {
      // Ignore if not leather armor.
    }

    return this;
  }

  // ---------------------------------------------------------------------------------
  // UTILITIES
  // ---------------------------------------------------------------------------------

  /**
   * Gets the current item stack.
   *
   * @return The current item stack
   */
  @NotNull
  public ItemStack get() {
    return stack;
  }

  /**
   * Clones this item builder.
   *
   * @return The cloned instance
   */
  @NotNull
  public ItemBuilder copy() {
    return new ItemBuilder(stack);
  }

  private void updateMeta(final Consumer<ItemMeta> consumer) {
    final ItemMeta meta = stack.getItemMeta();
    consumer.accept(meta);
    stack.setItemMeta(meta);
  }

  // ---------------------------------------------------------------------------------
  // DESERIALIZATION
  // ---------------------------------------------------------------------------------

  /**
   * Gets a material from a string. Note that this actually returns an item stack as a data values
   * may need to be included on legacy versions.
   *
   * @param strMaterial The string material to parse
   * @return A standard item stack with no meta, or null if the material is invalid or unsupported
   */
  @NotNull
  public static Optional<ItemStack> getMaterialSafe(@Nullable final String strMaterial) {

    if (strMaterial == null) {
      return Optional.empty();
    }

    final Optional<XMaterial> matchOptional = XMaterial.matchXMaterial(strMaterial);
    if (!matchOptional.isPresent()) {
      return Optional.empty();
    }

    final XMaterial match = matchOptional.get();

    if (!match.isSupported()) {
      return Optional.empty();
    }

    return Optional.ofNullable(match.parseItem());
  }

  /**
   * Gets a material from a string. Throws an error if the material is invalid or unsupported,
   * unlike {@link #getMaterialSafe(String)}.
   *
   * @param strMaterial The string material to parse
   * @return A standard item stack with no meta, or stone if invalid/unsupported
   * @see #getMaterialSafe(String)
   */
  @NotNull
  public static ItemStack getMaterial(@Nullable final String strMaterial) {

    if (strMaterial == null) {
      Common.error(null, "Invalid material: null", false);
      return new ItemStack(Material.STONE);
    }

    final ItemStack stack = getMaterialSafe(strMaterial).orElse(null);

    if (stack == null) {
      Common.error(null, "Invalid material: " + strMaterial, false);
      return new ItemStack(Material.STONE);
    }

    return stack;
  }

  /**
   * Attempts to get a material from a string, but returns the default item stack if the result of
   * the provided material string is invalid or unsupported, instead of throwing an error and just
   * returning stone like {@link #getMaterial(String)}.
   *
   * @param strMaterial The string material to parse
   * @param def         The default material if the provided material is invalid or unsupported
   * @return A standard item stack with no meta, or the default stack if invalid/unsupported
   * @see #getMaterial(String)
   */
  @NotNull
  public static ItemStack getMaterialOrDef(
      @Nullable final String strMaterial,
      @NotNull final ItemStack def) {

    if (strMaterial == null) {
      return def;
    }

    final ItemStack stack = getMaterialSafe(strMaterial).orElse(null);

    if (stack == null) {
      return def;
    }

    return stack;
  }
}
