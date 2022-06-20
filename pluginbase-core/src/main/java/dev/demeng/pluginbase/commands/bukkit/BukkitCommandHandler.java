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

package dev.demeng.pluginbase.commands.bukkit;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.bukkit.core.BukkitHandler;
import java.util.Optional;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Represents Bukkit's command handler implementation
 */
public interface BukkitCommandHandler extends CommandHandler {

  /**
   * Returns an optional {@link BukkitBrigadier} of this command handler.
   * <p>
   * On versions that do not support Brigadier (i.e. 1.12.2 or earlier), this optional will be
   * empty.
   *
   * @return The Brigadier accessor
   */
  @NotNull Optional<BukkitBrigadier> getBrigadier();

  /**
   * Checks to see if the Brigadier command system is supported by the server.
   *
   * @return true if Brigadier is supported.
   */
  boolean isBrigadierSupported();

  /**
   * Registers commands automatically on Minecraft's 1.13+ command system (so that you would get the
   * colorful command completions!)
   * <p>
   * Note that you should call this method after you've registered all your commands.
   * <p>
   * This is effectively the same as {@code getBrigadier().register()}, and will have no effect when
   * invoked on older versions.
   *
   * @return This command handler
   */
  BukkitCommandHandler registerBrigadier();

  /**
   * Returns the plugin this command handler was registered for.
   *
   * @return The owning plugin
   */
  @NotNull Plugin getPlugin();

  /**
   * Creates a new {@link BukkitCommandHandler} for the specified plugin
   *
   * @param plugin Plugin to create for
   * @return The newly created command handler
   */
  static @NotNull BukkitCommandHandler create(@NotNull Plugin plugin) {
    return new BukkitHandler(plugin);
  }
}

