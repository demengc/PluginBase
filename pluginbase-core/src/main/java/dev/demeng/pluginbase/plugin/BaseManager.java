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
import dev.demeng.pluginbase.exceptions.BaseException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A manager containing all other managers associated with the library, as well as the {@link
 * JavaPlugin} the library is working with.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BaseManager {

  private static BasePlugin plugin = null;
  private static Thread mainThread = null;

  /**
   * The command manager for the plugin. Should be set when your plugin enables.
   */
  @NotNull @Getter @Setter private static CommandManager commandManager;

  /**
   * The BukkitAudiences instance to use for Adventure. Should be set when your plugin enables.
   */
  @Getter @Setter private static BukkitAudiences adventure;

  /**
   * The settings the library should use.
   */
  @NotNull @Getter @Setter private static BaseSettings baseSettings = new BaseSettings() {
  };

  /**
   * Gets a never-null instance of the {@link JavaPlugin} the library is currently working with.
   *
   * @return The {@link JavaPlugin} the library is currently working with
   * @throws RuntimeException If the plugin is not set
   */
  @NotNull
  public static JavaPlugin getPlugin() {

    if (plugin == null) {
      throw new BaseException("Main class is not set");
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

  /**
   * Gets the main thread of the server that is using the plugin.
   *
   * @return The main thread
   */
  @NotNull
  public static synchronized Thread getMainThread() {

    if (mainThread == null && Bukkit.getServer().isPrimaryThread()) {
      mainThread = Thread.currentThread();
    }

    return mainThread;
  }
}
