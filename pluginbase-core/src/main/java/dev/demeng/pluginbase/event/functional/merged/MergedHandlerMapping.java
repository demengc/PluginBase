/*
 * MIT License
 *
 * Copyright (c) 2024 Demeng Chen
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

package dev.demeng.pluginbase.event.functional.merged;

import java.util.function.Function;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

class MergedHandlerMapping<T, E extends Event> {

  private final EventPriority priority;
  private final Function<Object, T> function;

  MergedHandlerMapping(final EventPriority priority, final Function<E, T> function) {
    this.priority = priority;
    //noinspection unchecked
    this.function = o -> function.apply((E) o);
  }

  public Function<Object, T> getFunction() {
    return this.function;
  }

  public EventPriority getPriority() {
    return this.priority;
  }
}
