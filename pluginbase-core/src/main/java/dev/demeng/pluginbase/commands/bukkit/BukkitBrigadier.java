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
package dev.demeng.pluginbase.commands.bukkit;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.demeng.pluginbase.commands.bukkit.brigadier.ArgumentTypeResolver;
import dev.demeng.pluginbase.commands.bukkit.brigadier.MinecraftArgumentType;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the brigadier hook for Bukkit
 */
public interface BukkitBrigadier {

  /**
   * Registers an argument type resolver for the given class. This will include subclasses as well.
   *
   * @param type     Type to register for
   * @param resolver The argument type resolver
   */
  void bind(@NotNull Class<?> type, @NotNull ArgumentTypeResolver resolver);

  /**
   * Registers an argument type for the given class. This will include subclasses as well.
   *
   * @param type         Type to register for
   * @param argumentType The argument type to register
   * @see MinecraftArgumentType
   */
  void bind(@NotNull Class<?> type, @NotNull ArgumentType<?> argumentType);

  /**
   * Registers an argument type for the given class. This will include subclasses as well.
   *
   * @param type         Type to register for
   * @param argumentType The argument type to register
   * @see MinecraftArgumentType
   */
  void bind(@NotNull Class<?> type, @NotNull MinecraftArgumentType argumentType);

  /**
   * Returns the argument type corresponding to the given parameter. If no resolver is able to
   * handle this parameter, {@link StringArgumentType#greedyString()} will be returned.
   *
   * @param parameter Parameter to got for
   * @return The argument type
   */
  @NotNull ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter);

  /**
   * Wraps Brigadier's command sender with the platform's appropriate {@link CommandActor}
   *
   * @param commandSource Source to wrap
   * @return The wrapped command source
   */
  @NotNull CommandActor wrapSource(@NotNull Object commandSource);

  /**
   * Registers the command handler's brigadier
   */
  void register();

}
