/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Parses resource files using UTF-8 character encoding.
 *
 * <p>See <a href=https://stackoverflow.com/a/4660195>https://stackoverflow.com/a/4660195</a>.
 */
final class UTF8Control extends ResourceBundle.Control {

  public static final UTF8Control INSTANCE = new UTF8Control();

  @Override
  public ResourceBundle newBundle(
      final String baseName,
      final Locale locale,
      final String format,
      final ClassLoader loader,
      final boolean reload)
      throws IOException {
    final String bundleName = toBundleName(baseName, locale);
    final String resourceName = toResourceName(bundleName, "properties");
    ResourceBundle bundle = null;
    InputStream stream = null;
    if (reload) {
      final URL url = loader.getResource(resourceName);
      if (url != null) {
        final URLConnection connection = url.openConnection();
        if (connection != null) {
          connection.setUseCaches(false);
          stream = connection.getInputStream();
        }
      }
    } else {
      stream = loader.getResourceAsStream(resourceName);
    }
    if (stream != null) {
      try {
        bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
      } finally {
        stream.close();
      }
    }
    return bundle;
  }
}
