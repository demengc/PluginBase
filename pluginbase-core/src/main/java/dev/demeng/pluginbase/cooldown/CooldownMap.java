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
    this.cache.putIfAbsent(key, base);
    return this.cache.get(key);
  }

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

  public boolean test(@NotNull final T key) {
    return get(key).test();
  }

  public boolean testSilently(@NotNull final T key) {
    return get(key).testSilently();
  }

  public long elapsed(@NotNull final T key) {
    return get(key).elapsed();
  }

  public void reset(@NotNull final T key) {
    get(key).reset();
  }

  public long remainingMillis(@NotNull final T key) {
    return get(key).remainingMillis();
  }

  public long remainingTime(@NotNull final T key, @NotNull final TimeUnit unit) {
    return get(key).remainingTime(unit);
  }

  @NotNull
  public OptionalLong getLastTested(@NotNull final T key) {
    return get(key).getLastTested();
  }

  public void setLastTested(@NotNull final T key, final long time) {
    get(key).setLastTested(time);
  }
}
