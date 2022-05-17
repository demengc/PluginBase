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
package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.command.ArgumentStack;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class MutableCommandPath extends CommandPath {

  public static @NotNull MutableCommandPath empty() {
    return new MutableCommandPath(new String[0]);
  }

  public MutableCommandPath(final String[] path) {
    super(path);
  }

  public MutableCommandPath(final ArgumentStack argumentStack) {
    super(argumentStack.toArray(new String[0]));
  }

  public String removeFirst() {
    return path.removeFirst();
  }

  public String removeLast() {
    return path.removeLast();
  }

  public void addFirst(final String s) {
    path.addFirst(s);
  }

  public void addLast(final String s) {
    path.addLast(s);
  }

  public boolean contains(final Object o) {
    return path.contains(o);
  }

  public boolean add(final String s) {
    return path.add(s);
  }

  public void clear() {
    path.clear();
  }

  public void add(final int index, final String element) {
    path.add(index, element);
  }

  public String peek() {
    return path.peek();
  }

  public String poll() {
    return path.poll();
  }

  public void push(final String s) {
    path.push(s);
  }

  public String pop() {
    return path.pop();
  }

  public CommandPath toImmutablePath() {
    return new CommandPath(path.toArray(new String[0]));
  }

  @Override
  public @NotNull Iterator<String> iterator() {
    return path.iterator();
  }

  @Override
  public boolean isMutable() {
    return true;
  }
}
