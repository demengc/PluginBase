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

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.command.CommandManager;
import dev.demeng.pluginbase.libby.Library;
import dev.demeng.pluginbase.libby.managers.BukkitLibraryManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An extended version of JavaPlugin. Main class must extend this class in order to use PluginBase.
 */
@SuppressWarnings("unused")
public abstract class BasePlugin extends JavaPlugin {

  private BukkitLibraryManager libraryManager;
  private CommandManager commandManager;

  @Override
  public final void onLoad() {
    BaseLoader.setPlugin(this);
    libraryManager = new BukkitLibraryManager();
    commandManager = new CommandManager();
    load();
  }

  @Override
  public final void onEnable() {
    enable();
  }

  @Override
  public final void onDisable() {
    disable();
    commandManager.unregisterAll();
  }

  /**
   * Load a new library at runtime. Make sure to add the repository using the library manager before
   * using this method.
   *
   * @param groupId       The group ID of the dependency
   * @param artifactId    The artifact ID of the dependency
   * @param version       The version of the dependency
   * @param pattern       The original packaging pattern, or null if you do not want to relocate
   * @param shadedPattern The new packaging pattern, or null if you do not want to relocate
   * @see BukkitLibraryManager#addMavenCentral()
   * @see BukkitLibraryManager#addRepository(String)
   */
  public void loadLib(
      String groupId, String artifactId, String version, String pattern, String shadedPattern) {

    if (groupId == null || artifactId == null || version == null) {
      throw new NullPointerException("Library group ID, artifact ID, or version is null");
    }

    final Library.Builder builder = Library.builder();

    builder.groupId(groupId).artifactId(artifactId).version(version);

    if (pattern != null && shadedPattern != null) {
      builder.relocate(pattern, shadedPattern);
    }

    libraryManager.loadLibrary(builder.build());
  }

  /**
   * The library manager for the plugin.
   *
   * @return The library manager
   */
  public BukkitLibraryManager getLibraryManager() {
    return libraryManager;
  }

  /**
   * The command manager for the plugin.
   *
   * @return The command manager
   */
  public CommandManager getCommandManager() {
    return commandManager;
  }

  /**
   * The settings the base should use.
   *
   * @return The base settings
   */
  public abstract BaseSettings getBaseSettings();

  /**
   * Code to perform at early plugin startup.
   */
  protected void load() {
    // Override if needed, otherwise nothing will be executed.
  }

  /**
   * Code to perform on plugin enable.
   */
  protected void enable() {
    // Override if needed, otherwise nothing will be executed.
  }

  /**
   * Code to perform on plugin disable.
   */
  protected void disable() {
    // Override if needed, otherwise nothing will be executed.
  }
}
