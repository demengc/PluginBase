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

package dev.demeng.pluginbase.commands.bukkit.core;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.autocomplete.SuggestionProvider;
import dev.demeng.pluginbase.commands.bukkit.BukkitBrigadier;
import dev.demeng.pluginbase.commands.bukkit.BukkitCommandActor;
import dev.demeng.pluginbase.commands.bukkit.BukkitCommandHandler;
import dev.demeng.pluginbase.commands.bukkit.adventure.AudienceSenderResolver;
import dev.demeng.pluginbase.commands.bukkit.adventure.ComponentResponseHandler;
import dev.demeng.pluginbase.commands.bukkit.brigadier.CommodoreBukkitBrigadier;
import dev.demeng.pluginbase.commands.bukkit.core.EntitySelectorResolver.SelectorSuggestionFactory;
import dev.demeng.pluginbase.commands.bukkit.exception.BukkitExceptionAdapter;
import dev.demeng.pluginbase.commands.bukkit.exception.InvalidPlayerException;
import dev.demeng.pluginbase.commands.bukkit.exception.InvalidWorldException;
import dev.demeng.pluginbase.commands.bukkit.exception.MalformedEntitySelectorException;
import dev.demeng.pluginbase.commands.bukkit.exception.MoreThanOnePlayerException;
import dev.demeng.pluginbase.commands.bukkit.exception.NonPlayerEntitiesException;
import dev.demeng.pluginbase.commands.command.CommandCategory;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.core.BaseCommandHandler;
import dev.demeng.pluginbase.commands.core.CommandPath;
import dev.demeng.pluginbase.commands.exception.EnumNotFoundException;
import dev.demeng.pluginbase.commands.util.Preconditions;
import dev.demeng.pluginbase.commands.util.Primitives;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import net.kyori.adventure.text.ComponentLike;
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

public final class BukkitHandler extends BaseCommandHandler implements BukkitCommandHandler {

  public static final SuggestionProvider playerSuggestionProvider = (args, sender, command) -> Bukkit.getOnlinePlayers()
      .stream()
      .filter(player -> !((BukkitCommandActor) sender).isPlayer()
          || ((BukkitCommandActor) sender).requirePlayer().canSee(player))
      .map(HumanEntity::getName)
      .collect(Collectors.toList());

  private final Plugin plugin;
  private Optional<BukkitBrigadier> brigadier;

  @SuppressWarnings("rawtypes")
  public BukkitHandler(@NotNull final Plugin plugin) {
    super();
    this.plugin = Preconditions.notNull(plugin, "plugin");
    try {
      brigadier = Optional.of(new CommodoreBukkitBrigadier(this));
    } catch (final NoClassDefFoundError e) {
      brigadier = Optional.empty();
    }
    registerSenderResolver(BukkitSenderResolver.INSTANCE);
    registerValueResolver(Player.class, context -> {
      final String value = context.pop();
      if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
        return ((BukkitCommandActor) context.actor()).requirePlayer();
      }
      if (EntitySelectorResolver.INSTANCE.supportsComplexSelectors()) {
        try {
          final List<Entity> entityList = Bukkit.selectEntities(
              ((BukkitActor) context.actor()).getSender(), value);
          if (entityList.stream().anyMatch(c -> !(c instanceof Player))) {
            throw new NonPlayerEntitiesException(value);
          }
          if (entityList.size() != 1) {
            throw new MoreThanOnePlayerException(value);
          }
          return (Player) entityList.get(0);
        } catch (final IllegalArgumentException e) {
          throw new MalformedEntitySelectorException(context.actor(), value,
              e.getCause().getMessage());
        }
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
        return ((BukkitCommandActor) context.actor()).requirePlayer();
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
        return ((BukkitCommandActor) context.actor()).requirePlayer().getWorld();
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
    if (EntitySelectorResolver.INSTANCE.supportsComplexSelectors() && isBrigadierSupported()) {
      getAutoCompleter().registerParameterSuggestions(EntityType.class, SuggestionProvider.EMPTY);
    }
    registerValueResolverFactory(EntitySelectorResolver.INSTANCE);
    if (!isBrigadierSupported()) {
      getAutoCompleter().registerParameterSuggestions(Player.class, playerSuggestionProvider);
    }
    getAutoCompleter().registerSuggestion("players", playerSuggestionProvider);
    //noinspection Convert2MethodRef
    getAutoCompleter()
        // Cannot be a method reference since WorldInfo does not exist in older versions.
        .registerSuggestion("worlds",
            SuggestionProvider.map(Bukkit::getWorlds, world -> world.getName()))
        .registerParameterSuggestions(Player.class, "players")
        .registerParameterSuggestions(World.class, "worlds")
        .registerSuggestionFactory(SelectorSuggestionFactory.INSTANCE);
    registerContextValue((Class) plugin.getClass(), plugin);
    registerDependency((Class) plugin.getClass(), plugin);
    registerDependency(FileConfiguration.class, (Supplier<FileConfiguration>) plugin::getConfig);
    registerDependency(Logger.class, (Supplier<Logger>) plugin::getLogger);
    registerPermissionReader(BukkitPermissionReader.INSTANCE);
    setExceptionHandler(BukkitExceptionAdapter.INSTANCE);
    Bukkit.getServer().getPluginManager().registerEvents(new BukkitCommandListeners(this), plugin);
    registerSenderResolver(new AudienceSenderResolver(BaseManager.getAdventure()));
    registerResponseHandler(ComponentLike.class,
        new ComponentResponseHandler(BaseManager.getAdventure()));
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
  public @NotNull Optional<BukkitBrigadier> getBrigadier() {
    return brigadier;
  }

  @Override
  public boolean isBrigadierSupported() {
    return brigadier.isPresent();
  }

  @Override
  public BukkitCommandHandler registerBrigadier() {
    brigadier.ifPresent(BukkitBrigadier::register);
    return this;
  }

  @Override
  public @NotNull Plugin getPlugin() {
    return plugin;
  }

  private @SneakyThrows void createPluginCommand(final String name,
      @Nullable final String description,
      @Nullable final String usage) {
    final PluginCommand cmd = COMMAND_CONSTRUCTOR.newInstance(name, plugin);
    COMMAND_MAP.register(plugin.getName(), cmd);
    final BukkitCommandExecutor executor = new BukkitCommandExecutor(this);
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
        if (rawAlias instanceof PluginCommand && ((PluginCommand) rawAlias).getPlugin() == plugin) {
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
