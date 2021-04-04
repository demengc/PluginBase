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

package dev.demeng.pluginbase.command;

import dev.demeng.pluginbase.command.annotations.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a custom command. Must be annotated with {@link Command} and registered using {@link
 * CommandManager#register(CommandBase)};
 */
public abstract class CommandBase {

  @Nullable @Getter private String command;

  @NotNull @Getter private final List<String> aliases = new ArrayList<>();
  @NotNull @Getter private final Map<String, String> arguments = new HashMap<>();

  protected CommandBase() {
  }

  protected CommandBase(@NotNull final String command, @NotNull final List<String> aliases) {
    this.command = command;
    this.aliases.addAll(aliases);
  }

  /**
   * Gets the argument used for the command.
   *
   * @param name The argument name
   * @return The argument value
   */
  @Nullable
  public String getArgument(@NotNull final String name) {
    return arguments.get(name);
  }

  /**
   * Adds a new argument.
   *
   * @param name     The argument name
   * @param argument The argument value
   */
  public void addArgument(@NotNull final String name, @NotNull final String argument) {
    arguments.put(name, argument);
  }

  /**
   * Clears the stored arguments.
   */
  public void clearArguments() {
    arguments.clear();
  }
}
