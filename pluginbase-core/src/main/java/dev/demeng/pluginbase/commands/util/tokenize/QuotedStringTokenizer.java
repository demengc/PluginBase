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
package dev.demeng.pluginbase.commands.util.tokenize;

import dev.demeng.pluginbase.commands.command.ArgumentParser;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.exception.ArgumentParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Parser for converting a quoted string into a list of arguments.
 *
 * <p>Grammar is roughly (yeah, this is not really a proper grammar but it gives
 * you an idea of what's happening:</p>
 *
 * <blockquote><pre> WHITESPACE = Character.isWhiteSpace(codePoint)
 * CHAR := (all unicode)
 * ESCAPE := '\' CHAR
 * QUOTE = ' | "
 * UNQUOTED_ARG := (CHAR | ESCAPE)+ WHITESPACE
 * QUOTED_ARG := QUOTE (CHAR | ESCAPE)+ QUOTE
 * ARGS := ((UNQUOTED_ARG | QUOTED_ARG) WHITESPACE+)+</pre></blockquote>
 */
public final class QuotedStringTokenizer implements ArgumentParser {

  public static final QuotedStringTokenizer INSTANCE = new QuotedStringTokenizer();

  private QuotedStringTokenizer() {
  }

  private static final int CHAR_BACKSLASH = '\\';
  private static final int CHAR_SINGLE_QUOTE = '\'';
  private static final int CHAR_DOUBLE_QUOTE = '"';

  @Override
  public ArgumentStack parse(@NotNull final String arguments) throws ArgumentParseException {
    if (arguments.length() == 0) {
      return ArgumentStack.empty();
    }
    final TokenizerState state = new TokenizerState(arguments);
    final ArgumentStack returnedArgs = ArgumentStack.empty();
    while (state.hasMore()) {
      skipWhiteSpace(state);
      final String arg = nextArg(state);
      returnedArgs.add(arg);
    }
    return returnedArgs;
  }

  private void skipWhiteSpace(final TokenizerState state) throws ArgumentParseException {
    if (!state.hasMore()) {
      return;
    }
    while (state.hasMore() && Character.isWhitespace(state.peek())) {
      state.next();
    }
  }

  private String nextArg(final TokenizerState state) throws ArgumentParseException {
    final StringBuilder argBuilder = new StringBuilder();
    if (state.hasMore()) {
      final int codePoint = state.peek();
      if (codePoint == CHAR_DOUBLE_QUOTE || codePoint == CHAR_SINGLE_QUOTE) {
        // quoted string
        parseQuotedString(state, codePoint, argBuilder);
      } else {
        parseUnquotedString(state, argBuilder);
      }
    }
    return argBuilder.toString();
  }

  private void parseQuotedString(final TokenizerState state, final int startQuotation,
      final StringBuilder builder)
      throws ArgumentParseException {
    // Consume the start quotation character
    int nextCodePoint = state.next();
    if (nextCodePoint != startQuotation) {
      throw state.createException(String.format(
          "Actual next character '%c' did not match expected quotation character '%c'",
          nextCodePoint, startQuotation));
    }

    while (true) {
      if (!state.hasMore()) {
        return;
      }
      nextCodePoint = state.peek();
      if (nextCodePoint == startQuotation) {
        state.next();
        return;
      } else if (nextCodePoint == CHAR_BACKSLASH) {
        parseEscape(state, builder);
      } else {
        builder.appendCodePoint(state.next());
      }
    }
  }

  private void parseUnquotedString(final TokenizerState state, final StringBuilder builder)
      throws ArgumentParseException {
    while (state.hasMore()) {
      final int nextCodePoint = state.peek();
      if (Character.isWhitespace(nextCodePoint)) {
        return;
      } else if (nextCodePoint == CHAR_BACKSLASH) {
        parseEscape(state, builder);
      } else {
        builder.appendCodePoint(state.next());
      }
    }
  }

  private static void parseEscape(final TokenizerState state, final StringBuilder builder)
      throws ArgumentParseException {
    state.next(); // Consume \
    builder.appendCodePoint(state.next());
  }

  private static class TokenizerState {

    private final String buffer;
    private int index = -1;

    TokenizerState(final String buffer) {
      this.buffer = buffer;
    }

    // Utility methods
    public boolean hasMore() {
      return index + 1 < buffer.length();
    }

    public int peek() throws ArgumentParseException {
      if (!hasMore()) {
        throw createException("Buffer overrun while parsing args");
      }
      return buffer.codePointAt(index + 1);
    }

    public int next() throws ArgumentParseException {
      if (!hasMore()) {
        throw createException("Buffer overrun while parsing args");
      }
      return buffer.codePointAt(++index);
    }

    public ArgumentParseException createException(final String message) {
      return new ArgumentParseException(message, buffer, index);
    }

    public int getIndex() {
      return index;
    }
  }
}
