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

import dev.demeng.pluginbase.commands.command.CommandActor;
import org.jetbrains.annotations.NotNull;

/**
 * Used to directly send-and-return a message to the command actor.
 * <p>
 * This exception should be used to directly transfer messages to the {@link CommandActor}, however
 * should not be used in command-fail contexts. To signal an error to the actor, use
 * {@link CommandErrorException}.
 */
public class SendMessageException extends SendableException {

  private final Object[] arguments;

  /**
   * Constructs a new {@link SendMessageException} with an inferred actor
   *
   * @param message Message to send
   */
  public SendMessageException(final String message, final Object... arguments) {
    super(message);
    this.arguments = arguments;
  }

  /**
   * Sends the message to the given actor
   *
   * @param actor Actor to send to
   */
  @Override
  public void sendTo(@NotNull final CommandActor actor) {
    actor.replyLocalized(getMessage(), arguments);
  }
}
