/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

import dev.demeng.pluginbase.Registerer;
import dev.demeng.pluginbase.exceptions.PluginErrorException;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a phase of a minigame (ex. pre-game, the main phase, post-game).
 *
 * <p>All event listeners in the child class will be registered on start and unregistered on end.
 * All additional listeners for this specific state should be registered using {@link
 * #registerListener(Listener)}. Tasks added to {@link #tasks} will be cancelled when the state
 * ends.
 */
@RequiredArgsConstructor
public abstract class GameState implements Listener {

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

  /**
   * The listeners to unregister when the state ends.
   */
  protected final Set<Listener> listeners = new HashSet<>();

  /**
   * The tasks to cancel when the state ends.
   */
  protected final Set<BukkitTask> tasks = new HashSet<>();

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

    try {
      registerListener(this);
      onStart();
    } catch (final Exception ex) {
      throw new PluginErrorException("Exception during state start.", false);
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
      throw new PluginErrorException("Exception during state update.", false);
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
      listeners.forEach(HandlerList::unregisterAll);
      listeners.clear();
      tasks.forEach(BukkitTask::cancel);
      tasks.clear();

      onEnd();
    } catch (final Exception ex) {
      throw new PluginErrorException("Exception during state end.", false);
    }
  }

  /**
   * Registers a listener and ensures that it is unregistered when the state ends.
   *
   * @param listener The listener to register
   */
  public void registerListener(@NotNull final Listener listener) {
    Registerer.registerListener(listener);
    listeners.add(listener);
  }

  /**
   * The number of milliseconds until the state is over, based on {@link #getDuration()}, or 0 if
   * the duration has already passed.
   *
   * @return The remaining duration, minimum 0
   */
  public long getRemainingDuration() {
    return Math.max(0L, System.currentTimeMillis() - (startTime + getDuration()));
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
