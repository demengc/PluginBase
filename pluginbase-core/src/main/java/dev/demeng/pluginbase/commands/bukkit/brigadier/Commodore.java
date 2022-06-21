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

package dev.demeng.pluginbase.commands.bukkit.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

final class Commodore {

  // obc.CraftServer#console field
  private static final Field CONSOLE_FIELD;

  // nms.MinecraftServer#getCommandDispatcher method
  private static final Method GET_COMMAND_DISPATCHER_METHOD;

  // nms.CommandListenerWrapper#getBukkitSender method
  private static final Method GET_BUKKIT_SENDER_METHOD;

  // nms.CommandDispatcher#getDispatcher (obfuscated) method
  private static final Method GET_BRIGADIER_DISPATCHER_METHOD;

  // ArgumentCommandNode#customSuggestions field
  private static final Field CUSTOM_SUGGESTIONS_FIELD;

  // CommandNode#command
  private static final Field COMMAND_EXECUTE_FUNCTION_FIELD;

  // CommandNode#children, CommandNode#literals, CommandNode#arguments fields
  private static final Field CHILDREN_FIELD;
  private static final Field LITERALS_FIELD;
  private static final Field ARGUMENTS_FIELD;

  // An array of the CommandNode fields above: [#children, #literals, #arguments]
  private static final Field[] CHILDREN_FIELDS;

  static {
    try {
      final Class<?> minecraftServer;
      final Class<?> commandListenerWrapper;
      final Class<?> commandDispatcher;

      if (ReflectionUtil.minecraftVersion() > 16) {
        minecraftServer = ReflectionUtil.mcClass("server.MinecraftServer");
        commandListenerWrapper = ReflectionUtil.mcClass("commands.CommandListenerWrapper");
        commandDispatcher = ReflectionUtil.mcClass("commands.CommandDispatcher");
      } else {
        minecraftServer = ReflectionUtil.nmsClass("MinecraftServer");
        commandListenerWrapper = ReflectionUtil.nmsClass("CommandListenerWrapper");
        commandDispatcher = ReflectionUtil.nmsClass("CommandDispatcher");
      }

      final Class<?> craftServer = ReflectionUtil.obcClass("CraftServer");
      CONSOLE_FIELD = craftServer.getDeclaredField("console");
      CONSOLE_FIELD.setAccessible(true);

      GET_COMMAND_DISPATCHER_METHOD = Arrays.stream(minecraftServer.getDeclaredMethods())
          .filter(method -> method.getParameterCount() == 0)
          .filter(method -> commandDispatcher.isAssignableFrom(method.getReturnType()))
          .findFirst().orElseThrow(NoSuchMethodException::new);
      GET_COMMAND_DISPATCHER_METHOD.setAccessible(true);

      GET_BUKKIT_SENDER_METHOD = commandListenerWrapper.getDeclaredMethod("getBukkitSender");
      GET_BUKKIT_SENDER_METHOD.setAccessible(true);

      GET_BRIGADIER_DISPATCHER_METHOD = Arrays.stream(commandDispatcher.getDeclaredMethods())
          .filter(method -> method.getParameterCount() == 0)
          .filter(method -> CommandDispatcher.class.isAssignableFrom(method.getReturnType()))
          .findFirst().orElseThrow(NoSuchMethodException::new);
      GET_BRIGADIER_DISPATCHER_METHOD.setAccessible(true);

      CUSTOM_SUGGESTIONS_FIELD = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
      CUSTOM_SUGGESTIONS_FIELD.setAccessible(true);

      COMMAND_EXECUTE_FUNCTION_FIELD = CommandNode.class.getDeclaredField("command");
      COMMAND_EXECUTE_FUNCTION_FIELD.setAccessible(true);

      CHILDREN_FIELD = CommandNode.class.getDeclaredField("children");
      LITERALS_FIELD = CommandNode.class.getDeclaredField("literals");
      ARGUMENTS_FIELD = CommandNode.class.getDeclaredField("arguments");
      CHILDREN_FIELDS = new Field[]{CHILDREN_FIELD, LITERALS_FIELD, ARGUMENTS_FIELD};
      for (final Field field : CHILDREN_FIELDS) {
        field.setAccessible(true);
      }

    } catch (final ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final Plugin plugin;
  private final List<LiteralCommandNode<?>> registeredNodes = new ArrayList<>();

  Commodore(final Plugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager()
        .registerEvents(new ServerReloadListener(), this.plugin);
  }

  public CommandDispatcher<?> getDispatcher() {
    try {
      final Object mcServerObject = CONSOLE_FIELD.get(Bukkit.getServer());
      final Object commandDispatcherObject = GET_COMMAND_DISPATCHER_METHOD.invoke(mcServerObject);
      return (CommandDispatcher<?>) GET_BRIGADIER_DISPATCHER_METHOD.invoke(commandDispatcherObject);
    } catch (final ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public CommandSender getBukkitSender(final Object commandWrapperListener) {
    Objects.requireNonNull(commandWrapperListener, "commandWrapperListener");
    try {
      return (CommandSender) GET_BUKKIT_SENDER_METHOD.invoke(commandWrapperListener);
    } catch (final ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings({"rawtypes"})
  public void register(final LiteralCommandNode<?> node) {
    Objects.requireNonNull(node, "node");

    final CommandDispatcher dispatcher = getDispatcher();
    final RootCommandNode root = dispatcher.getRoot();

    removeChild(root, node.getName());
    root.addChild(node);
    registeredNodes.add(node);
  }

  public void register(final Command command, LiteralCommandNode<?> node,
      final Predicate<? super Player> permissionTest) {
    Objects.requireNonNull(command, "command");
    Objects.requireNonNull(node, "node");
    Objects.requireNonNull(permissionTest, "permissionTest");

    final Collection<String> aliases = getAliases(command);
    if (!aliases.contains(node.getLiteral())) {
      node = renameLiteralNode(node, command.getName());
    }

    for (final String alias : aliases) {
      if (node.getLiteral().equals(alias)) {
        register(node);
      } else {
        register(LiteralArgumentBuilder.literal(alias).redirect((LiteralCommandNode<Object>) node)
            .build());
      }
    }

    plugin.getServer().getPluginManager()
        .registerEvents(new CommandDataSendListener(command, permissionTest), plugin);
  }

  public void register(final Command command, final LiteralCommandNode<?> node) {
    Objects.requireNonNull(command, "command");
    Objects.requireNonNull(node, "node");

    register(command, node, command::testPermissionSilent);
  }

  @SuppressWarnings({"rawtypes"})
  private static void removeChild(final RootCommandNode root, final String name) {
    try {
      for (final Field field : CHILDREN_FIELDS) {
        final Map<String, ?> children = (Map<String, ?>) field.get(root);
        children.remove(name);
      }
    } catch (final ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static <S> LiteralCommandNode<S> renameLiteralNode(final LiteralCommandNode<S> node,
      final String newLiteral) {
    final LiteralCommandNode<S> clone = new LiteralCommandNode<>(newLiteral, node.getCommand(),
        node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());
    for (final CommandNode<S> child : node.getChildren()) {
      clone.addChild(child);
    }
    return clone;
  }

  /**
   * Listens for server (re)loads, and re-adds all registered nodes to the dispatcher.
   */
  private final class ServerReloadListener implements Listener {

    @SuppressWarnings({"rawtypes"})
    @EventHandler
    public void onLoad(final ServerLoadEvent e) {
      final CommandDispatcher dispatcher = getDispatcher();
      final RootCommandNode root = dispatcher.getRoot();

      for (final LiteralCommandNode<?> node : registeredNodes) {
        removeChild(root, node.getName());
        root.addChild(node);
      }
    }
  }

  /**
   * Removes minecraft namespaced argument data, & data for players without permission to view the
   * corresponding commands.
   */
  private static final class CommandDataSendListener implements Listener {

    private final Set<String> aliases;
    private final Set<String> minecraftPrefixedAliases;
    private final Predicate<? super Player> permissionTest;

    CommandDataSendListener(final Command pluginCommand,
        final Predicate<? super Player> permissionTest) {
      aliases = new HashSet<>(getAliases(pluginCommand));
      minecraftPrefixedAliases = aliases.stream().map(alias -> "minecraft:" + alias)
          .collect(Collectors.toSet());
      this.permissionTest = permissionTest;
    }

    @EventHandler
    public void onCommandSend(final PlayerCommandSendEvent e) {
      // always remove 'minecraft:' prefixed aliases added by craftbukkit.
      // this happens because bukkit thinks our injected commands are vanilla commands.
      e.getCommands().removeAll(minecraftPrefixedAliases);

      // remove the actual aliases if the player doesn't pass the permission test
      if (!permissionTest.test(e.getPlayer())) {
        e.getCommands().removeAll(aliases);
      }
    }
  }

  static void ensureSetup() {
    // do nothing - this is only called to trigger the static initializer
  }

  private static Collection<String> getAliases(final Command command) {
    Objects.requireNonNull(command, "command");
    Stream<String> aliasesStream = Stream.concat(
        Stream.of(command.getLabel()),
        command.getAliases().stream()
    );

    if (command instanceof PluginCommand) {
      final String fallbackPrefix = ((PluginCommand) command).getPlugin().getName().toLowerCase()
          .trim();
      aliasesStream = aliasesStream.flatMap(
          alias -> Stream.of(alias, fallbackPrefix + ":" + alias));
    }

    return aliasesStream.distinct().collect(Collectors.toList());
  }
}
