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

package dev.demeng.pluginbase.commands.bukkit.core;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.bukkit.BukkitCommandActor;
import dev.demeng.pluginbase.commands.bukkit.BukkitCommandHandler;
import dev.demeng.pluginbase.commands.bukkit.exception.SenderNotConsoleException;
import dev.demeng.pluginbase.commands.bukkit.exception.SenderNotPlayerException;
import dev.demeng.pluginbase.commands.util.Preconditions;
import dev.demeng.pluginbase.plugin.BaseManager;
import dev.demeng.pluginbase.text.Text;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
public final class BukkitActor implements BukkitCommandActor {

  private static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes(
      "CONSOLE".getBytes(StandardCharsets.UTF_8));

  private final CommandSender sender;
  private final BukkitHandler handler;

  public BukkitActor(final CommandSender sender, final CommandHandler handler) {
    this.sender = Preconditions.notNull(sender, "sender");
    this.handler = (BukkitHandler) Preconditions.notNull(handler, "handler");
  }

  @Override
  public @NotNull CommandSender getSender() {
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
  public @NotNull Audience audience() {
    return BaseManager.getAdventure().sender(getSender());
  }

  @Override
  public void reply(@NotNull final ComponentLike component) {
    audience().sendMessage(component);
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
    Preconditions.notNull(message, "message");
    if (BaseManager.getBaseSettings().colorScheme() != null) {
      Text.tell(sender, BaseManager.getBaseSettings().colorScheme().getPrimary() + message);
    } else {
      Text.tell(sender, message);
    }
  }

  @Override
  public void error(@NotNull final String message) {
    Preconditions.notNull(message, "message");
    Text.tell(sender, "&c" + message);
  }

  @Override
  public BukkitCommandHandler getCommandHandler() {
    return handler;
  }

  @Override
  public @NotNull Locale getLocale() {
    return Text.getLocale(sender);
  }
}
