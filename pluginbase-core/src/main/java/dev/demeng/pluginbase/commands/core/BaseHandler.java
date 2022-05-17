/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

import static dev.demeng.pluginbase.commands.util.Preconditions.notNull;

import com.google.common.base.Suppliers;
import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.autocomplete.SuggestionProvider;
import dev.demeng.pluginbase.commands.brigadier.BaseBrigadier;
import dev.demeng.pluginbase.commands.brigadier.BrigadierTreeParser;
import dev.demeng.pluginbase.commands.command.CommandCategory;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.core.EntitySelectorResolver.SelectorSuggestionFactory;
import dev.demeng.pluginbase.commands.exception.DefaultExceptionHandler;
import dev.demeng.pluginbase.commands.exception.EnumNotFoundException;
import dev.demeng.pluginbase.commands.exception.InvalidPlayerException;
import dev.demeng.pluginbase.commands.exception.InvalidWorldException;
import dev.demeng.pluginbase.commands.process.PermissionReader;
import dev.demeng.pluginbase.commands.util.Primitives;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BaseHandler extends BaseCommandHandler implements CommandHandler {

  private final Plugin plugin;

  @SuppressWarnings("Guava")
  // old guava versions would throw an error as they do not implement Java's Supplier.
  private final com.google.common.base.Supplier<Optional<BaseBrigadier>> brigadier = Suppliers.memoize(
      () -> {
        if (!CommodoreProvider.isSupported()) {
          return Optional.empty();
        }
        return Optional.of(new BaseBrigadier(CommodoreProvider.getCommodore(getPlugin()), this));
      });

  @SuppressWarnings("rawtypes")
  public BaseHandler(@NotNull final Plugin plugin) {
    super();
    this.plugin = notNull(plugin, "plugin");
    registerSenderResolver(BaseSenderResolver.INSTANCE);
    registerValueResolver(Player.class, context -> {
      final String value = context.pop();
      if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
        return context.actor().requirePlayer();
      }
      final Player player = Bukkit.getPlayerExact(value);
      if (player == null) {
        throw new InvalidPlayerException(context.parameter(), value);
      }
      return player;
    });
    registerValueResolver(OfflinePlayer.class, context -> {
      final String value = context.pop();
      if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
        return context.actor().requirePlayer();
      }
      final OfflinePlayer player = Bukkit.getOfflinePlayer(value);
      if (!player.hasPlayedBefore()) {
        throw new InvalidPlayerException(context.parameter(), value);
      }
      return player;
    });
    registerValueResolver(World.class, context -> {
      final String value = context.pop();
      if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
        return context.actor().requirePlayer().getWorld();
      }
      final World world = Bukkit.getWorld(value);
      if (world == null) {
        throw new InvalidWorldException(context.parameter(), value);
      }
      return world;
    });
    registerValueResolver(EntityType.class, context -> {
      String value = context.pop().toLowerCase();
      if (value.startsWith("minecraft:")) {
        value = value.substring("minecraft:".length());
      }
      final EntityType type = EntityType.fromName(value);
      if (type == null) {
        throw new EnumNotFoundException(context.parameter(), value);
      }
      return type;
    });
    if (EntitySelectorResolver.INSTANCE.supportsComplexSelectors() && brigadier.get()
        .isPresent()) {
      getAutoCompleter().registerParameterSuggestions(EntityType.class,
          SuggestionProvider.EMPTY);
    }
    registerValueResolverFactory(EntitySelectorResolver.INSTANCE);
    getAutoCompleter().registerSuggestion("players",
        (args, sender, command) -> Bukkit.getOnlinePlayers()
            .stream()
            .filter(player -> !sender.isPlayer()
                || sender.requirePlayer().canSee(player))
            .map(HumanEntity::getName)
            .collect(Collectors.toList()));
    getAutoCompleter()
        .registerSuggestion("worlds", SuggestionProvider.map(Bukkit::getWorlds, World::getName))
        .registerParameterSuggestions(Player.class, "players")
        .registerParameterSuggestions(World.class, "worlds")
        .registerSuggestionFactory(SelectorSuggestionFactory.INSTANCE);
    registerContextValue((Class) plugin.getClass(), plugin);
    registerDependency((Class) plugin.getClass(), plugin);
    registerDependency(FileConfiguration.class, (Supplier<FileConfiguration>) plugin::getConfig);
    registerDependency(Logger.class, plugin.getLogger());
    registerPermissionReader(PermissionReader.INSTANCE);
    setExceptionHandler(DefaultExceptionHandler.INSTANCE);
  }

  @Override
  public @NotNull CommandHandler register(@NotNull final Object... commands) {
    super.register(commands);
    for (final ExecutableCommand command : executables.values()) {
      if (command.getParent() != null) {
        continue;
      }
      createPluginCommand(command.getName(), command.getDescription(), command.getUsage());
    }
    for (final CommandCategory category : categories.values()) {
      if (category.getParent() != null) {
        continue;
      }
      createPluginCommand(category.getName(), null, null);
    }
    return this;
  }

  @Override
  public CommandHandler registerBrigadier() {
    brigadier.get().ifPresent(brigadier -> BrigadierTreeParser
        .parse(brigadier, this)
        .forEach(brigadier::register));
    return this;
  }

  @Override
  public @NotNull Plugin getPlugin() {
    return plugin;
  }

  private @SneakyThrows
  void createPluginCommand(final String name, @Nullable final String description,
      @Nullable final String usage) {
    final PluginCommand cmd = COMMAND_CONSTRUCTOR.newInstance(name, plugin);
    COMMAND_MAP.register(plugin.getName(), cmd);
    final BaseTabExecutor executor = new BaseTabExecutor(this);
    cmd.setExecutor(executor);
    cmd.setTabCompleter(executor);
    cmd.setDescription(description == null ? "" : description);
    if (usage != null) {
      cmd.setUsage(usage);
    }
  }

  @Override
  public boolean unregister(@NotNull final CommandPath path) {
    if (path.isRoot()) {
      final PluginCommand command = ((JavaPlugin) plugin).getCommand(path.getFirst());
      unregisterCommand(command);
    }
    return super.unregister(path);
  }

  private void unregisterCommand(final PluginCommand command) {
    if (command != null) {
      command.unregister(COMMAND_MAP);
      final Map<String, Command> knownCommands = getKnownCommands();
      if (knownCommands != null) {
        final Command rawAlias = knownCommands.get(command.getName());
        if (rawAlias instanceof PluginCommand
            && ((PluginCommand) rawAlias).getPlugin() == plugin) {
          knownCommands.remove(command.getName());
        }
        knownCommands.remove(plugin.getDescription().getName() + ":" + command.getName());
      }
    }
  }

  private static final Constructor<PluginCommand> COMMAND_CONSTRUCTOR;
  private static final @Nullable Field KNOWN_COMMANDS;
  private static final CommandMap COMMAND_MAP;

  static {
    final Constructor<PluginCommand> ctr;
    Field knownCommands = null;
    final CommandMap commandMap;
    try {
      ctr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
      ctr.setAccessible(true);
      final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
      commandMapField.setAccessible(true);
      commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
      if (commandMap instanceof SimpleCommandMap) {
        knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommands.setAccessible(true);
      }
    } catch (final NoSuchMethodException e) {
      throw new IllegalStateException("Unable to access PluginCommand(String, Plugin) construtor!");
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to access Bukkit.getServer()#commandMap!");
    }
    COMMAND_CONSTRUCTOR = ctr;
    COMMAND_MAP = commandMap;
    KNOWN_COMMANDS = knownCommands;
  }

  public static Class<? extends Entity> getSelectedEntity(@NotNull final Type selectorType) {
    return (Class<? extends Entity>) Primitives.getInsideGeneric(selectorType, Entity.class);
  }

  @SneakyThrows
  private static @Nullable Map<String, Command> getKnownCommands() {
    if (KNOWN_COMMANDS != null) {
      return (Map<String, Command>) KNOWN_COMMANDS.get(COMMAND_MAP);
    }
    return null;

  }
}
