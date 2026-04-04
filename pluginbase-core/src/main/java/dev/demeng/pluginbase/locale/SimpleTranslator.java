/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
 * Copyright (c) 2026 Demeng Chen
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

import dev.demeng.pluginbase.exceptions.BaseException;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

final class SimpleTranslator implements Translator {

  private static final String DEFAULT_RESOURCE_BUNDLE = "pluginbase";
  private static final List<LocaleReader> EMPTY_LIST = Collections.emptyList();
  private static final Logger LOGGER = Logger.getLogger(SimpleTranslator.class.getName());

  private final ConcurrentHashMap<Locale, List<LocaleReader>> registeredBundles =
      new ConcurrentHashMap<>();
  private volatile Locale locale = Locale.ENGLISH;

  SimpleTranslator() {
    loadDefault();
  }

  public void loadDefault() {
    addResourceBundleSilent(getClass().getClassLoader(), DEFAULT_RESOURCE_BUNDLE);
  }

  @Override
  public void addResourceBundleFromFolder(@NotNull final String resourceBundle) {

    final File folder =
        BaseManager.getPlugin().getDataFolder().toPath().resolve("locales").toFile();

    if (!folder.exists()) {
      return;
    }

    final URL[] urls;

    try {
      urls = new URL[] {folder.toURI().toURL()};
    } catch (final MalformedURLException ex) {
      throw new BaseException(
          "Could not load resource bundle from filesystem: " + resourceBundle, ex);
    }

    addResourceBundle(new URLClassLoader(urls), resourceBundle);
  }

  @Override
  public boolean containsKey(@NotNull final String key) {
    for (final LocaleReader registeredBundle :
        registeredBundles.getOrDefault(this.locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsKey(@NotNull final String key, @NotNull final Locale locale) {
    for (final LocaleReader registeredBundle : registeredBundles.getOrDefault(locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @NotNull String get(@NotNull final String key) {
    return get(key, locale);
  }

  @Override
  public @NotNull String get(@NotNull final String key, @NotNull final Locale locale) {
    for (final LocaleReader registeredBundle : registeredBundles.getOrDefault(locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return registeredBundle.get(key);
      }
    }
    for (final LocaleReader registeredBundle :
        registeredBundles.getOrDefault(this.locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return registeredBundle.get(key);
      }
    }
    LOGGER.fine(
        () ->
            "Translation key not found: \""
                + key
                + "\" (requested: "
                + locale
                + ", default: "
                + this.locale
                + ")");
    return key;
  }

  @Override
  public @NotNull String @NotNull [] getArray(@NotNull final String key) {
    return getArray(key, locale);
  }

  @Override
  public @NotNull String @NotNull [] getArray(
      @NotNull final String key, @NotNull final Locale locale) {
    for (final LocaleReader registeredBundle : registeredBundles.getOrDefault(locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return registeredBundle.getArray(key);
      }
    }
    for (final LocaleReader registeredBundle :
        registeredBundles.getOrDefault(this.locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return registeredBundle.getArray(key);
      }
    }
    LOGGER.fine(
        () ->
            "Translation array key not found: \""
                + key
                + "\" (requested: "
                + locale
                + ", default: "
                + this.locale
                + ")");
    return new String[] {key};
  }

  @Override
  public void add(@NotNull final LocaleReader reader) {
    registeredBundles
        .computeIfAbsent(reader.getLocale(), v -> new CopyOnWriteArrayList<>())
        .add(0, reader);
  }

  @Override
  public @NotNull Locale getLocale() {
    return locale;
  }

  @Override
  public void setLocale(@NotNull final Locale locale) {
    this.locale = locale;
  }

  @Override
  public void addResourceBundle(
      @NotNull final ClassLoader loader,
      @NotNull final String resourceBundle,
      @NotNull final Locale... locales) {
    int loaded = 0;
    for (final Locale l : locales) {
      try {
        final ResourceBundle bundle =
            ResourceBundle.getBundle(resourceBundle, l, loader, UTF8Control.INSTANCE);
        add(bundle);
        loaded++;
      } catch (final MissingResourceException ignored) {
      }
    }
    if (loaded == 0 && locales.length > 0) {
      LOGGER.warning(
          () ->
              "No resource bundles found for \""
                  + resourceBundle
                  + "\" across "
                  + locales.length
                  + " locale(s)");
    }
  }

  @Override
  public void addResourceBundle(
      @NotNull final ClassLoader loader, @NotNull final String resourceBundle) {
    int loaded = 0;
    for (final Locale l : Locales.getLocales()) {
      try {
        final ResourceBundle bundle =
            ResourceBundle.getBundle(resourceBundle, l, loader, UTF8Control.INSTANCE);
        add(bundle);
        loaded++;
      } catch (final MissingResourceException ignored) {
      }
    }
    if (loaded == 0) {
      LOGGER.warning(() -> "No resource bundles found for \"" + resourceBundle + "\"");
    }
  }

  @Override
  public void clear() {
    registeredBundles.clear();
  }

  @Override
  public void add(@NotNull final ResourceBundle resourceBundle) {
    add(LocaleReader.wrap(resourceBundle));
  }

  private void addResourceBundleSilent(
      @NotNull final ClassLoader loader, @NotNull final String resourceBundle) {
    for (final Locale l : Locales.getLocales()) {
      try {
        final ResourceBundle bundle =
            ResourceBundle.getBundle(resourceBundle, l, loader, UTF8Control.INSTANCE);
        add(bundle);
      } catch (final MissingResourceException ignored) {
      }
    }
  }
}
