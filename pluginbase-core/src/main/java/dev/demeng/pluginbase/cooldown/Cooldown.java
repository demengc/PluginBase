/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) lucko/helper contributors
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

package dev.demeng.pluginbase.cooldown;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A cooldown lasting a specified amount of time.
 */
public class Cooldown {

  // When the last test occurred.
  private long lastTested = 0;
  // The cooldown duration, in milliseconds.
  @Getter private final long duration;

  private Cooldown(long amount, TimeUnit unit) {
    this.duration = unit.toMillis(amount);
  }

  /**
   * Creates a cooldown lasting a number of game ticks.
   *
   * @param ticks The number of ticks
   * @return The new Cooldown
   */
  @NotNull
  public static Cooldown ofTicks(long ticks) {
    return new Cooldown(ticks * 50, TimeUnit.MILLISECONDS);
  }

  /**
   * Creates a cooldown lasting a specified amount of time.
   *
   * @param amount The amount of time
   * @param unit   The unit of time
   * @return The new Cooldown
   */
  @NotNull
  public static Cooldown of(long amount, @NotNull TimeUnit unit) {
    return new Cooldown(amount, unit);
  }

  /**
   * Returns true if the cooldown is not active, and then resets the timer.
   *
   * <p>If the cooldown is currently active, the timer is <strong>not</strong> reset.</p>
   *
   * @return true if the cooldown is not active
   */
  public boolean test() {
    if (!testSilently()) {
      return false;
    }

    reset();
    return true;
  }

  /**
   * Returns true if the cooldown is not active.
   *
   * @return true if the cooldown is not active
   */
  public boolean testSilently() {
    return elapsed() > getDuration();
  }

  /**
   * Returns the elapsed time in milliseconds since the cooldown was last reset, or since creation
   * time.
   *
   * @return The elapsed time
   */
  public long elapsed() {
    return System.currentTimeMillis() - getLastTested().orElse(0);
  }

  /**
   * Resets the cooldown.
   */
  public void reset() {
    setLastTested(System.currentTimeMillis());
  }

  /**
   * Gets the time in milliseconds until the cooldown will become inactive.
   *
   * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
   *
   * @return The time in millis until the cooldown will expire
   */
  public long remainingMillis() {
    long diff = elapsed();
    return diff > getDuration() ? 0L : getDuration() - diff;
  }

  /**
   * Gets the time until the cooldown will become inactive.
   *
   * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
   *
   * @param unit The unit to return in
   * @return The time until the cooldown will expire
   */
  public long remainingTime(TimeUnit unit) {
    return Math.max(0L, unit.convert(remainingMillis(), TimeUnit.MILLISECONDS));
  }

  /**
   * Return the time in milliseconds when this cooldown was last {@link #test()}ed.
   *
   * @return The last call time
   */
  @NotNull
  public OptionalLong getLastTested() {
    return this.lastTested == 0 ? OptionalLong.empty() : OptionalLong.of(this.lastTested);
  }

  /**
   * Sets the time in milliseconds when this cooldown was last tested.
   *
   * <p>Note: this should only be used when re-constructing a cooldown
   * instance. Use {@link #test()} otherwise.</p>
   *
   * @param time The time
   */
  public void setLastTested(long time) {
    if (time <= 0) {
      this.lastTested = 0;
    } else {
      this.lastTested = time;
    }
  }

  /**
   * Copies the properties of this cooldown to a new instance
   *
   * @return A cloned Cooldown instance
   */
  @NotNull
  public Cooldown copy() {
    return new Cooldown(this.duration, TimeUnit.MILLISECONDS);
  }
}
