/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
 * Copyright (c) 2024 Demeng Chen
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

final class SimpleTranslator implements Translator {

  private static final String DEFAULT_RESOURCE_BUNDLE = "pluginbase";
  private static final LinkedList<LocaleReader> EMPTY_LIST = new LinkedList<>();

  private final Map<Locale, LinkedList<LocaleReader>> registeredBundles = new HashMap<>();
  private volatile Locale locale = Locale.ENGLISH;

  SimpleTranslator() {
    loadDefault();
  }

  public void loadDefault() {
    addResourceBundle(DEFAULT_RESOURCE_BUNDLE);
  }

  @Override
  public void addResourceBundleFromFolder(@NotNull final String resourceBundle) {

    final File folder = BaseManager.getPlugin().getDataFolder().toPath().resolve("locales")
        .toFile();

    if (!folder.exists()) {
      return;
    }

    final URL[] urls;

    try {
      urls = new URL[]{folder.toURI().toURL()};
    } catch (final MalformedURLException ex) {
      throw new BaseException("Could not load resource bundle from filesystem: " + resourceBundle,
          ex);
    }

    addResourceBundle(new URLClassLoader(urls), resourceBundle);
  }

  @Override public boolean containsKey(@NotNull String key) {
    for (final LocaleReader registeredBundle : registeredBundles.getOrDefault(this.locale,
        EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  @Override public boolean containsKey(@NotNull String key, @NotNull Locale locale) {
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
    for (final LocaleReader registeredBundle : registeredBundles.getOrDefault(this.locale,
        EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return registeredBundle.get(key);
      }
    }
    return key;
  }

  @Override
  public void add(@NotNull final LocaleReader reader) {
    final LinkedList<LocaleReader> list = registeredBundles.computeIfAbsent(reader.getLocale(),
        v -> new LinkedList<>());
    list.push(reader);
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
    for (final Locale l : locales) {
      try {
        final ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, l, loader,
            UTF8Control.INSTANCE);
        add(bundle);
      } catch (final MissingResourceException ignored) {
      }
    }
  }

  @Override
  public void addResourceBundle(@NotNull final ClassLoader loader,
      @NotNull final String resourceBundle) {
    for (final Locale l : Locales.getLocales()) {
      try {
        final ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, l, loader,
            UTF8Control.INSTANCE);
        add(bundle);
      } catch (final MissingResourceException ignored) {
      }
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
}
