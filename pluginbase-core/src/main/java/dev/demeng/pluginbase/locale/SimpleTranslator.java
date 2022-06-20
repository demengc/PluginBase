/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
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
package dev.demeng.pluginbase.locale;

import static dev.demeng.pluginbase.commands.util.Preconditions.notNull;

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

  private static final String LOCALE_PREFIX = "locale";
  private static final String LOCALES_FOLDER = "locales";
  private static final LinkedList<LocaleReader> EMPTY_LIST = new LinkedList<>();

  private final Map<Locale, LinkedList<LocaleReader>> registeredBundles = new HashMap<>();
  private volatile Locale locale = Locale.ENGLISH;

  SimpleTranslator() {
    reload();
  }

  public void addResourceBundleFromFilesystem(@NotNull String resourceBundle) {
    notNull(resourceBundle, "resource bundle");

    final File folder = BaseManager.getPlugin().getDataFolder().toPath().resolve(LOCALES_FOLDER)
        .toFile();

    if (!folder.exists()) {
      return;
    }

    final URL[] urls;

    try {
      urls = new URL[]{folder.toURI().toURL()};
    } catch (MalformedURLException ex) {
      throw new BaseException("Could not load resource bundle from filesystem: " + resourceBundle,
          ex);
    }

    addResourceBundle(new URLClassLoader(urls), resourceBundle);
  }

  @Override
  public @NotNull String get(@NotNull String key) {
    return get(key, locale);
  }

  @Override
  public @NotNull String get(@NotNull String key, @NotNull Locale locale) {
    notNull(key, "key");
    notNull(locale, "locale");
    for (LocaleReader registeredBundle : registeredBundles.getOrDefault(locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return registeredBundle.get(key);
      }
    }
    for (LocaleReader registeredBundle : registeredBundles.getOrDefault(this.locale, EMPTY_LIST)) {
      if (registeredBundle.containsKey(key)) {
        return registeredBundle.get(key);
      }
    }
    return key;
  }

  @Override
  public void add(@NotNull LocaleReader reader) {
    LinkedList<LocaleReader> list = registeredBundles.computeIfAbsent(reader.getLocale(),
        v -> new LinkedList<>());
    list.push(reader);
  }

  @Override
  public @NotNull Locale getLocale() {
    return locale;
  }

  @Override
  public void setLocale(@NotNull Locale locale) {
    notNull(locale, "locale");
    this.locale = locale;
  }

  @Override
  public void addResourceBundle(
      @NotNull ClassLoader loader,
      @NotNull String resourceBundle,
      @NotNull Locale... locales) {
    notNull(loader, "loader");
    notNull(resourceBundle, "resource bundle");
    notNull(locales, "locales");
    for (Locale l : locales) {
      try {
        ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, l, loader,
            UTF8Control.INSTANCE);
        add(bundle);
      } catch (MissingResourceException ignored) {
      }
    }
  }

  @Override
  public void addResourceBundle(@NotNull ClassLoader loader, @NotNull String resourceBundle) {
    notNull(loader, "loader");
    notNull(resourceBundle, "resource bundle");
    for (Locale l : Locales.getLocales()) {
      try {
        ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, l, loader,
            UTF8Control.INSTANCE);
        add(bundle);
      } catch (MissingResourceException ignored) {
      }
    }
  }

  @Override
  public void clear() {
    registeredBundles.clear();
  }

  @Override
  public void reload() {
    clear();
    addResourceBundle(LOCALE_PREFIX);
    addResourceBundleFromFilesystem(LOCALE_PREFIX);
  }

  @Override
  public void add(@NotNull ResourceBundle resourceBundle) {
    notNull(resourceBundle, "resource bundle");
    add(LocaleReader.wrap(resourceBundle));
  }

  private static boolean classExists(String name) {
    try {
      Class.forName(name);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
