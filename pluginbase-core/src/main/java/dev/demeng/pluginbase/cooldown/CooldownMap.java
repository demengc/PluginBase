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

  private CooldownMap(Cooldown base) {
    this.base = base;
    this.cache = ExpiringMap.builder()
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
  public static <T> CooldownMap<T> create(@NotNull Cooldown base) {
    return new CooldownMap<>(base);
  }

  /**
   * Gets the internal cooldown instance associated with the given key.
   *
   * <p>The inline Cooldown methods in this class should be used to access the functionality of the
   * cooldown as opposed to calling the methods directly via the instance returned by this
   * method.</p>
   *
   * @param key The key
   * @return A cooldown instance
   */
  @NotNull
  public Cooldown get(@NotNull T key) {
    return this.cache.get(key);
  }

  public void put(@NotNull T key, @NotNull Cooldown cooldown) {

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

  public boolean test(@NotNull T key) {
    return get(key).test();
  }

  public boolean testSilently(@NotNull T key) {
    return get(key).testSilently();
  }

  public long elapsed(@NotNull T key) {
    return get(key).elapsed();
  }

  public void reset(@NotNull T key) {
    get(key).reset();
  }

  public long remainingMillis(@NotNull T key) {
    return get(key).remainingMillis();
  }

  public long remainingTime(@NotNull T key, @NotNull TimeUnit unit) {
    return get(key).remainingTime(unit);
  }

  @NotNull
  public OptionalLong getLastTested(@NotNull T key) {
    return get(key).getLastTested();
  }

  public void setLastTested(@NotNull T key, long time) {
    get(key).setLastTested(time);
  }
}
