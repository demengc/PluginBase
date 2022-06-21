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

package dev.demeng.pluginbase.plugin;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Registerer;
import dev.demeng.pluginbase.ServerProperties;
import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.bukkit.BukkitCommandHandler;
import dev.demeng.pluginbase.dependencyloader.DependencyEngine;
import dev.demeng.pluginbase.locale.Translator;
import dev.demeng.pluginbase.menu.MenuManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An extended version of JavaPlugin. It is recommended that you extend this in your main class, but
 * you can also manually set the values in {@link BaseManager} should you wish to use your own
 * loading system.
 */
public abstract class BasePlugin extends JavaPlugin {

  /**
   * The dependency engine for this plugin.
   */
  @Nullable private DependencyEngine dependencyEngine;

  @Override
  public final void onLoad() {
    BaseManager.setPlugin(this);
    load();
  }

  @Override
  public final void onEnable() {

    BaseManager.setAdventure(BukkitAudiences.create(this));

    if (dependencyEngine != null && !dependencyEngine.getErrors().isEmpty()) {
      return;
    }

    BaseManager.setTranslator(Translator.create());
    BaseManager.setCommandHandler(BukkitCommandHandler.create(this));

    Registerer.registerListener(new MenuManager());

    enable();
  }

  @Override
  public final void onDisable() {

    if (getAdventure() != null) {
      getAdventure().close();
      BaseManager.setAdventure(null);
    }

    if (dependencyEngine != null && !dependencyEngine.getErrors().isEmpty()) {
      return;
    }

    disable();

    if (getCommandHandler() != null) {
      getCommandHandler().unregisterAllCommands();
    }

    ServerProperties.clearCache();
    BaseManager.setPlugin(null);
  }

  /**
   * Executes at early plugin startup.
   */
  protected void load() {
    // Override if needed, otherwise nothing will be executed.
  }

  /**
   * Executes on plugin enable.
   */
  protected void enable() {
    // Override if needed, otherwise nothing will be executed.
  }

  /**
   * Executes on plugin disable.
   */
  protected void disable() {
    // Override if needed, otherwise nothing will be executed.
  }

  /**
   * Loads the dependency engine for the plugin. Call on {@link #load()} only if needed.
   *
   * @return True if successful, false otherwise
   */
  protected boolean loadDependencyEngine() {

    dependencyEngine = DependencyEngine
        .createNew(getDataFolder().toPath().resolve("dependencies"));

    dependencyEngine
        .addDependenciesFromClass(getClass())
        .loadDependencies()
        .exceptionally(ex -> {
          dependencyEngine.getErrors().add(ex);
          return null;
        })
        .join();

    if (!dependencyEngine.getErrors().isEmpty()) {
      for (final Throwable t : dependencyEngine.getErrors()) {
        Common.error(t, "Failed to download dependencies.", false);
      }

      return false;
    }

    return true;
  }

  /**
   * Gets the command handler for the plugin.
   *
   * @return The command handler for the plugin
   */
  @NotNull
  public CommandHandler getCommandHandler() {
    return BaseManager.getCommandHandler();
  }

  /**
   * Gets the BukkitAudiences instance to use for Adventure.
   *
   * @return The BukkitAudiences instance to use for Adventure
   */
  public BukkitAudiences getAdventure() {
    return BaseManager.getAdventure();
  }

  /**
   * Gets the settings the library should use.
   *
   * @return The settings the library should use
   */
  @NotNull
  public BaseSettings getBaseSettings() {
    return BaseManager.getBaseSettings();
  }

  /**
   * Sets the settings the library should use. Default will be used if not set.
   *
   * @param baseSettings The new base settings
   */
  public void setBaseSettings(@Nullable final BaseSettings baseSettings) {

    if (baseSettings == null) {
      BaseManager.setBaseSettings(new BaseSettings() {
      });
      return;
    }

    BaseManager.setBaseSettings(baseSettings);
  }
}
