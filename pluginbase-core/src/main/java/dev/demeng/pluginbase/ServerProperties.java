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

package dev.demeng.pluginbase;

import dev.demeng.pluginbase.exceptions.BaseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Utility for getting information from server.properties. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerProperties {

  private static final Map<String, String> cache = new HashMap<>();
  private static File propertiesFile;

  /**
   * Gets a property from server.properties.
   *
   * @param key The property key
   * @return The property value, or null if failed to retrieve
   */
  public static String getProperty(final String key) {

    if (cache.containsKey(key)) {
      return cache.get(key);
    }

    final Properties properties = new Properties();

    try (final FileInputStream in = new FileInputStream(getPropertiesFile())) {
      properties.load(in);

      final String value = properties.getProperty(key);
      cache.put(key, value);
      return value;

    } catch (final IOException ex) {
      Common.error(ex, "Failed to get server property: " + key, false);
      return null;
    }
  }

  /** Clears the properties cache. */
  public static void clearCache() {
    cache.clear();
  }

  private static File getPropertiesFile() {

    if (propertiesFile != null) {
      return propertiesFile;
    }

    propertiesFile = new File("server.properties");

    if (!propertiesFile.exists()) {
      throw new BaseException("Could not locate server.properties");
    }

    return propertiesFile;
  }
}
