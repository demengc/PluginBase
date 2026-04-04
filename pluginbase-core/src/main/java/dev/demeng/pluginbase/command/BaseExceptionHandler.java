/*
 * MIT License
 *
 * Copyright (c) 2026 Demeng Chen
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
import java.util.Locale;
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
   * Sends a localized error message for invalid player input.
   *
   * <p>Locale key: {@code commands.invalid-player}
   */
  @Override
  public void onInvalidPlayer(final InvalidPlayerException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.invalid-player", "&cInvalid player: &e{0}&c.", e.input()));
  }

  /**
   * Sends a localized error message for an invalid world name.
   *
   * <p>Locale key: {@code commands.invalid-world}
   */
  @Override
  public void onInvalidWorld(final InvalidWorldException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.invalid-world", "&cInvalid world: &e{0}&c.", e.input()));
  }

  /**
   * Sends a localized error message when a required location axis parameter is missing.
   *
   * <p>Locale key: {@code commands.missing-location-parameter}
   */
  @Override
  public void onInvalidWorld(
      final MissingLocationParameterException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.missing-location-parameter",
            "&cExpected &e{0}&c.",
            e.axis().name().toLowerCase(Locale.ROOT)));
  }

  /**
   * Sends a localized error message when a non-console sender runs a console-only command.
   *
   * <p>Locale key: {@code commands.sender-not-console}
   */
  @Override
  public void onSenderNotConsole(
      final SenderNotConsoleException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.sender-not-console",
            "&cYou must be the console to execute this command!"));
  }

  /**
   * Sends a localized error message when a non-player sender runs a player-only command.
   *
   * <p>Locale key: {@code commands.sender-not-player}
   */
  @Override
  public void onSenderNotPlayer(final SenderNotPlayerException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.sender-not-player",
            "&cYou must be a player to execute this command!"));
  }

  /**
   * Sends a localized error message for a malformed entity selector.
   *
   * <p>Locale key: {@code commands.malformed-entity-selector}
   */
  @Override
  public void onMalformedEntitySelector(
      final MalformedEntitySelectorException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.malformed-entity-selector",
            "&cMalformed entity selector: &e{0}&c. Error: &e{1}",
            e.input(),
            e.errorMessage()));
  }

  /**
   * Sends a localized error message when an entity selector targets non-player entities but only
   * players are allowed.
   *
   * <p>Locale key: {@code commands.non-player-entities}
   */
  @Override
  public void onNonPlayerEntities(
      final NonPlayerEntitiesException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.non-player-entities",
            "&cEntity selector &e{0} &cmust only target players.",
            e.input()));
  }

  /**
   * Sends a localized error message when an entity selector matches more than one entity but only
   * one is allowed.
   *
   * <p>Locale key: {@code commands.more-than-one-entity}
   */
  @Override
  public void onMoreThanOneEntity(
      final MoreThanOneEntityException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.more-than-one-entity",
            "&cEntity selector must target only one entity."));
  }

  /**
   * Sends a localized error message when an entity selector matches no entities.
   *
   * <p>Locale key: {@code commands.empty-entity-selector}
   */
  @Override
  public void onEmptyEntitySelector(
      final EmptyEntitySelectorException e, final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.empty-entity-selector", "&cNo entities were found."));
  }

  /**
   * Sends a localized error message when input does not match any enum constant.
   *
   * <p>Locale key: {@code commands.enum-not-found}
   */
  @Override
  public void onEnumNotFound(
      @NotNull final EnumNotFoundException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.enum-not-found", "&cInvalid value: &e{0}&c.", e.input()));
  }

  /**
   * Sends a localized error message when a literal node received unexpected input.
   *
   * <p>Locale key: {@code commands.expected-literal}
   */
  @Override
  public void onExpectedLiteral(
      @NotNull final ExpectedLiteralException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.expected-literal",
            "&cExpected &e{0}&c, found &e{1}&c.",
            e.node().name(),
            e.input()));
  }

  /**
   * Sends a localized error message for input parsing failures. The specific message depends on the
   * parse error cause:
   *
   * <ul>
   *   <li>Invalid escape character: {@code commands.input-parse.invalid-escape}
   *   <li>Unclosed quote: {@code commands.input-parse.unclosed-quote}
   *   <li>Expected whitespace: {@code commands.input-parse.expected-whitespace}
   * </ul>
   */
  @Override
  public void onInputParse(
      @NotNull final InputParseException e, @NotNull final BukkitCommandActor actor) {
    switch (e.cause()) {
      case INVALID_ESCAPE_CHARACTER:
        Text.tell(
            actor.sender(),
            Text.localizedOrDefault(
                actor.sender(),
                "commands.input-parse.invalid-escape",
                "&cInvalid input. Use &e\\\\\\\\\\\\\\\\ &cto include a backslash."));
        break;
      case UNCLOSED_QUOTE:
        Text.tell(
            actor.sender(),
            Text.localizedOrDefault(
                actor.sender(),
                "commands.input-parse.unclosed-quote",
                "&cUnclosed quote. Make sure to close all quotes."));
        break;
      case EXPECTED_WHITESPACE:
        Text.tell(
            actor.sender(),
            Text.localizedOrDefault(
                actor.sender(),
                "commands.input-parse.expected-whitespace",
                "&cUnexpected text after argument."));
        break;
    }
  }

  /**
   * Sends a localized error message when a list argument has too few or too many entries.
   *
   * <ul>
   *   <li>Too few entries: {@code commands.invalid-list-size.too-small}
   *   <li>Too many entries: {@code commands.invalid-list-size.too-large}
   * </ul>
   */
  @Override
  public void onInvalidListSize(
      @NotNull final InvalidListSizeException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, ?> parameter) {
    if (e.inputSize() < e.minimum()) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.invalid-list-size.too-small",
              "&cYou must input at least &e{0} &centries for &e{1}&c.",
              fmt(e.minimum()),
              parameter.name()));
    }
    if (e.inputSize() > e.maximum()) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.invalid-list-size.too-large",
              "&cYou must input at most &e{0} &centries for &e{1}&c.",
              fmt(e.maximum()),
              parameter.name()));
    }
  }

  /**
   * Sends a localized error message when a string argument is too short or too long.
   *
   * <ul>
   *   <li>Too short: {@code commands.invalid-string-size.too-small}
   *   <li>Too long: {@code commands.invalid-string-size.too-large}
   * </ul>
   */
  @Override
  public void onInvalidStringSize(
      @NotNull final InvalidStringSizeException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, ?> parameter) {
    if (e.input().length() < e.minimum()) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.invalid-string-size.too-small",
              "&cParameter &e{0} &cmust be at least &e{1} &ccharacters long.",
              parameter.name(),
              fmt(e.minimum())));
    }
    if (e.input().length() > e.maximum()) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.invalid-string-size.too-large",
              "&cParameter &e{0} &ccan be at most &e{1} &ccharacters long.",
              parameter.name(),
              fmt(e.maximum())));
    }
  }

  /**
   * Sends a localized error message when input cannot be parsed as a boolean.
   *
   * <p>Locale key: {@code commands.invalid-boolean}
   */
  @Override
  public void onInvalidBoolean(
      @NotNull final InvalidBooleanException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.invalid-boolean",
            "&cExpected &etrue &cor &efalse&c, found &e{0}&c.",
            e.input()));
  }

  /**
   * Sends a localized error message when input cannot be parsed as a decimal number.
   *
   * <p>Locale key: {@code commands.invalid-decimal}
   */
  @Override
  public void onInvalidDecimal(
      @NotNull final InvalidDecimalException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.invalid-decimal", "&cInvalid number: &e{0}&c.", e.input()));
  }

  /**
   * Sends a localized error message when input cannot be parsed as an integer.
   *
   * <p>Locale key: {@code commands.invalid-integer}
   */
  @Override
  public void onInvalidInteger(
      @NotNull final InvalidIntegerException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.invalid-integer", "&cInvalid integer: &e{0}&c.", e.input()));
  }

  /**
   * Sends a localized error message when input cannot be parsed as a UUID.
   *
   * <p>Locale key: {@code commands.invalid-uuid}
   */
  @Override
  public void onInvalidUUID(
      @NotNull final InvalidUUIDException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.invalid-uuid", "&cInvalid UUID: &e{0}&c.", e.input()));
  }

  /**
   * Sends a localized error message when a required command argument is missing.
   *
   * <p>Locale key: {@code commands.missing-argument}
   */
  @Override
  public void onMissingArgument(
      @NotNull final MissingArgumentException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, ?> parameter) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.missing-argument",
            "&cRequired parameter is missing: &e{0}&c. Usage: &e/{1}&c.",
            parameter.name(),
            parameter.command().usage()));
  }

  /**
   * Sends a localized error message when the sender lacks permission to run the command.
   *
   * <p>Locale key: {@code commands.no-permission}
   */
  @Override
  public void onNoPermission(
      @NotNull final NoPermissionException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.no-permission",
            "&cYou do not have permission to execute this command!"));
  }

  /**
   * Sends a localized error message when a numeric argument falls outside its allowed range.
   *
   * <ul>
   *   <li>Below minimum: {@code commands.number-not-in-range.too-small}
   *   <li>Above maximum: {@code commands.number-not-in-range.too-large}
   * </ul>
   */
  @Override
  public void onNumberNotInRange(
      @NotNull final NumberNotInRangeException e,
      @NotNull final BukkitCommandActor actor,
      @NotNull final ParameterNode<BukkitCommandActor, Number> parameter) {
    if (e.input().doubleValue() < e.minimum()) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.number-not-in-range.too-small",
              "&c{0} too small &e({1})&c. Must be at least &e{2}&c.",
              parameter.name(),
              fmt(e.input()),
              fmt(e.minimum())));
    }
    if (e.input().doubleValue() > e.maximum()) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.number-not-in-range.too-large",
              "&c{0} too large &e({1})&c. Must be at most &e{2}&c.",
              parameter.name(),
              fmt(e.input()),
              fmt(e.maximum())));
    }
  }

  /**
   * Sends a localized error message for an out-of-range help page number. Uses {@code
   * commands.invalid-help-page.single} when there is only one page, otherwise {@code
   * commands.invalid-help-page.multiple}.
   */
  @Override
  public void onInvalidHelpPage(
      @NotNull final InvalidHelpPageException e, @NotNull final BukkitCommandActor actor) {
    if (e.numberOfPages() == 1) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.invalid-help-page.single",
              "&cInvalid help page: &e{0}&c. Must be 1.",
              e.page()));
    } else {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.invalid-help-page.multiple",
              "&cInvalid help page: &e{0}&c. Must be between &e1 &cand &e{1}&c.",
              e.page(),
              e.numberOfPages()));
    }
  }

  /**
   * Sends a localized error message when the input does not match any known command.
   *
   * <p>Locale key: {@code commands.unknown-command}
   */
  @Override
  public void onUnknownCommand(
      @NotNull final UnknownCommandException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(), "commands.unknown-command", "&cUnknown command: &e{0}&c.", e.input()));
  }

  /**
   * Sends a localized error message when the provided value is not in the set of allowed values.
   *
   * <p>Locale key: {@code commands.value-not-allowed}
   */
  @Override
  public void onValueNotAllowed(
      @NotNull final ValueNotAllowedException e, @NotNull final BukkitCommandActor actor) {
    final String allowedValues = String.join("&c, &e", e.allowedValues());
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.value-not-allowed",
            "&cInvalid value: &e{0}&c. Allowed: &e{1}&c.",
            e.input(),
            allowedValues));
  }

  /**
   * Sends a localized internal error message and logs the underlying cause when command execution
   * throws an unexpected exception.
   *
   * <p>Locale key: {@code commands.internal-error}
   */
  @Override
  public void onCommandInvocation(
      @NotNull final CommandInvocationException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.internal-error",
            "&cAn error occurred while executing this command. Please contact an administrator."));

    Common.error(e.cause(), "Failed to execute command.", false);
  }

  /**
   * Sends a localized error message for an unrecognized flag or shorthand parameter. Uses {@code
   * commands.unknown-parameter.shorthand} for shorthand flags, otherwise {@code
   * commands.unknown-parameter.flag}.
   */
  @Override
  public void onUnknownParameter(
      @NotNull final UnknownParameterException e, @NotNull final BukkitCommandActor actor) {
    if (e.shorthand()) {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.unknown-parameter.shorthand",
              "&cUnknown shorthand flag: &e{0}&c.",
              e.name()));
    } else {
      Text.tell(
          actor.sender(),
          Text.localizedOrDefault(
              actor.sender(),
              "commands.unknown-parameter.flag",
              "&cUnknown flag: &e{0}&c.",
              e.name()));
    }
  }

  /**
   * Sends a localized error message when a command is on cooldown, including the remaining wait
   * time formatted with the concise duration formatter.
   *
   * <p>Locale key: {@code commands.cooldown}
   */
  @Override
  public void onCooldown(
      @NotNull final CooldownException e, @NotNull final BukkitCommandActor actor) {
    Text.tell(
        actor.sender(),
        Text.localizedOrDefault(
            actor.sender(),
            "commands.cooldown",
            "&cYou must wait &e{0} &cbefore using this command again.",
            Time.formatDuration(DurationFormatter.CONCISE, e.getTimeLeftMillis())));
  }
}
