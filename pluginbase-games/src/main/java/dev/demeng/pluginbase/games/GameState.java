/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
 * Credits: https://www.spigotmc.org/threads/235786/ (Minikloon/FSMgasm)
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

package dev.demeng.pluginbase.games;

import dev.demeng.pluginbase.Schedulers;
import dev.demeng.pluginbase.exceptions.PluginErrorException;
import dev.demeng.pluginbase.terminable.composite.CompositeTerminable;
import dev.demeng.pluginbase.terminable.module.TerminableModule;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a phase of a minigame (ex. pre-game, the main phase, post-game). All
 * {@link dev.demeng.pluginbase.terminable.Terminable}s should be bound to the registry using
 * {@link #bind(AutoCloseable)} or {@link #bindModule(TerminableModule)}, and will be terminated
 * when the state ends.
 */
@RequiredArgsConstructor
public abstract class GameState {

  /**
   * If this state has begun.
   */
  @Getter private boolean started;
  /**
   * If this state has ended.
   */
  @Getter private boolean ended;
  /**
   * The timestamp in milliseconds since Unix epoch of when the state started, or 0 if not started.
   */
  @Getter private long startTime;

  protected CompositeTerminable registry = CompositeTerminable.create();
  private final Object lock = new Object();

  /**
   * Called when the state starts.
   */
  protected abstract void onStart();

  /**
   * Called at every interval from state start to end.
   */
  protected abstract void onUpdate();

  /**
   * Called when the state ends.
   */
  protected abstract void onEnd();

  /**
   * The duration of the state, in milliseconds.
   */
  protected abstract long getDuration();

  /**
   * Starts the state.
   */
  public void start() {

    synchronized (lock) {
      if (started || ended) {
        return;
      }

      started = true;
    }

    startTime = System.currentTimeMillis();

    Schedulers.builder()
        .async()
        .after(10, TimeUnit.SECONDS)
        .every(30, TimeUnit.SECONDS)
        .run(registry::cleanup)
        .bindWith(registry);

    try {
      onStart();
    } catch (final Exception ex) {
      throw new PluginErrorException(ex, "Exception during state start.", false);
    }
  }

  /**
   * Updates the state.
   */
  public void update() {
    synchronized (lock) {
      if (!started || ended) {
        return;
      }
    }

    if (isReadyToEnd()) {
      end();
      return;
    }

    try {
      onUpdate();
    } catch (final Exception ex) {
      throw new PluginErrorException(ex, "Exception during state update.", false);
    }
  }

  /**
   * Ends the state.
   */
  public void end() {

    synchronized (lock) {
      if (!started || ended) {
        return;
      }

      ended = true;
    }

    try {
      registry.closeAndReportException();
      onEnd();
    } catch (final Exception ex) {
      throw new PluginErrorException(ex, "Exception during state end.", false);
    }
  }

  @NotNull
  public <T extends AutoCloseable> T bind(@NotNull final T terminable) {
    return registry.bind(terminable);
  }

  @NotNull
  public <T extends TerminableModule> T bindModule(@NotNull final T module) {
    return registry.bindModule(module);
  }

  /**
   * The number of milliseconds until the state is over, based on {@link #getDuration()}, or 0 if
   * the duration has already passed.
   *
   * @return The remaining duration, minimum 0
   */
  public long getRemainingDuration() {
    return Math.max(0L, (startTime + getDuration()) - System.currentTimeMillis());
  }

  /**
   * If the state is ready to be ended. This is called every time the state updates to check if the
   * state should be ended. By default, this checks the remaining duration of the state, but it can
   * be overridden for another end condition (ex. number of players).
   *
   * @return If the state is ready to be ended
   */
  protected boolean isReadyToEnd() {
    return ended || getRemainingDuration() <= 0L;
  }
}
