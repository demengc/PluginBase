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

package dev.demeng.pluginbase.commands.command;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.core.BaseActor;
import dev.demeng.pluginbase.commands.exception.SenderNotConsoleException;
import dev.demeng.pluginbase.commands.exception.SenderNotPlayerException;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a command sender, responsible for performing a command-related action.
 */
public interface CommandActor {

  /**
   * Returns the name of this actor. Varies depending on the platform.
   *
   * @return The actor name
   */
  @NotNull String getName();

  /**
   * Returns the unique UID of this subject. Varies depending on the platform.
   * <p>
   * Although some platforms explicitly have their underlying senders have UUIDs, some platforms may
   * have to generate this UUID based on other available data.
   *
   * @return The UUID of this subject.
   */
  @NotNull UUID getUniqueId();

  /**
   * Replies to the sender with the specified message.
   *
   * @param message Message to reply with.
   */
  void reply(@NotNull String message);

  /**
   * Returns the command handler that constructed this actor
   *
   * @return The command handler
   */
  CommandHandler getCommandHandler();

  /**
   * Returns this actor as the specified type. This is effectively casting this actor to the given
   * type.
   *
   * @param type Type to cast to
   * @param <T>  The actor type
   * @return This actor but casted.
   */
  default <T extends CommandActor> T as(@NotNull Class<T> type) {
    return type.cast(this);
  }

  /**
   * Returns the underlying {@link CommandSender} of this actor
   *
   * @return The sender
   */
  CommandSender getSender();

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
   * Returns this actor as a {@link Player} if it is a player, otherwise throws a {@link
   * SenderNotPlayerException}.
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
   * Creates a new {@link CommandActor} that wraps the given {@link CommandSender}.
   *
   * @param sender Command sender to wrap
   * @return The wrapping {@link CommandActor}.
   */
  static @NotNull CommandActor wrap(@NotNull CommandSender sender,
      @NotNull CommandHandler handler) {
    return new BaseActor(sender, handler);
  }
}
