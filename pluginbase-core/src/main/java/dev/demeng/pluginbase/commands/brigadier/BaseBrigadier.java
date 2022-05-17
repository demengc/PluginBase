/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package dev.demeng.pluginbase.commands.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.core.BaseActor;
import dev.demeng.pluginbase.commands.core.BaseHandler;
import dev.demeng.pluginbase.commands.core.EntitySelector;
import dev.demeng.pluginbase.commands.util.ClassMap;
import java.lang.reflect.Constructor;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.MinecraftArgumentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A convenient way to hook into Brigadier.
 */
public final class BaseBrigadier {

  private static final ArgumentType<?> PLAYERS = entity(false, true);

  private final ClassMap<ArgumentType<?>> argumentTypes = new ClassMap<>();
  private final Commodore commodore;
  private final CommandHandler handler;

  public BaseBrigadier(Commodore commodore, CommandHandler handler) {
    this.commodore = commodore;
    this.handler = handler;
    argumentTypes.add(Player.class, entity(true, true));
    argumentTypes.add(EntitySelector.class, entity(false, false));
    argumentTypes.add(EntityType.class,
        MinecraftArgumentTypes.getByKey(NamespacedKey.minecraft("entity_summon")));
  }

  /**
   * Wraps Brigadier's command sender with the platform's appropriate {@link CommandActor}
   *
   * @param commandSource Source to wrap
   * @return The wrapped command source
   */
  @NotNull
  public CommandActor wrapSource(@NotNull Object commandSource) {
    return new BaseActor(commodore.getBukkitSender(commandSource), handler);
  }

  /**
   * Registers the given command node to the dispatcher.
   *
   * @param node Node to register
   */
  public void register(@NotNull LiteralCommandNode<?> node) {
    Command command = ((JavaPlugin) handler.getPlugin()).getCommand(node.getLiteral());
    if (command == null) {
      commodore.register(node);
    } else {
      commodore.register(command, node);
    }
  }

  /**
   * Returns the {@link ArgumentType} corresponding to this parameter. This may be used to return
   * certain argument types that cannot be registered in {@link #getAdditionalArgumentTypes()} (for
   * example, generic types).
   *
   * @param parameter Parameter to get for
   * @return The argument type, or {@code null} if not applicable.
   */
  @Nullable
  public ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter) {

    if (EntitySelector.class.isAssignableFrom(parameter.getType())) {
      Class<? extends Entity> type = BaseHandler.getSelectedEntity(parameter.getFullType());
      if (Player.class.isAssignableFrom(type)) // EntitySelector<Player>
      {
        return PLAYERS;
      }
    }
    return null;

  }

  /**
   * Returns the special argument types for parameter types.
   *
   * @return Additional argument types
   */
  @NotNull
  public ClassMap<ArgumentType<?>> getAdditionalArgumentTypes() {
    return argumentTypes;
  }

  /**
   * Registers the given command node builder to the dispatcher
   *
   * @param node Node to register
   */
  public void register(@NotNull LiteralArgumentBuilder<?> node) {
    register(node.build());
  }

  private static ArgumentType<?> entity(boolean single, boolean playerOnly) {
    return newEntityType(NamespacedKey.minecraft("entity"), single, playerOnly);
  }

  private static ArgumentType<?> newEntityType(NamespacedKey key, Object... args) {
    try {
      final Constructor<? extends ArgumentType<?>> constructor = MinecraftArgumentTypes.getClassByKey(
          key).getDeclaredConstructor(boolean.class, boolean.class);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
