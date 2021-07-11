/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.exceptions;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.TaskUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of RuntimeException that calls {@link dev.demeng.pluginbase.Common#error(Throwable,
 * String, boolean, Player...)}.
 */
public class PluginErrorException extends RuntimeException {

  /**
   * Creates a new exception that will report the plugin error.
   *
   * @see Common#error(Throwable, String, boolean, Player...)
   */
  public PluginErrorException(
      @NotNull final String description,
      final boolean disable,
      final Player... players) {
    super();
    TaskUtils.delay(runnable -> Common.error(null, description, disable, players), 2L);
  }
}
