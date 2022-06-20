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

import com.mojang.brigadier.arguments.ArgumentType;
import dev.demeng.pluginbase.commands.bukkit.BukkitBrigadier;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A resolver that creates dedicated {@link ArgumentType}s for parameters. This can read annotations
 * and other information to construct a suitable argument type.
 * <p>
 * Register with {@link BukkitBrigadier#bind(Class, ArgumentTypeResolver)}.
 */
@FunctionalInterface
public interface ArgumentTypeResolver {

  /**
   * Returns the argument type for the given parameter. If this resolver cannot deal with the
   * parameter, it may return null.
   *
   * @param parameter Parameter to create for
   * @return The argument type
   */
  @Nullable ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter);

}
