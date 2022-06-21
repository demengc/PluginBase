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

  public void senderNotPlayer(@NotNull final CommandActor actor,
      @NotNull final SenderNotPlayerException exception) {
    actor.errorLocalized("commands.must-be-player");
  }

  public void senderNotConsole(@NotNull final CommandActor actor,
      @NotNull final SenderNotConsoleException exception) {
    actor.errorLocalized("commands.must-be-console");
  }

  public void invalidPlayer(@NotNull final CommandActor actor,
      @NotNull final InvalidPlayerException exception) {
    actor.errorLocalized("commands.invalid-player", exception.getInput());
  }

  public void invalidWorld(@NotNull final CommandActor actor,
      @NotNull final InvalidWorldException exception) {
    actor.errorLocalized("commands.invalid-world", exception.getInput());
  }

  public void malformedEntitySelector(@NotNull final CommandActor actor,
      @NotNull final MalformedEntitySelectorException exception) {
    actor.errorLocalized("commands.invalid-selector", exception.getInput());
  }

  public void moreThanOnePlayer(@NotNull final CommandActor actor,
      @NotNull final MoreThanOnePlayerException exception) {
    actor.errorLocalized("commands.only-one-player", exception.getInput());
  }

  public void nonPlayerEntities(@NotNull final CommandActor actor,
      @NotNull final NonPlayerEntitiesException exception) {
    actor.errorLocalized("commands.non-players-not-allowed", exception.getInput());
  }
}
