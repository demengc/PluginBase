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

package dev.demeng.pluginbase.commands.core;

import static dev.demeng.pluginbase.commands.util.Preconditions.notNull;

import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.exception.SenderNotConsoleException;
import dev.demeng.pluginbase.commands.exception.SenderNotPlayerException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BaseActor implements CommandActor {

  private static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes(
      "CONSOLE".getBytes(StandardCharsets.UTF_8));

  private final CommandSender sender;
  private final CommandHandler handler;

  public BaseActor(final CommandSender sender, final CommandHandler handler) {
    this.sender = notNull(sender, "sender");
    this.handler = notNull(handler, "handler");
  }

  @Override
  public CommandSender getSender() {
    return sender;
  }

  @Override
  public boolean isPlayer() {
    return sender instanceof Player;
  }

  @Override
  public boolean isConsole() {
    return sender instanceof ConsoleCommandSender;
  }

  @Override
  public @Nullable Player getAsPlayer() {
    return isPlayer() ? (Player) sender : null;
  }

  @Override
  public @NotNull Player requirePlayer() {
    if (!isPlayer()) {
      throw new SenderNotPlayerException();
    }
    return (Player) sender;
  }

  @Override
  public @NotNull ConsoleCommandSender requireConsole() {
    if (!isConsole()) {
      throw new SenderNotConsoleException();
    }
    return (ConsoleCommandSender) sender;
  }

  @Override
  public @NotNull String getName() {
    return sender.getName();
  }

  @Override
  public @NotNull UUID getUniqueId() {
    if (isPlayer()) {
      return ((Player) sender).getUniqueId();
    } else if (isConsole()) {
      return CONSOLE_UUID;
    } else {
      return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_8));
    }
  }

  @Override
  public void reply(@NotNull final String message) {
    notNull(message, "message");
    ChatUtils.tell(sender, message);
  }

  @Override
  public CommandHandler getCommandHandler() {
    return handler;
  }
}
