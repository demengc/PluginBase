/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

package dev.demeng.pluginbase.command;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Time;
import dev.demeng.pluginbase.Time.DurationFormatter;
import dev.demeng.pluginbase.text.Text;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.exception.EmptyEntitySelectorException;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.InvalidWorldException;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.bukkit.exception.MissingLocationParameterException;
import revxrsal.commands.bukkit.exception.MoreThanOneEntityException;
import revxrsal.commands.bukkit.exception.NonPlayerEntitiesException;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.exception.CommandInvocationException;
import revxrsal.commands.exception.CooldownException;
import revxrsal.commands.exception.EnumNotFoundException;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.InvalidBooleanException;
import revxrsal.commands.exception.InvalidDecimalException;
import revxrsal.commands.exception.InvalidHelpPageException;
import revxrsal.commands.exception.InvalidIntegerException;
import revxrsal.commands.exception.InvalidListSizeException;
import revxrsal.commands.exception.InvalidStringSizeException;
import revxrsal.commands.exception.InvalidUUIDException;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.exception.NumberNotInRangeException;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.exception.UnknownParameterException;
import revxrsal.commands.exception.ValueNotAllowedException;
import revxrsal.commands.node.ParameterNode;

/**
 * Comprehensive exception handler for Lamp command framework, providing localized error messages
 * using PluginBase's utilities. All error messages can be customized through locale files. See the
 * "snippets" folder for a sample locale configuration.
 */
public class BaseExceptionHandler extends BukkitExceptionHandler {

  /**
   * Gets a localized error message with fallback to a default message.
   *
   * @param actor          The command actor
   * @param key            The localization key
   * @param defaultMessage The default message if localization is not available
   * @param args           Arguments for placeholders in the localized message
   * @return The localized or default message (not colorized, will be handled by Text.tell)
   */
  private String getLocalizedError(
      final BukkitCommandActor actor,
      final String key,
      final String defaultMessage,
      final Object... args) {
    final String localized = Text.localized(key, actor.sender(), args);
    // If the key wasn't found, the translator returns the key itself
    // In that case, use the default message
    if (localized.equals(key)) {
      return defaultMessage;
    }
    return localized;
  }

  @Override
  public void onInvalidPlayer(final InvalidPlayerException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor, "commands.invalid-player", "&cInvalid player: &e{0}&c.", e.input()));
  }

  @Override
  public void onInvalidWorld(final InvalidWorldException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(actor, "commands.invalid-world", "&cInvalid world: &e{0}&c.", e.input()));
  }

  @Override
  public void onInvalidWorld(
      final MissingLocationParameterException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.missing-location-parameter",
            "&cExpected &e{0}&c.",
            e.axis().name().toLowerCase()));
  }

  @Override
  public void onSenderNotConsole(
      final SenderNotConsoleException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.sender-not-console",
            "&cYou must be the console to execute this command!"));
  }

  @Override
  public void onSenderNotPlayer(final SenderNotPlayerException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.sender-not-player",
            "&cYou must be a player to execute this command!"));
  }

  @Override
  public void onMalformedEntitySelector(
      final MalformedEntitySelectorException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.malformed-entity-selector",
            "&cMalformed entity selector: &e{0}&c. Error: &e{1}",
            e.input(),
            e.errorMessage()));
  }

  @Override
  public void onNonPlayerEntities(
      final NonPlayerEntitiesException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.non-player-entities",
            "&cEntity selector &e{0} &cmust only target players.",
            e.input()));
  }

  @Override
  public void onMoreThanOneEntity(
      final MoreThanOneEntityException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.more-than-one-entity",
            "&cEntity selector must target only one entity."));
  }

  @Override
  public void onEmptyEntitySelector(
      final EmptyEntitySelectorException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(actor, "commands.empty-entity-selector", "&cNo entities were found."));
  }

  @Override
  public void onEnumNotFound(
      @NotNull final EnumNotFoundException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor, "commands.enum-not-found", "&cInvalid value: &e{0}&c.", e.input()));
  }

  @Override
  public void onExpectedLiteral(
      @NotNull final ExpectedLiteralException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.expected-literal",
            "&cExpected &e{0}&c, found &e{1}&c.",
            e.node().name(),
            e.input()));
  }

  @Override
  public void onInputParse(
      @NotNull final InputParseException e, @NotNull final BukkitCommandActor actor) {
    switch (e.cause()) {
      case INVALID_ESCAPE_CHARACTER:
        Text.tell(
            actor.sender(),
            getLocalizedError(
                actor,
                "commands.input-parse.invalid-escape",
                "&cInvalid input. Use &e\\\\\\\\\\\\\\\\ &cto include a backslash."));
        break;
      case UNCLOSED_QUOTE:
        Text.tell(
            actor.sender(),
            getLocalizedError(
                actor,
                "commands.input-parse.unclosed-quote",
                "&cUnclosed quote. Make sure to close all quotes."));
        break;
      case EXPECTED_WHITESPACE:
        Text.tell(
            actor.sender(),
            getLocalizedError(
                actor,
                "commands.input-parse.expected-whitespace",
                "&cUnexpected text after argument."));
        break;
    }
  }

  @Override
  public void onInvalidListSize(
      @NotNull final InvalidListSizeException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, ?> parameter) {
    if (e.inputSize() < e.minimum()) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.invalid-list-size.too-small",
              "&cYou must input at least &e{0} &centries for &e{1}&c.",
              fmt(e.minimum()),
              parameter.name()));
    }
    if (e.inputSize() > e.maximum()) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.invalid-list-size.too-large",
              "&cYou must input at most &e{0} &centries for &e{1}&c.",
              fmt(e.maximum()),
              parameter.name()));
    }
  }

  @Override
  public void onInvalidStringSize(
      @NotNull final InvalidStringSizeException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, ?> parameter) {
    if (e.input().length() < e.minimum()) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.invalid-string-size.too-small",
              "&cParameter &e{0} &cmust be at least &e{1} &ccharacters long.",
              parameter.name(),
              fmt(e.minimum())));
    }
    if (e.input().length() > e.maximum()) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.invalid-string-size.too-large",
              "&cParameter &e{0} &ccan be at most &e{1} &ccharacters long.",
              parameter.name(),
              fmt(e.maximum())));
    }
  }

  @Override
  public void onInvalidBoolean(
      @NotNull final InvalidBooleanException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.invalid-boolean",
            "&cExpected &etrue &cor &efalse&c, found &e{0}&c.",
            e.input()));
  }

  @Override
  public void onInvalidDecimal(
      @NotNull final InvalidDecimalException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor, "commands.invalid-decimal", "&cInvalid number: &e{0}&c.", e.input()));
  }

  @Override
  public void onInvalidInteger(
      @NotNull final InvalidIntegerException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor, "commands.invalid-integer", "&cInvalid integer: &e{0}&c.", e.input()));
  }

  @Override
  public void onInvalidUUID(
      @NotNull final InvalidUUIDException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(actor, "commands.invalid-uuid", "&cInvalid UUID: &e{0}&c.", e.input()));
  }

  @Override
  public void onMissingArgument(
      @NotNull final MissingArgumentException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, ?> parameter) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.missing-argument",
            "&cRequired parameter is missing: &e{0}&c. Usage: &e/{1}&c.",
            parameter.name(),
            parameter.command().usage()));
  }

  @Override
  public void onNoPermission(
      @NotNull final NoPermissionException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.no-permission",
            "&cYou do not have permission to execute this command!"));
  }

  @Override
  public void onNumberNotInRange(
      @NotNull final NumberNotInRangeException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, Number> parameter) {
    if (e.input().doubleValue() < e.minimum()) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.number-not-in-range.too-small",
              "&c{0} too small &e({1})&c. Must be at least &e{2}&c.",
              parameter.name(),
              fmt(e.input()),
              fmt(e.minimum())));
    }
    if (e.input().doubleValue() > e.maximum()) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.number-not-in-range.too-large",
              "&c{0} too large &e({1})&c. Must be at most &e{2}&c.",
              parameter.name(),
              fmt(e.input()),
              fmt(e.maximum())));
    }
  }

  @Override
  public void onInvalidHelpPage(
      @NotNull final InvalidHelpPageException e, @NotNull final BukkitCommandActor actor) {
    if (e.numberOfPages() == 1) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.invalid-help-page.single",
              "&cInvalid help page: &e{0}&c. Must be 1.",
              e.page()));
    } else {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.invalid-help-page.multiple",
              "&cInvalid help page: &e{0}&c. Must be between &e1 &cand &e{1}&c.",
              e.page(),
              e.numberOfPages()));
    }
  }

  @Override
  public void onUnknownCommand(
      @NotNull final UnknownCommandException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor, "commands.unknown-command", "&cUnknown command: &e{0}&c.", e.input()));
  }

  @Override
  public void onValueNotAllowed(
      @NotNull final ValueNotAllowedException e, @NotNull final BukkitCommandActor actor) {
    final String allowedValues = String.join("&c, &e", e.allowedValues());
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.value-not-allowed",
            "&cInvalid value: &e{0}&c. Allowed: &e{1}&c.",
            e.input(),
            allowedValues));
  }

  @Override
  public void onCommandInvocation(
      @NotNull final CommandInvocationException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.internal-error",
            "&cAn error occurred while executing this command. Please contact an administrator."));

    Common.error(e.cause(), "Failed to execute command.", false);
  }

  @Override
  public void onUnknownParameter(
      @NotNull final UnknownParameterException e, @NotNull final BukkitCommandActor actor) {
    if (e.shorthand()) {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor,
              "commands.unknown-parameter.shorthand",
              "&cUnknown shorthand flag: &e{0}&c.",
              e.name()));
    } else {
      Text.tell(
          actor.sender(),
          getLocalizedError(
              actor, "commands.unknown-parameter.flag", "&cUnknown flag: &e{0}&c.", e.name()));
    }
  }

  @Override
  public void onCooldown(
      @NotNull final CooldownException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        getLocalizedError(
            actor,
            "commands.cooldown",
            "&cYou must wait &e{0} &cbefore using this command again.",
            Time.formatDuration(DurationFormatter.CONCISE, e.getTimeLeftMillis())));
  }
}
