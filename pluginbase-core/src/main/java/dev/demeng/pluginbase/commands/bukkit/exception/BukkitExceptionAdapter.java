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

package dev.demeng.pluginbase.commands.bukkit.exception;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.exception.DefaultExceptionHandler;
import org.jetbrains.annotations.NotNull;

public class BukkitExceptionAdapter extends DefaultExceptionHandler {

  public static final BukkitExceptionAdapter INSTANCE = new BukkitExceptionAdapter();

  public void senderNotPlayer(@NotNull CommandActor actor,
      @NotNull SenderNotPlayerException exception) {
    actor.errorLocalized("must-be-player");
  }

  public void senderNotConsole(@NotNull CommandActor actor,
      @NotNull SenderNotConsoleException exception) {
    actor.errorLocalized("must-be-console");
  }

  public void invalidPlayer(@NotNull CommandActor actor,
      @NotNull InvalidPlayerException exception) {
    actor.errorLocalized("invalid-player", exception.getInput());
  }

  public void invalidWorld(@NotNull CommandActor actor, @NotNull InvalidWorldException exception) {
    actor.errorLocalized("invalid-world", exception.getInput());
  }

  public void malformedEntitySelector(@NotNull CommandActor actor,
      @NotNull MalformedEntitySelectorException exception) {
    actor.errorLocalized("invalid-selector", exception.getInput());
  }

  public void moreThanOnePlayer(@NotNull CommandActor actor,
      @NotNull MoreThanOnePlayerException exception) {
    actor.errorLocalized("only-one-player", exception.getInput());
  }

  public void nonPlayerEntities(@NotNull CommandActor actor,
      @NotNull NonPlayerEntitiesException exception) {
    actor.errorLocalized("non-players-not-allowed", exception.getInput());
  }
}
