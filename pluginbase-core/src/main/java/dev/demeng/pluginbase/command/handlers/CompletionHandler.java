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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles command tab-completion and maps provided arguments to an auto-completion list. A list of
 * default completion IDs/shortcuts can be found below.
 * <ul>
 *   <li>#players - A list of online players.</li>
 *   <li>#boolean - True and false.</li>
 *   <li>#empty - Empty completion.</li>
 *   <li>#enum - All values of the enum inside your command method parameters.</li>
 *   <li>#range - A range of numbers.
 *   <ul>
 *     <li>#range:20 - Returns a list of numbers from 1 through 20 (inclusive).</li>
 *     <li>#range:0-5 - Returns a list of numbers from 0 through 5 (inclusive).</li>
 *     <li>#range - Return a list of numbers from 1 through 0 (inclusive).</li>
 *   </ul></li>
 * </ul>
 */
public final class CompletionHandler {

  private final Map<String, CompletionResolver> registeredCompletions = new HashMap<>();

  /**
   * Registers all the default completions.
   */
  public CompletionHandler() {

    register("#empty", input -> Collections.emptyList());

    register("#boolean", input -> Arrays.asList("false", "true"));

    register(
        "#players",
        input -> {
          final List<String> players = new ArrayList<>();

          for (final Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
          }

          players.sort(String.CASE_INSENSITIVE_ORDER);
          return players;
        });

    register(
        "#enum",
        input -> {
          // noinspection unchecked
          final Class<? extends Enum<?>> enumCls = (Class<? extends Enum<?>>) input;
          final List<String> values = new ArrayList<>();

          for (final Enum<?> enumValue : enumCls.getEnumConstants()) {
            values.add(enumValue.name());
          }

          values.sort(String.CASE_INSENSITIVE_ORDER);
          return values;
        });

    register("#list", input -> Arrays.asList(String.valueOf(input).split("\\|")));

    register(
        "#range",
        input -> {
          final String str = String.valueOf(input);

          if (str.contains("class")) {
            return IntStream.rangeClosed(1, 10)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
          }

          if (!str.contains("-")) {
            return IntStream.rangeClosed(1, Integer.parseInt(str))
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
          }

          final String[] minMax = str.split("-");
          final int[] range =
              IntStream.rangeClosed(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]))
                  .toArray();

          final List<String> rangeList = new ArrayList<>();

          for (final int number : range) {
            rangeList.add(String.valueOf(number));
          }

          return rangeList;
        });
  }

  /**
   * Registers a new completion.
   *
   * @param completionId       The ID of the completion to register
   * @param completionResolver A function with the result you want
   */
  public void register(@NotNull final String completionId,
      @NotNull final CompletionResolver completionResolver) {

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
   * @param input        The input the output will be resolved from, typically not needed
   * @return The string list with all the completions
   */
  @NotNull
  public List<String> getTypeResult(@NotNull final String completionId,
      @NotNull final Object input) {
    return ChatUtils.colorize(registeredCompletions.get(completionId).resolve(input));
  }

  /**
   * Checks if the ID is registered.
   *
   * @param id The ID to check
   * @return True if registered, false otherwise
   */
  public boolean isRegistered(@NotNull final String id) {
    String identifier = id;

    if (id.contains(":")) {
      identifier = identifier.split(":")[0];
    }

    return registeredCompletions.get(identifier) != null;
  }
}
