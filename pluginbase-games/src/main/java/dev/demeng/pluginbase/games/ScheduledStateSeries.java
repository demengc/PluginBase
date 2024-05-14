/*
 * MIT License
 *
 * Copyright (c) 2024 Demeng Chen
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
import dev.demeng.pluginbase.scheduler.Task;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A sequential state composition that schedules each state to execute after the previous state's
 * duration has passed, or if the state's {@link GameState#isReadyToEnd()} returns true.
 */
public class ScheduledStateSeries extends GameState {

  /**
   * The list of states in this state series.
   */
  @NotNull @Getter protected final List<GameState> states = new ArrayList<>();
  private final long interval;

  protected int currentIndex = 0;
  protected Task scheduledTask;

  /**
   * Creates a new state series.
   *
   * @param states The states to add
   */
  public ScheduledStateSeries(@NotNull final List<GameState> states) {
    this.interval = 1;
    this.states.addAll(states);
  }

  /**
   * Creates a new state series.
   *
   * @param states The states to add
   */
  public ScheduledStateSeries(@NotNull final GameState... states) {
    this.interval = 1;
    this.states.addAll(Arrays.stream(states).collect(Collectors.toList()));
  }

  /**
   * Creates a new empty state series.
   */
  public ScheduledStateSeries() {
    this.interval = 1;
  }

  /**
   * Creates a new state series.
   *
   * @param interval The update interval, in ticks
   * @param states   The states to add
   */
  public ScheduledStateSeries(final long interval, @NotNull final List<GameState> states) {
    this.interval = interval;
    this.states.addAll(states);
  }

  /**
   * Creates a new state series.
   *
   * @param interval The update interval, in ticks
   * @param states   The states to add
   */
  public ScheduledStateSeries(final long interval, @NotNull final GameState... states) {
    this.interval = interval;
    this.states.addAll(Arrays.stream(states).collect(Collectors.toList()));
  }

  /**
   * Creates a new empty state series.
   *
   * @param interval The update interval, in ticks
   */
  public ScheduledStateSeries(final long interval) {
    this.interval = interval;
  }

  /**
   * Adds the states to execute after the current one ends, pushing all other states back.
   *
   * @param nextStates The next states to add
   */
  public void addNext(@NotNull final GameState... nextStates) {
    if (states.isEmpty() || currentIndex == states.size() - 1) {
      states.addAll(Arrays.asList(nextStates));
    } else {
      int addIndex = currentIndex + 1;
      for (final GameState state : nextStates) {
        addIndex = Math.min(addIndex, states.size());
        states.add(addIndex, state);
        addIndex++;
      }
    }
  }

  /**
   * Adds the states to execute after the current one ends, pushing all other states back.
   *
   * @param nextStates The next states to add
   */
  public void addNext(@NotNull final List<GameState> nextStates) {
    addNext(nextStates.toArray(new GameState[0]));
  }

  @Override
  protected void onStart() {

    if (states.isEmpty()) {
      end();
      return;
    }

    states.get(currentIndex).start();
    scheduledTask = Schedulers.sync().runRepeating(this::update, 0L, interval);
  }

  @Override
  protected void onUpdate() {

    states.get(currentIndex).update();

    final GameState current = states.get(currentIndex);

    if (current.isReadyToEnd()) {

      current.end();
      currentIndex++;

      if (currentIndex >= states.size()) {
        end();
        return;
      }

      states.get(currentIndex).start();
    }
  }

  @Override
  protected void onEnd() {

    if (currentIndex < states.size()) {
      states.get(currentIndex).end();
    }

    scheduledTask.stop();
  }

  @Override
  protected boolean isReadyToEnd() {
    return (currentIndex == states.size() - 1 && states.get(currentIndex).isReadyToEnd());
  }

  @Override
  protected long getDuration() {
    return states.stream().mapToLong(GameState::getDuration).sum();
  }
}
