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

package dev.demeng.pluginbase.locale.reader;

import dev.demeng.pluginbase.locale.LocaleReader;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * A locale reader that uses Bukkit config files (such as .yml) instead of standard resource bundles
 * (.properties). This should typically only be used if you want to have a singular locale file,
 * such as a "messages.yml".
 *
 * @see dev.demeng.pluginbase.locale.Translator#add(LocaleReader)
 */
@RequiredArgsConstructor
public class ConfigLocaleReader implements LocaleReader {

  @NotNull private final FileConfiguration config;
  @Getter @NotNull private final Locale locale;

  @Override
  public boolean containsKey(final String key) {
    return config.contains(key);
  }

  @Override
  public String get(final String key) {
    return config.getString(key);
  }
}
