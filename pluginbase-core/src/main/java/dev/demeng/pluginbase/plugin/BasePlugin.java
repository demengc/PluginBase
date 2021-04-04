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
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.command.CommandManager;
import dev.demeng.pluginbase.dependencyloader.DependencyEngine;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * An extended version of JavaPlugin. Main class must extend this class in order to use PluginBase.
 */
@SuppressWarnings("unused")
public abstract class BasePlugin extends JavaPlugin {

  /**
   * The dependency engine for this plugin.
   */
  private final @NotNull DependencyEngine dependencyEngine;

  /**
   * The command manager for this plugin.
   */
  @Getter private CommandManager commandManager;

  /**
   * An instance of the Adventure library, for internal use.
   */
  @Getter private BukkitAudiences adventure;

  protected BasePlugin() {
    this.dependencyEngine = DependencyEngine
        .createNew(this.getDataFolder().toPath().resolve("dependencies"));
  }

  @Override
  public final void onLoad() {

    BaseLoader.setPlugin(this);

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

      return;
    }

    commandManager = new CommandManager();
    load();
  }

  @Override
  public final void onEnable() {

    adventure = BukkitAudiences.create(this);

    if (!dependencyEngine.getErrors().isEmpty()) {
      return;
    }

    enable();
  }

  @Override
  public final void onDisable() {

    if (adventure != null) {
      adventure.close();
      adventure = null;
    }

    if (!dependencyEngine.getErrors().isEmpty()) {
      return;
    }

    disable();
    commandManager.unregisterAll();

    BaseLoader.setPlugin(null);
  }

  /**
   * The settings the base should use.
   *
   * @return The base settings
   */
  public abstract BaseSettings getBaseSettings();

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
}
