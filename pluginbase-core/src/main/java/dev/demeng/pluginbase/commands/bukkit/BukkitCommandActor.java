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
import dev.demeng.pluginbase.commands.bukkit.core.BukkitActor;
import dev.demeng.pluginbase.commands.bukkit.exception.SenderNotConsoleException;
import dev.demeng.pluginbase.commands.bukkit.exception.SenderNotPlayerException;
import dev.demeng.pluginbase.commands.command.CommandActor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Bukkit {@link CommandActor} that wraps {@link CommandSender}
 */
public interface BukkitCommandActor extends CommandActor {

  /**
   * Returns the underlying {@link CommandSender} of this actor
   *
   * @return The sender
   */
  @NotNull CommandSender getSender();

  /**
   * Tests whether is this actor a player or not
   *
   * @return Is this a player or not
   */
  boolean isPlayer();

  /**
   * Tests whether is this actor the console or not
   *
   * @return Is this the console or not
   */
  boolean isConsole();

  /**
   * Returns this actor as a {@link Player} if it is a player, otherwise returns {@code null}.
   *
   * @return The sender as a player, or null.
   */
  @Nullable Player getAsPlayer();

  /**
   * Returns this actor as a {@link Player} if it is a player, otherwise throws a
   * {@link SenderNotPlayerException}.
   *
   * @return The actor as a player
   * @throws SenderNotPlayerException if not a player
   */
  @NotNull Player requirePlayer() throws SenderNotPlayerException;

  /**
   * Returns this actor as a {@link ConsoleCommandSender} if it is a player, otherwise throws a
   * {@link SenderNotConsoleException}.
   *
   * @return The actor as console
   * @throws SenderNotConsoleException if not a player
   */
  @NotNull ConsoleCommandSender requireConsole() throws SenderNotConsoleException;

  /**
   * Returns the {@link Audience} of this sender.
   *
   * @return The audience of this sender
   */
  @NotNull Audience audience();

  /**
   * Sends the given component to this actor.
   *
   * @param component Component to send
   */
  void reply(@NotNull ComponentLike component);

  /**
   * Returns the command handler that constructed this actor
   *
   * @return The command handler
   */
  @Override
  BukkitCommandHandler getCommandHandler();

  /**
   * Creates a new {@link BukkitCommandActor} that wraps the given {@link CommandSender}.
   *
   * @param sender Command sender to wrap
   * @return The wrapping {@link BukkitCommandActor}.
   */
  static @NotNull BukkitCommandActor wrap(@NotNull CommandSender sender,
      @NotNull CommandHandler handler) {
    return new BukkitActor(sender, handler);
  }
}
