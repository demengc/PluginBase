/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.plugin;

import dev.demeng.pluginbase.exceptions.BaseException;
import org.jetbrains.annotations.NotNull;

/**
 * Simple class containg a method to get the {@link BasePlugin} the library is working with.
 */
public final class BaseLoader {

  private static BasePlugin plugin = null;

  private BaseLoader() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Gets a never-null instance of the {@link BasePlugin} the library is currently working with.
   *
   * @return A never-null instance of the {@link BasePlugin} the library is currently working with.
   * @throws RuntimeException If the plugin is not set (main class does not extend {@link
   *                          BasePlugin})
   */
  @NotNull
  public static BasePlugin getPlugin() {

    if (plugin == null) {
      throw new BaseException("Main class does not extend DemPlugin");
    }

    return plugin;
  }

  /**
   * Sets the {@link BasePlugin} that the library is currently working with.
   *
   * @param newPlugin The new instance of {@link BasePlugin}.
   */
  public static void setPlugin(final BasePlugin newPlugin) {
    plugin = newPlugin;
  }
}
