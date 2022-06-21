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

package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when extra arguments are inputted for a command. By default, this exception is not thrown,
 * unless specifend by {@link CommandHandler#failOnTooManyArguments()}.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class TooManyArgumentsException extends RuntimeException {

  /**
   * The command being executed
   */
  private final @NotNull ExecutableCommand command;

  /**
   * The extra arguments
   */
  private final @NotNull ArgumentStack arguments;
}