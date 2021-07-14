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

package dev.demeng.pluginbase;

import dev.demeng.pluginbase.exceptions.BaseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for getting information from server.properties.
 *
 * <p>Work in progress: Currently, this is only used by the library to get the server's main world.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerProperties {

  private static File propertiesFile;
  private static String mainWorld;

  /**
   * Gets the name of the server's main world.
   *
   * @return The name of the server's main world from server.properties, or the "first" world if
   * unable to read property
   */
  @NotNull
  public static String getMainWorld() {

    if (mainWorld != null) {
      return mainWorld;
    }

    try {
      mainWorld = getProperty("level-name");
      return mainWorld;

    } catch (IOException ex) {
      Common.error(ex, "Failed to find main world.", false);
      return Bukkit.getWorlds().get(0).getName();
    }
  }

  private static String getProperty(String key) throws IOException {

    final Properties properties = new Properties();

    try (final FileInputStream in = new FileInputStream(getPropertiesFile())) {
      properties.load(in);
      return properties.getProperty(key);
    }
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
