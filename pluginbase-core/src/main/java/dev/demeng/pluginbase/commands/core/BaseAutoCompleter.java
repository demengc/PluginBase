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

package dev.demeng.pluginbase.commands.core;

import static dev.demeng.pluginbase.commands.util.Collections.listOf;
import static dev.demeng.pluginbase.commands.util.Preconditions.coerceIn;
import static dev.demeng.pluginbase.commands.util.Preconditions.notNull;
import static java.util.Collections.emptyList;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.autocomplete.AutoCompleter;
import dev.demeng.pluginbase.commands.autocomplete.SuggestionProvider;
import dev.demeng.pluginbase.commands.autocomplete.SuggestionProviderFactory;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandCategory;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.util.Primitives;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

final class BaseAutoCompleter implements AutoCompleter {

  private final BaseCommandHandler handler;
  final Map<String, SuggestionProvider> suggestionKeys = new HashMap<>();
  final LinkedList<SuggestionProviderFactory> factories = new LinkedList<>();

  public BaseAutoCompleter(final BaseCommandHandler handler) {
    this.handler = handler;
    registerSuggestion("nothing", SuggestionProvider.EMPTY);
    registerSuggestion("empty", SuggestionProvider.EMPTY);
    registerParameterSuggestions(boolean.class, SuggestionProvider.of("true", "false"));
    registerSuggestionFactory(new AutoCompleterAnnotationFactory(suggestionKeys));
  }

  @Override
  public AutoCompleter registerSuggestion(@NotNull final String providerID,
      @NotNull final SuggestionProvider provider) {
    notNull(provider, "provider ID");
    notNull(provider, "tab suggestion provider");
    suggestionKeys.put(providerID, provider);
    return this;
  }

  @Override
  public AutoCompleter registerSuggestion(@NotNull final String providerID,
      @NotNull final Collection<String> completions) {
    notNull(providerID, "provider ID");
    notNull(completions, "completions");
    suggestionKeys.put(providerID, (args, sender, command) -> completions);
    return this;
  }

  @Override
  public AutoCompleter registerSuggestion(@NotNull final String providerID,
      @NotNull final String... completions) {
    registerSuggestion(providerID, listOf(completions));
    return this;
  }

  @Override
  public AutoCompleter registerParameterSuggestions(@NotNull final Class<?> parameterType,
      @NotNull final SuggestionProvider provider) {
    notNull(parameterType, "parameter type");
    notNull(provider, "provider");
    registerSuggestionFactory(SuggestionProviderFactory.forType(parameterType, provider));
    final Class<?> wrapped = Primitives.wrap(parameterType);
    if (wrapped != parameterType) {
      registerSuggestionFactory(SuggestionProviderFactory.forType(wrapped, provider));
    }
    return this;
  }

  @Override
  public AutoCompleter registerParameterSuggestions(@NotNull final Class<?> parameterType,
      @NotNull final String providerID) {
    notNull(parameterType, "parameter type");
    notNull(providerID, "provider ID");
    final SuggestionProvider provider = suggestionKeys.get(providerID);
    if (provider == null) {
      throw new IllegalArgumentException(
          "No such tab provider: " + providerID + ". Available: " + suggestionKeys.keySet());
    }
    registerParameterSuggestions(parameterType, provider);
    return this;
  }

  @Override
  public AutoCompleter registerSuggestionFactory(@NotNull final SuggestionProviderFactory factory) {
    notNull(factory, "suggestion provider factory cannot be null!");
    factories.add(factory);
    return this;
  }

  @Override
  public AutoCompleter registerSuggestionFactory(final int priority,
      @NotNull final SuggestionProviderFactory factory) {
    notNull(factory, "suggestion provider factory cannot be null!");
    factories.add(coerceIn(priority, 0, factories.size()), factory);
    return this;
  }

  public SuggestionProvider getProvider(final CommandParameter parameter) {
    if (parameter.isSwitch()) {
      return SuggestionProvider.of(handler.switchPrefix + parameter.getSwitchName());
    }
    for (final SuggestionProviderFactory factory : factories) {
      final SuggestionProvider provider = factory.createSuggestionProvider(parameter);
      if (provider == null) {
        continue;
      }
      return provider;
    }
    if (parameter.getType().isEnum()) {
      return EnumSuggestionProviderFactory.INSTANCE.createSuggestionProvider(parameter);
    }
    return SuggestionProvider.EMPTY;
  }

  @Override
  public SuggestionProvider getSuggestionProvider(@NotNull final String id) {
    return suggestionKeys.get(id);
  }

  @Override
  public List<String> complete(@NotNull final CommandActor actor,
      @NotNull final ArgumentStack arguments) {
    final CommandPath path = CommandPath.get(arguments.subList(0, arguments.size() - 1));
    final int originalSize = arguments.size();
    final ExecutableCommand command = searchForCommand(path, actor);
    if (command != null) {
      command.getPath().forEach(c -> arguments.removeFirst());
      return getCompletions(actor, arguments, command);
    }
    final CommandCategory category = getLastCategory(path);
    if (category == null) {
      return emptyList();
    }

    category.getPath().forEach(c -> arguments.removeFirst());
    return getCompletions(actor, arguments, category, originalSize);
  }

  @Override
  public List<String> complete(@NotNull final CommandActor actor, @NotNull final String buffer) {
    return complete(actor, handler.parseArgumentsForCompletion(buffer));
  }

  private ExecutableCommand searchForCommand(final CommandPath path, final CommandActor actor) {
    ExecutableCommand found = handler.getCommand(path);
    if (found != null && !found.isSecret() && found.getPermission().canExecute(actor)) {
      return found;
    }
    final MutableCommandPath mpath = MutableCommandPath.empty();
    for (final String p : path) {
      mpath.add(p);
      found = handler.getCommand(mpath);
      if (found != null && !found.isSecret() && found.getPermission().canExecute(actor)) {
        return found;
      }
    }
    return null;
  }

  private CommandCategory getLastCategory(final CommandPath path) {
    final MutableCommandPath mpath = MutableCommandPath.empty();
    CommandCategory category = null;
    for (final String p : path) {
      mpath.add(p);
      final CommandCategory c = handler.getCategory(mpath);
      if (c == null && category != null) {
        return category;
      }
      if (c != null) {
        category = c;
      }
    }
    return category;
  }

  @SneakyThrows
  private List<String> getCompletions(final CommandActor actor,
      final ArgumentStack args,
      @NotNull final ExecutableCommand command) {
    try {
      if (args.isEmpty()) {
        return emptyList();
      }
      if (command.getValueParameters().isEmpty()) {
        return emptyList();
      }
      final List<CommandParameter> parameters = new ArrayList<>(
          command.getValueParameters().values());
      Collections.sort(parameters);
      for (final CommandParameter parameter : parameters) {
        try {
          if (parameter.isFlag()) {
            continue;
          }
          if (parameter.getCommandIndex() == args.size() - 1) {
            if (!parameter.getPermission().canExecute(actor)) {
              return emptyList();
            }
            final SuggestionProvider provider = parameter.getSuggestionProvider();
            notNull(provider, "provider must not be null!");
            return getParamCompletions(provider.getSuggestions(args, actor, command), args);
          }
        } catch (final Throwable ignored) {
        }
      }
      parameters.removeIf(c -> !c.isFlag());
      if (parameters.isEmpty()) {
        return emptyList();
      }
      final Optional<CommandParameter> currentFlag = parameters.stream().filter(c -> {
        final int index = args.indexOf(handler.getFlagPrefix() + c.getFlagName());
        return index == args.size() - 2;
      }).findFirst();
      if (currentFlag.isPresent()) {
        final SuggestionProvider provider = currentFlag.get().getSuggestionProvider();
        return getParamCompletions(provider.getSuggestions(args, actor, command), args);
      }
      for (final CommandParameter flag : parameters) {
        final int index = args.indexOf(handler.getFlagPrefix() + flag.getFlagName());
        if (index == -1) {
          return listOf(handler.getFlagPrefix() + flag.getFlagName());
        } else if (index == args.size() - 2) {
          return getParamCompletions(
              flag.getSuggestionProvider().getSuggestions(args, actor, command), args);
        }
      }
      return emptyList();
    } catch (final IndexOutOfBoundsException e) {
      return emptyList();
    }
  }

  @NotNull
  private List<String> getParamCompletions(final Collection<String> provider,
      final ArgumentStack args) {
    return provider
        .stream()
        .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .distinct()
        .collect(Collectors.toList());
  }

  private List<String> getCompletions(final CommandActor actor,
      @Unmodifiable final ArgumentStack args,
      final CommandCategory category, final int originalSize) {
    if (args.isEmpty()) {
      return emptyList();
    }
    final Set<String> suggestions = new HashSet<>();
    if (category.getDefaultAction() != null) {
      suggestions.addAll(getCompletions(actor, args, category.getDefaultAction()));
    }
    if (originalSize - category.getPath().size() == 1) {
      category.getCommands().values().forEach(c -> {
        if (!c.isSecret() && c.getPermission().canExecute(actor)) {
          suggestions.add(c.getName());
        }
      });
      category.getCategories().values().forEach(c -> {
        if (!c.isSecret() && c.getPermission().canExecute(actor)) {
          suggestions.add(c.getName());
        }
      });
    }
    return getParamCompletions(suggestions, args);
  }

  @Override
  public CommandHandler and() {
    return handler;
  }
}
