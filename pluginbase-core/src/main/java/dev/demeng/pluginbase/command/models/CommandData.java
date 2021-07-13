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

package dev.demeng.pluginbase.command.models;

import dev.demeng.pluginbase.command.CommandBase;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores all of the relevant data for a command.
 */
@Data
@AllArgsConstructor
public final class CommandData {

  /**
   * The command base this command data is relevant to.
   */
  @NotNull @Getter private final CommandBase commandBase;

  /**
   * The list of argument types, in order.
   */
  @NotNull @Getter private final List<Class<?>> arguments;

  /**
   * The list of argument names, in order.
   */
  @NotNull @Getter private final List<String> argumentNames;

  /**
   * Map of argument indexes and its completion ID. Used for parameter completions ({@link
   * dev.demeng.pluginbase.command.annotations.Completion}).
   */
  @NotNull @Getter private final Map<Integer, String> completions;

  /**
   * The name of the command.
   */
  @Nullable @Getter @Setter private String name;

  /**
   * The method to invoke when the command is executed.
   */
  @Getter @Setter private Method method;

  /**
   * If this command is the default command of the command base or not.
   */
  @Getter @Setter private boolean def;

  /**
   * The completion method to invoke. Only used for completion methods ({@link
   * dev.demeng.pluginbase.command.annotations.CompleteFor}).
   */
  @Nullable @Getter @Setter private Method completionMethod;

  /**
   * The type of the expected command sender, which is either console, player, or a CommandSender in
   * general.
   */
  @Getter @Setter private Class<?> senderClass;

  /**
   * A brief description of the command.
   */
  @Nullable @Getter @Setter private String description;

  /**
   * If this command data belongs to an alias of another command.
   */
  @Getter @Setter private boolean alias;

  /**
   * The usage of the command.
   */
  @Nullable @Getter @Setter private String usage;

  /**
   * The permission required to use the command.
   */
  @Nullable @Getter @Setter private String permission;

  /**
   * If this method contains an optional argument at the end or not.
   */
  @Getter @Setter private boolean optionalArgument;

  /**
   * Creates a new command data object with default values.
   *
   * @param commandBase The command base this data is relevant to
   */
  public CommandData(@NotNull final CommandBase commandBase) {
    this.commandBase = commandBase;
    this.arguments = new ArrayList<>();
    this.argumentNames = new ArrayList<>();
    this.completions = new HashMap<>();
  }

  /**
   * Copies all of the current data into a new command data object.
   *
   * @return The same command data as a different object
   */
  public CommandData copy() {
    return new CommandData(commandBase, arguments, argumentNames, completions, name, method, def,
        completionMethod, senderClass, description, alias, usage, permission, optionalArgument);
  }
}
