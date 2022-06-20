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
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.exception.ArgumentParseException;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BukkitCommandExecutor implements TabExecutor {

  private final BukkitHandler handler;

  public BukkitCommandExecutor(BukkitHandler handler) {
    this.handler = handler;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    BukkitCommandActor actor = new BukkitActor(sender, handler);
    try {
      ArgumentStack arguments = handler.parseArguments(args);
      arguments.addFirst(command.getName());

      handler.dispatch(actor, arguments);
    } catch (Throwable t) {
      handler.getExceptionHandler().handleException(t, actor);
    }
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    try {
      BukkitCommandActor actor = new BukkitActor(sender, handler);
      ArgumentStack arguments = handler.parseArgumentsForCompletion(args);

      arguments.addFirst(command.getName());
      return handler.getAutoCompleter().complete(actor, arguments);
    } catch (ArgumentParseException e) {
      return Collections.emptyList();
    }
  }
}
