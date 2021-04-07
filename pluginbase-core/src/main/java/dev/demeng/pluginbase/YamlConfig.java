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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import lombok.Getter;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

/**
 * A standard YAML configuration file with several shortcuts to common methods including such as
 * reloading or saving. Uses the SimpleYAML library rather than Bukkit's YAML library as it allows
 * more features such as persistent comments.
 */
public class YamlConfig {

  /**
   * The name of the key that stores an integer value, which is the current configuration file
   * version.
   */
  private static final String VERSION_KEY = "config-version";

  /**
   * The actual configuration object which is what you use for modifying and accessing the config.
   */
  @Getter private final YamlFile config;

  /**
   * Loads a YAML configuration file. If the file does not exist, a new file will be copied from the
   * project's resources folder.
   *
   * @param parent The folder the file should go in- should be your plugin data folder
   * @param name   The name of the config, ending in .yml or .yaml
   * @throws InvalidConfigurationException If there was a formatting/parsing error in the config
   * @throws IOException                   If the file could not be created and/or loaded
   */
  public YamlConfig(final File parent, final String name)
      throws InvalidConfigurationException, IOException {

    final String completeName =
        name.endsWith(".yml") || name.endsWith(".yaml") ? name : name + ".yml";

    final String completePath =
        parent == null ? completeName : parent.toPath().toString() + File.separator + completeName;

    config = new YamlFile(completePath);

    if (!config.exists()) {
      copyInputStreamToFile(getFileFromResourceAsStream(name), new File(completePath));

    } else {
      config.createNewFile(false);
    }

    config.load();
  }

  /**
   * Reloads the configuration file.
   *
   * @throws InvalidConfigurationException If there was a formatting/parsing error in the config
   * @throws IOException                   If the file could not be reloaded
   */
  public void reload() throws InvalidConfigurationException, IOException {
    config.load();
  }

  /**
   * Saves the configuration file (after making edits).
   *
   * @throws IOException If the file could not be saved
   */
  public void save() throws IOException {
    config.save();
  }

  /**
   * Checks if the config version integer equals or is greater than the current version.
   *
   * @param currentVersion The expected value of the config version
   * @return true if the config is update to date, false otherwise
   */
  public boolean configUpToDate(final int currentVersion) {
    return config.getInt(VERSION_KEY, -1) >= currentVersion;
  }

  private InputStream getFileFromResourceAsStream(final String fileName) {

    final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

    if (inputStream == null) {
      throw new IllegalArgumentException("File not found in JAR file: " + fileName);
    } else {
      return inputStream;
    }
  }

  private static void copyInputStreamToFile(final InputStream source, final File destination)
      throws IOException {

    java.nio.file.Files.copy(
        source,
        destination.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    try {
      source.close();
    } catch (final IOException ignored) {
      // Close quietly.
    }
  }
}
