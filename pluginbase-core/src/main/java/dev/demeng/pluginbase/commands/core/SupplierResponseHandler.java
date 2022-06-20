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

package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * A response handler that appropriately handles return types of {@link Supplier}s.
 */
final class SupplierResponseHandler implements ResponseHandler<Supplier<Object>> {

  private final ResponseHandler<Object> delegate;

  public SupplierResponseHandler(ResponseHandler<Object> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void handleResponse(Supplier<Object> response, @NotNull CommandActor actor,
      @NotNull ExecutableCommand command) {
    try {
      delegate.handleResponse(response.get(), actor, command);
    } catch (Throwable throwable) {
      command.getCommandHandler().getExceptionHandler().handleException(throwable, actor);
    }
  }
}
