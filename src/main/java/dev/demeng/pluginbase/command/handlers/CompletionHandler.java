/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2019 Matt
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

package dev.demeng.pluginbase.command.handlers;

import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.command.exceptions.CustomCommandException;
import dev.demeng.pluginbase.command.resolvers.CompletionResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Handles command tab-completion and maps provided arguments to an auto-completion list. Primarily
 * for internal use.
 */
public final class CompletionHandler {

  private final Map<String, CompletionResolver> registeredCompletions = new HashMap<>();

  /** Registers all the default completions. */
  public CompletionHandler() {
    register(
        "#players",
        input -> {
          final List<String> players = new ArrayList<>();
          for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
          }
          players.sort(String.CASE_INSENSITIVE_ORDER);
          return players;
        });
    register("#empty", input -> Collections.singletonList(""));
    register(
        "#range",
        input -> {
          final String s = String.valueOf(input);

          if (s.contains("class"))
            return IntStream.rangeClosed(1, 10)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());

          if (!s.contains("-"))
            return IntStream.rangeClosed(1, Integer.parseInt(s))
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());

          final String[] minMax = s.split("-");
          final int[] range =
              IntStream.rangeClosed(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]))
                  .toArray();

          final List<String> rangeList = new ArrayList<>();

          for (int number : range) {
            rangeList.add(String.valueOf(number));
          }

          return rangeList;
        });
    register(
        "#enum",
        input -> {
          // noinspection unchecked
          final Class<? extends Enum<?>> enumCls = (Class<? extends Enum<?>>) input;
          final List<String> values = new ArrayList<>();

          for (Enum<?> enumValue : enumCls.getEnumConstants()) {
            values.add(enumValue.name());
          }

          values.sort(String.CASE_INSENSITIVE_ORDER);
          return values;
        });
    register("#boolean", input -> Arrays.asList("false", "true"));
  }

  /**
   * Registers a new completion.
   *
   * @param completionId The ID of the completion to register
   * @param completionResolver A function with the result you want
   */
  public void register(String completionId, CompletionResolver completionResolver) {

    if (!completionId.startsWith("#")) {
      throw new CustomCommandException(
          String.format("Completion ID %s must start with '#'", completionId));
    }

    registeredCompletions.put(completionId, completionResolver);
  }

  /**
   * Gets the values from the registered functions.
   *
   * @param completionId The completion ID to use
   * @param input The input the output will be resolved from, typically not needed
   * @return The string list with all the completions
   */
  List<String> getTypeResult(String completionId, Object input) {
    return ChatUtils.colorize(registeredCompletions.get(completionId).resolve(input));
  }

  /**
   * Checks if the ID is registered.
   *
   * @param id The ID to check
   * @return True if registered, false otherwise
   */
  boolean isNotRegistered(String id) {
    String identifier = id;

    if (id.contains(":")) {
      identifier = identifier.split(":")[0];
    }

    return registeredCompletions.get(identifier) == null;
  }
}
