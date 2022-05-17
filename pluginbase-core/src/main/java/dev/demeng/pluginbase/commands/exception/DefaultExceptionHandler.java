/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.TimeUtils;
import dev.demeng.pluginbase.TimeUtils.DurationFormatter;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.text.NumberFormat;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of {@link CommandExceptionHandler}, which sends basic messages describing
 * the exception.
 * <p>
 * See {@link CommandExceptionAdapter} for handling custom exceptions.
 */
public class DefaultExceptionHandler extends CommandExceptionAdapter {

  public static final DefaultExceptionHandler INSTANCE = new DefaultExceptionHandler();
  public static final NumberFormat FORMAT = NumberFormat.getInstance();

  @Override
  public void invalidCommand(@NotNull CommandActor actor,
      @NotNull InvalidCommandException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidCommand()
        .replace("%input%", exception.getInput()));
  }

  @Override
  public void invalidSubcommand(@NotNull CommandActor actor,
      @NotNull InvalidSubcommandException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidSubcommand()
        .replace("%input%", exception.getInput()));
  }

  @Override
  public void noSubcommandSpecified(@NotNull CommandActor actor,
      @NotNull NoSubcommandSpecifiedException exception) {
    actor.reply(BaseManager.getBaseSettings().noSubcommandSpecified());
  }

  public void senderNotPlayer(@NotNull CommandActor actor,
      @NotNull SenderNotPlayerException exception) {
    actor.reply(BaseManager.getBaseSettings().notPlayer());
  }

  public void senderNotConsole(@NotNull CommandActor actor,
      @NotNull SenderNotConsoleException exception) {
    actor.reply(BaseManager.getBaseSettings().notConsole());
  }

  @Override
  public void noPermission(@NotNull CommandActor actor, @NotNull NoPermissionException exception) {
    actor.reply(BaseManager.getBaseSettings().insufficientPermission());
  }

  @Override
  public void missingArgument(@NotNull CommandActor actor,
      @NotNull MissingArgumentException exception) {
    actor.reply(BaseManager.getBaseSettings().missingArgument()
        .replace("%usage%", getFullUsage(exception.getCommand()))
        .replace("%parameter%", exception.getParameter().getName()));
  }

  @Override
  public void tooManyArguments(@NotNull CommandActor actor,
      @NotNull TooManyArgumentsException exception) {
    actor.reply(BaseManager.getBaseSettings().tooManyArguments()
        .replace("%usage%", getFullUsage(exception.getCommand())));
  }

  @Override
  public void cooldown(@NotNull CommandActor actor, @NotNull CooldownException exception) {
    actor.reply(BaseManager.getBaseSettings().cooldown()
        .replace("%remaining%",
            TimeUtils.formatDuration(DurationFormatter.LONG, exception.getTimeLeftMillis())));
  }

  @Override
  public void invalidNumber(@NotNull CommandActor actor,
      @NotNull InvalidNumberException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidNumber()
        .replace("%input%", exception.getInput()));
  }

  public void invalidPlayer(@NotNull CommandActor actor,
      @NotNull InvalidPlayerException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidPlayer()
        .replace("%input%", exception.getInput()));
  }

  @Override
  public void invalidUuid(@NotNull CommandActor actor, @NotNull InvalidUuidException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidUuid()
        .replace("%input%", exception.getInput()));
  }

  @Override
  public void invalidBoolean(@NotNull CommandActor actor,
      @NotNull InvalidBooleanException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidBoolean()
        .replace("%input%", exception.getInput()));
  }

  @Override
  public void invalidEnumValue(@NotNull CommandActor actor,
      @NotNull EnumNotFoundException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidEnum()
        .replace("%parameter%", exception.getParameter().getName())
        .replace("%input%", exception.getInput()));
  }

  @Override
  public void invalidUrl(@NotNull CommandActor actor, @NotNull InvalidUrlException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidUrl()
        .replace("%input%", exception.getInput()));
  }

  public void invalidWorld(@NotNull CommandActor actor, @NotNull InvalidWorldException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidWorld()
        .replace("%input%", exception.getInput()));
  }

  public void malformedEntitySelector(@NotNull CommandActor actor,
      @NotNull MalformedEntitySelectorException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidSelector()
        .replace("%input%", exception.getInput()));
  }

  @Override
  public void argumentParse(@NotNull CommandActor actor,
      @NotNull ArgumentParseException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidQuotedString()
        .replace("%source-string%", exception.getSourceString())
        .replace("%annotated-position%", exception.getAnnotatedPosition()));
  }

  @Override
  public void numberNotInRange(@NotNull CommandActor actor,
      @NotNull NumberNotInRangeException exception) {
    actor.reply(BaseManager.getBaseSettings().numberNotInRange()
        .replace("%parameter%", exception.getParameter().getName())
        .replace("%input%", FORMAT.format(exception.getInput()))
        .replace("%min%", FORMAT.format(exception.getMinimum()))
        .replace("%max%", FORMAT.format(exception.getMaximum())));
  }

  @Override
  public void invalidHelpPage(@NotNull CommandActor actor,
      @NotNull InvalidHelpPageException exception) {
    actor.reply(BaseManager.getBaseSettings().invalidHelpPage()
        .replace("%input%", FORMAT.format(exception.getPage()))
        .replace("%max%", FORMAT.format(exception.getPageCount())));
  }

  @Override
  public void commandInvocation(@NotNull CommandActor actor,
      @NotNull CommandInvocationException exception) {
    Common.error(exception.getCause(), "Failed to invoke command.", false, actor.getSender());
  }

  @Override
  public void sendableException(@NotNull CommandActor actor, @NotNull SendableException exception) {
    exception.sendTo(actor);
  }

  @Override
  public void onUnhandledException(@NotNull CommandActor actor, @NotNull Throwable throwable) {
    throwable.printStackTrace();
  }

  private String getFullUsage(ExecutableCommand command) {
    return (command.getPath().toRealString() + " " + command.getUsage()).trim();
  }
}
