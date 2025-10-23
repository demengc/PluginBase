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

import dev.demeng.pluginbase.plugin.BaseManager;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * A standard YAML configuration file with several shortcuts to common methods including such as
 * reloading or saving.
 */
public class YamlConfig {

  /**
   * The name of the key that stores an integer value, which is the current configuration file
   * version.
   */
  private static final String VERSION_KEY = "config-version";

  /**
   * The system file of the config.
   */
  @Getter private final File file;

  /**
   * The actual configuration object which is what you use for modifying and accessing the config.
   */
  @Getter private final FileConfiguration config;

  /**
   * Loads a YAML configuration file. If the file does not exist, a new file will be copied from the
   * project's resources folder.
   *
   * @param name The name of the config, ending in .yml
   * @throws InvalidConfigurationException If there was a formatting/parsing error in the config
   * @throws IOException                   If the file could not be created and/or loaded
   */
  public YamlConfig(final String name)
      throws InvalidConfigurationException, IOException {

    final String completeName = name.endsWith(".yml") ? name : name + ".yml";

    final File configFile = new File(BaseManager.getPlugin().getDataFolder(), completeName);

    if (!configFile.exists()) {
      //noinspection ResultOfMethodCallIgnored
      configFile.getParentFile().mkdirs();
      BaseManager.getPlugin().saveResource(completeName, false);
    }

    this.file = configFile;
    this.config = YamlConfiguration.loadConfiguration(file);
    reload();
  }

  /**
   * Reloads the configuration file.
   *
   * @throws InvalidConfigurationException If there was a formatting/parsing error in the config
   * @throws IOException                   If the file could not be reloaded
   */
  public void reload() throws InvalidConfigurationException, IOException {
    config.load(file);
  }

  /**
   * Saves the configuration file (after making edits).
   *
   * @throws IOException If the file could not be saved
   */
  public void save() throws IOException {
    config.save(file);
  }

  /**
   * Checks if the config version integer equals or is less than the current version.
   *
   * @param currentVersion The expected value of the config version
   * @return True if the config is outdated, false otherwise
   */
  public boolean isOutdated(final int currentVersion) {
    return config.getInt(VERSION_KEY, -1) < currentVersion;
  }
}
