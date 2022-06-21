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

import static dev.demeng.pluginbase.commands.util.Collections.linkedListOf;

import dev.demeng.pluginbase.commands.util.Preconditions;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the full, qualified, case-insensitive path of a command.
 * <p>
 * This class is immutable, hence is thread-safe, and is intended to be used as a key for maps that
 * use hashing.
 */
public class CommandPath implements Iterable<String>, Comparable<CommandPath> {

  /**
   * Returns the corresponding {@link CommandPath} to the given path
   *
   * @param path Path to wrap
   * @return The command path
   */
  public static @NotNull CommandPath get(@NotNull final String... path) {
    Preconditions.notEmpty(path, "Path cannot be empty!");
    return new CommandPath(path.clone());
  }

  /**
   * Returns the corresponding {@link CommandPath} to the given path
   *
   * @param path Path to wrap
   * @return The command path
   */
  public static @NotNull CommandPath get(@NotNull final Collection<String> path) {
    Preconditions.notEmpty(path, "Path cannot be empty!");
    return new CommandPath(path.toArray(new String[0]));
  }

  /**
   * Represents the actual path of this command
   */
  protected final LinkedList<String> path;

  /**
   * Instantiates a path with the specified array.
   *
   * @param path Path to use.
   */
  CommandPath(final String[] path) {
    for (int i = 0; i < path.length; i++) {
      final String s = path[i];
      path[i] = s.toLowerCase();
    }
    this.path = linkedListOf(path);
  }

  /**
   * Converts this path to a string, where all elements in the path are joined by a space.
   *
   * @return The real path
   */
  public @NotNull String toRealString() {
    return String.join(" ", path);
  }

  /**
   * Returns a clone linked list of this path.
   *
   * @return A linked list of this path
   */
  public @NotNull LinkedList<String> toList() {
    return new LinkedList<>(path);
  }

  /**
   * Returns the root parent of this command path. This is equivilent to calling
   * {@link #getFirst()}.
   *
   * @return The parent
   */
  public @NotNull String getParent() {
    return path.getFirst();
  }

  /**
   * Returns the name (tail) of this command path. This is equivilent to calling
   * {@link #getLast()}.
   *
   * @return The name
   */
  public @NotNull String getName() {
    return path.getLast();
  }

  /**
   * Returns the first element of this command path
   *
   * @return The first element
   */
  public @NotNull String getFirst() {
    return path.getFirst();
  }

  /**
   * Returns the last element in this command path
   *
   * @return The last element
   */
  public @NotNull String getLast() {
    return path.getLast();
  }

  /**
   * Returns the string element at the given index.
   *
   * @param index Index to fetch at
   * @return The string at the given index
   * @throws IndexOutOfBoundsException -
   */
  public @NotNull String get(final int index) {
    return path.get(index);
  }

  /**
   * Returns the size of this command path
   *
   * @return The path size
   */
  public int size() {
    return path.size();
  }

  /**
   * Returns whether this path represents a root command path or not
   *
   * @return If this path represents a root path
   */
  public boolean isRoot() {
    return path.size() == 1;
  }

  /**
   * Returns the full path of the category of this command. This will return null if this path
   * represents a root command.
   *
   * @return The command category path
   */
  public @Nullable CommandPath getCategoryPath() {
    if (path.size() <= 1) {
      return null;
    }
    final LinkedList<String> list = toList();
    list.removeLast();
    return CommandPath.get(list);
  }

  /**
   * Returns the subcommand path of this command path. This will simply drop the command's parent
   * name.
   *
   * @return The subcommand path.
   */
  public @NotNull LinkedList<String> getSubcommandPath() {
    return new LinkedList<>(path.subList(1, path.size()));
  }

  /**
   * Returns a mutable copy of this command path.
   *
   * @return The mutable copy.
   */
  public MutableCommandPath toMutablePath() {
    return new MutableCommandPath(path.toArray(new String[0]));
  }

  /**
   * Returns whether is this command path mutable or not.
   * <p>
   * This should only return true in cases of {@link MutableCommandPath}.
   *
   * @return Whether is the path mutable or not.
   */
  public boolean isMutable() {
    return false;
  }

  /**
   * Tests whether is this path a child of the specified path or not
   *
   * @param other Path to test against
   * @return True if this is a child of it, false if otherwise.
   */
  public boolean isChildOf(final CommandPath other) {
    return toRealString().startsWith(other.toRealString());
  }

  /**
   * Tests whether is this path a parent of the specified path or not
   *
   * @param other Path to test against
   * @return True if this is a child of it, false if otherwise.
   */
  public boolean isParentOf(final CommandPath other) {
    return other.toRealString().startsWith(toRealString());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CommandPath)) {
      return false;
    }
    final CommandPath that = (CommandPath) o;
    return Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public String toString() {
    return "CommandPath{" + toRealString() + '}';
  }

  @NotNull
  @Override
  public Iterator<String> iterator() {
    return new PathIterator<>(path.iterator());
  }

  /**
   * An implementation of {@link Iterator} that ensures the path cannot be mutated by
   * {@link Iterator#remove()}.
   */
  private static class PathIterator<E> implements Iterator<E> {

    private final Iterator<? extends E> iterator;

    public PathIterator(final Iterator<? extends E> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public E next() {
      return iterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Iterator.remove() is disabled.");
    }

  }

  @Override
  public int compareTo(@NotNull final CommandPath o) {
    if (isParentOf(o)) {
      return -1;
    } else if (isChildOf(o)) {
      return 1;
    } else if (o.equals(this)) {
      return 0;
    } else {
      return toRealString().compareTo(o.toRealString());
    }
  }
}
