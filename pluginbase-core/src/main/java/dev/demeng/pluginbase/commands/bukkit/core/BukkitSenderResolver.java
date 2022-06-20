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

import dev.demeng.pluginbase.commands.bukkit.BukkitCommandActor;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.SenderResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

enum BukkitSenderResolver implements SenderResolver {

  INSTANCE;

  @Override
  public boolean isCustomType(Class<?> type) {
    return CommandSender.class.isAssignableFrom(type);
  }

  @Override
  public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor,
      @NotNull ExecutableCommand command) {
    BukkitCommandActor bActor = (BukkitCommandActor) actor;
    if (Player.class.isAssignableFrom(customSenderType)) {
      return bActor.requirePlayer();
    }
    if (ConsoleCommandSender.class.isAssignableFrom(customSenderType)) {
      return bActor.requireConsole();
    }
    return bActor.getSender();
  }
}
