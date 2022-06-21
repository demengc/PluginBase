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

package dev.demeng.pluginbase.commands.autocomplete;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A provider for tab completions.
 * <p>
 * Register with {@link AutoCompleter#registerSuggestion(String, SuggestionProvider)}
 */
public interface SuggestionProvider {

  /**
   * A {@link SuggestionProvider} that always returns an empty list.
   */
  SuggestionProvider EMPTY = (args, sender, command) -> Collections.emptyList();

  /**
   * Returns the suggestions
   *
   * @param args    The command arguments
   * @param sender  The command sender
   * @param command The handled command
   * @return The command suggestions.
   */
  @NotNull
  Collection<String> getSuggestions(@NotNull List<String> args,
      @NotNull CommandActor sender,
      @NotNull ExecutableCommand command) throws Throwable;

  /**
   * Composes the two {@link SuggestionProvider}s into one provider that returns the completions
   * from both.
   *
   * @param other Other provider to merge with
   * @return The new provider
   */
  @Contract("null -> this; !null -> new")
  default SuggestionProvider compose(@Nullable final SuggestionProvider other) {
    if (other == null) {
      return this;
    }
    return (args, sender, command) -> {
      final Set<String> completions = new HashSet<>(other.getSuggestions(args, sender, command));
      completions.addAll(getSuggestions(args, sender, command));
      return completions;
    };
  }

  /**
   * Returns a {@link SuggestionProvider} that always returns the given values
   *
   * @param suggestions Values to return.
   * @return The provider
   */
  static SuggestionProvider of(@Nullable final Collection<String> suggestions) {
    if (suggestions == null) {
      return EMPTY;
    }
    return (args, sender, command) -> suggestions;
  }

  /**
   * Returns a {@link SuggestionProvider} that always returns the given values
   *
   * @param suggestions Values to return.
   * @return The provider
   */
  static SuggestionProvider of(@Nullable final String... suggestions) {
    if (suggestions == null) {
      return EMPTY;
    }
    final List<String> values = dev.demeng.pluginbase.commands.util.Collections.listOf(suggestions);
    return (args, sender, command) -> values;
  }

  /**
   * Returns a {@link SuggestionProvider} that computes the given supplier every time suggestions
   * are returned.
   *
   * @param supplier The collection supplier
   * @return The provider
   */
  static SuggestionProvider of(@NotNull final Supplier<Collection<String>> supplier) {
    return (args, sender, command) -> supplier.get();
  }

  /**
   * Returns a {@link SuggestionProvider} that takes the given collection of values and maps it to
   * strings according to the given function.
   *
   * @param values   Values to map
   * @param function Function to remap values with
   * @param <T>      The values type
   * @return The provider
   */
  static <T> SuggestionProvider map(@NotNull final Supplier<Collection<T>> values,
      final Function<T, String> function) {
    return (args, sender, command) -> values.get().stream().map(function)
        .collect(Collectors.toList());
  }
}
