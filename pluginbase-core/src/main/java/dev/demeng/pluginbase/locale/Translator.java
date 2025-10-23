/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
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
package dev.demeng.pluginbase.locale;

import java.util.Locale;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a translator. A translator allows localizing messages, adding bundles, registering
 * custom locales, and changing the default locale.
 */
public interface Translator {

  /**
   * Creates a new {@link Translator}
   *
   * @return The newly created translator
   */
  static @NotNull Translator create() {
    return new SimpleTranslator();
  }

  /**
   * Returns whether this translator contains a localized message for the given key, using the
   * current {@link #getLocale()}.
   *
   * @param key Key to check for
   * @return {@code true} if this translator has a localized message for the key
   */
  boolean containsKey(@NotNull String key);

  /**
   * Returns whether this translator contains a localized message for the given key.
   *
   * @param key Key to check for
   * @param locale Locale to check in
   * @return {@code true} if this translator has a localized message for the key
   */
  boolean containsKey(@NotNull String key, @NotNull Locale locale);

  /**
   * Returns the message that corresponds to the given key, using the current {@link #getLocale()}.
   * If no such message is found, the key will be returned.
   *
   * @param key Message key to fetch with
   * @return The translated message, or the key if not found.
   */
  @NotNull
  String get(@NotNull String key);

  /**
   * Returns the message that corresponds to the given key, using the given locale. If no such
   * message is found, the key will be returned.
   *
   * @param key Message key to fetch with
   * @param locale Locale to get with
   * @return The translated message, or the key if not found.
   */
  @NotNull
  String get(@NotNull String key, @NotNull Locale locale);

  /**
   * Adds the given locale reader to this translator.
   *
   * @param reader The locale reader to add
   */
  void add(@NotNull LocaleReader reader);

  /**
   * Registers the given resource bundle
   *
   * @param resourceBundle Resource bundle to register
   */
  void add(@NotNull ResourceBundle resourceBundle);

  /**
   * Adds the given resource bundle from "/locales" in the plugin data folder. This will only
   * register for the given {@link Locale}s.
   *
   * <p>For example, if you have the following files:
   *
   * <ul>
   *   <li>foo_en_US.properties
   *   <li>foo_fr.properties
   *   <li>foo_de.properties
   * </ul>
   *
   * The resource bundle will be "foo", and locales will be each {@link Locales#ENGLISH}, {@link
   * Locales#FRENCH} and {@link Locales#GERMAN}.
   *
   * @param resourceBundle Resource bundle to register
   */
  void addResourceBundleFromFolder(@NotNull final String resourceBundle);

  /**
   * Adds the given resource bundle. This will only register for the given {@link Locale}s.
   *
   * <p>For example, if you have the following files:
   *
   * <ul>
   *   <li>foo_en_US.properties
   *   <li>foo_fr.properties
   *   <li>foo_de.properties
   * </ul>
   *
   * The resource bundle will be "foo", and locales will be each {@link Locales#ENGLISH}, {@link
   * Locales#FRENCH} and {@link Locales#GERMAN}.
   *
   * @param loader Class loader for the bundle
   * @param resourceBundle Resource bundle to register
   * @param locales Locales to register for
   */
  void addResourceBundle(
      @NotNull ClassLoader loader, @NotNull String resourceBundle, @NotNull Locale... locales);

  /**
   * Adds the given resource bundle. This will automatically check for all locales in {@link
   * Locales}.
   *
   * <p>For example, if you have the following files:
   *
   * <ul>
   *   <li>foo_en_US.properties
   *   <li>foo_fr.properties
   *   <li>foo_de.properties
   * </ul>
   *
   * The resource bundle will be "foo", and locales will be each {@link Locales#ENGLISH}, {@link
   * Locales#FRENCH} and {@link Locales#GERMAN}.
   *
   * @param loader Class loader for the bundle
   * @param resourceBundle Resource bundle to register
   */
  void addResourceBundle(@NotNull ClassLoader loader, @NotNull String resourceBundle);

  /** Clears all currently registered locales. */
  void clear();

  /**
   * Gets the current, default locale used by this translator
   *
   * @return The default locale
   */
  @NotNull
  Locale getLocale();

  /**
   * Sets the locale of this translator.
   *
   * @param locale The locale of this translator
   */
  void setLocale(@NotNull Locale locale);

  /**
   * Adds the given resource bundle from the current class loader.
   *
   * @param resourceBundle Resource bundle to register
   * @see #addResourceBundle(ClassLoader, String, Locale...)
   */
  default void addResourceBundle(
      @NotNull final String resourceBundle, @NotNull final Locale... locales) {
    addResourceBundle(getClass().getClassLoader(), resourceBundle, locales);
  }

  /**
   * Adds the given resource bundle from the current class loader.
   *
   * @param resourceBundle Resource bundle to register
   * @see #addResourceBundle(ClassLoader, String)
   */
  default void addResourceBundle(@NotNull final String resourceBundle) {
    addResourceBundle(getClass().getClassLoader(), resourceBundle);
  }
}
