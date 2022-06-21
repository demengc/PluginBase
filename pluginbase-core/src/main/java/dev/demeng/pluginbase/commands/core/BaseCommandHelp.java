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

import dev.demeng.pluginbase.commands.command.CommandCategory;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.exception.InvalidHelpPageException;
import dev.demeng.pluginbase.commands.help.CommandHelp;
import dev.demeng.pluginbase.commands.help.CommandHelpWriter;
import dev.demeng.pluginbase.commands.process.ContextResolver;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

final class BaseCommandHelp<T> extends ArrayList<T> implements CommandHelp<T> {

  @Override
  public CommandHelp<T> paginate(final int page, final int elementsPerPage)
      throws InvalidHelpPageException {
    if (isEmpty()) {
      return new BaseCommandHelp<>();
    }
    final BaseCommandHelp<T> list = new BaseCommandHelp<>();
    final int size = getPageSize(elementsPerPage);
    if (page > size) {
      throw new InvalidHelpPageException(this, page, elementsPerPage);
    }
    final int listIndex = page - 1;
    final int l = Math.min(page * elementsPerPage, size());
    for (int i = listIndex * elementsPerPage; i < l; ++i) {
      list.add(get(i));
    }
    return list;
  }

  @Override
  public @Range(from = 1, to = Long.MAX_VALUE) int getPageSize(final int elementsPerPage) {
    if (elementsPerPage < 1) {
      throw new IllegalArgumentException(
          "Elements per page cannot be less than 1! (Found " + elementsPerPage + ")");
    }
    return (size() / elementsPerPage) + (size() % elementsPerPage == 0 ? 0 : 1);
  }

  static final class Resolver implements ContextResolver<CommandHelp<?>> {

    private final BaseCommandHandler handler;

    public Resolver(final BaseCommandHandler handler) {
      this.handler = handler;
    }

    @Override
    public CommandHelp<?> resolve(@NotNull final ContextResolverContext context) {
      if (handler.getHelpWriter() == null) {
        throw new IllegalArgumentException("No help writer is registered!");
      }
      final ExecutableCommand helpCommand = context.command();
      final CommandHelpWriter<?> writer = handler.getHelpWriter();
      final BaseCommandHelp<Object> entries = new BaseCommandHelp<>();
      final CommandCategory parent = helpCommand.getParent();
      final CommandPath parentPath = parent == null ? null : parent.getPath();
      handler.executables.values().stream().sorted().forEach(c -> {
        if (parentPath == null || parentPath.isParentOf(c.getPath())) {
          if (c != helpCommand) {
            final Object generated = writer.generate(c, context.actor());
            if (generated != null) {
              entries.add(generated);
            }
          }
        }
      });
      return entries;
    }
  }
}
