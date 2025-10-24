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

import static revxrsal.commands.util.Collections.filter;

import dev.demeng.pluginbase.text.StringSimilarity;
import dev.demeng.pluginbase.text.Text;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.Potential;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.node.FailureHandler;
import revxrsal.commands.stream.StringStream;

/**
 * Custom failure handler for Lamp, to be set in dispatcher settings. Provides localized error
 * messages and best matches using PluginBase's utilities.
 */
public final class BaseFailureHandler<A extends CommandActor> implements FailureHandler<A> {

  private static final BaseFailureHandler<CommandActor> INSTANCE = new BaseFailureHandler<>();

  @SuppressWarnings("unchecked")
  public static <A extends CommandActor> FailureHandler<A> baseFailureHandler() {
    return (FailureHandler<A>) INSTANCE;
  }

  @Override
  public void handleFailedAttempts(
      @NotNull final A actor,
      @NotNull @Unmodifiable final List<Potential<A>> failedAttempts,
      @NotNull final StringStream input) {

    if (failedAttempts.isEmpty()) {
      return;
    }

    if (failedAttempts.size() == 1) {
      failedAttempts.get(0).handleException();
      return;
    }

    final List<Potential<A>> realExceptions =
        filter(failedAttempts, v -> !(v.error() instanceof ExpectedLiteralException));

    if (realExceptions.isEmpty()) {
      if (!(actor instanceof final BukkitCommandActor bukkitActor)) {
        return;
      }

      final String userInput = input.source();

      failedAttempts.stream()
          .map(potential -> potential.context().command().path())
          .max(Comparator.comparingDouble(cmd -> StringSimilarity.similarity(userInput, cmd)))
          .ifPresent(
              bestMatch ->
                  Text.tell(
                      bukkitActor.sender(),
                      Text.localizedOrDefault(
                          bukkitActor.sender(),
                          "commands.failed-resolve",
                          "&cUnknown command: &e/{0}&c. Did you mean &e/{1}&c?",
                          userInput,
                          bestMatch)));

    } else {
      realExceptions.get(0).handleException();
    }
  }
}
