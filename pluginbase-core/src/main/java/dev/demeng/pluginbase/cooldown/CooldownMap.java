/*
 * MIT License
 *
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

import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;

/**
 * A self-populating map of cooldown instances.
 *
 * @param <T> The type
 */
public class CooldownMap<T> {

  @Getter private final Cooldown base;
  private final ExpiringMap<T, Cooldown> cache;

  private CooldownMap(final Cooldown base) {
    this.base = base;
    this.cache =
        ExpiringMap.builder()
            .expiration(base.getDuration() + 10000L, TimeUnit.MILLISECONDS)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .build();
  }

  /**
   * Creates a new collection with the cooldown properties defined by the base instance.
   *
   * @param base The cooldown to base off
   * @return A new collection
   */
  @NotNull
  public static <T> CooldownMap<T> create(@NotNull final Cooldown base) {
    return new CooldownMap<>(base);
  }

  /**
   * Gets the internal cooldown instance associated with the given key.
   *
   * <p>The inline Cooldown methods in this class should be used to access the functionality of the
   * cooldown as opposed to calling the methods directly via the instance returned by this method.
   *
   * @param key The key
   * @return A cooldown instance
   */
  @NotNull
  public Cooldown get(@NotNull final T key) {
    this.cache.putIfAbsent(key, base.copy());
    return this.cache.get(key);
  }

  /**
   * Stores a custom cooldown instance for the given key, replacing any existing entry.
   *
   * <p>The provided cooldown must have the same duration as the base cooldown, since the expiring
   * cache is configured around that duration.
   *
   * @param key The key to associate the cooldown with
   * @param cooldown The cooldown instance to store
   * @throws IllegalArgumentException If the cooldown's duration differs from the base cooldown's
   *     duration
   */
  public void put(@NotNull final T key, @NotNull final Cooldown cooldown) {

    if (cooldown.getDuration() != base.getDuration()) {
      throw new IllegalArgumentException(
          "Cooldown duration " + cooldown.getDuration() + ", expected " + base.getDuration());
    }

    this.cache.put(key, cooldown);
  }

  /**
   * Gets the cooldowns contained within this collection.
   *
   * @return The backing map
   */
  @NotNull
  public Map<T, Cooldown> getAll() {
    return cache;
  }

  /**
   * Tests whether the given key is off cooldown and, if so, records the current time as the last
   * tested instant. Delegates to {@link Cooldown#test()}.
   *
   * @param key The key to test
   * @return {@code true} if the key is not currently on cooldown
   */
  public boolean test(@NotNull final T key) {
    return get(key).test();
  }

  /**
   * Tests whether the given key is off cooldown without recording the current time. Use this when
   * you need to check cooldown status without consuming it. Delegates to {@link
   * Cooldown#testSilently()}.
   *
   * @param key The key to test
   * @return {@code true} if the key is not currently on cooldown
   */
  public boolean testSilently(@NotNull final T key) {
    return get(key).testSilently();
  }

  /**
   * Returns the number of milliseconds that have elapsed since the cooldown for the given key was
   * last tested. Delegates to {@link Cooldown#elapsed()}.
   *
   * @param key The key to check
   * @return Milliseconds elapsed since the last test
   */
  public long elapsed(@NotNull final T key) {
    return get(key).elapsed();
  }

  /**
   * Resets the cooldown for the given key so that it behaves as if it was never tested. Delegates
   * to {@link Cooldown#reset()}.
   *
   * @param key The key whose cooldown should be reset
   */
  public void reset(@NotNull final T key) {
    get(key).reset();
  }

  /**
   * Returns the number of milliseconds remaining on the cooldown for the given key. Returns {@code
   * 0} if the key is not currently on cooldown. Delegates to {@link Cooldown#remainingMillis()}.
   *
   * @param key The key to check
   * @return Remaining cooldown time in milliseconds, or {@code 0} if not on cooldown
   */
  public long remainingMillis(@NotNull final T key) {
    return get(key).remainingMillis();
  }

  /**
   * Returns the remaining cooldown time for the given key expressed in the specified time unit.
   * Returns {@code 0} if the key is not currently on cooldown. Delegates to {@link
   * Cooldown#remainingTime(TimeUnit)}.
   *
   * @param key The key to check
   * @param unit The time unit to express the result in
   * @return Remaining cooldown time in the given unit, or {@code 0} if not on cooldown
   */
  public long remainingTime(@NotNull final T key, @NotNull final TimeUnit unit) {
    return get(key).remainingTime(unit);
  }

  /**
   * Returns the timestamp at which the cooldown for the given key was last tested, as milliseconds
   * since the epoch. Returns an empty {@link OptionalLong} if the cooldown has never been tested.
   * Delegates to {@link Cooldown#getLastTested()}.
   *
   * @param key The key to check
   * @return The last tested timestamp, or an empty optional if never tested
   */
  @NotNull
  public OptionalLong getLastTested(@NotNull final T key) {
    return get(key).getLastTested();
  }

  /**
   * Manually sets the last tested timestamp for the cooldown associated with the given key.
   * Delegates to {@link Cooldown#setLastTested(long)}.
   *
   * @param key The key whose cooldown should be updated
   * @param time The timestamp to set, as milliseconds since the epoch
   */
  public void setLastTested(@NotNull final T key, final long time) {
    get(key).setLastTested(time);
  }
}
