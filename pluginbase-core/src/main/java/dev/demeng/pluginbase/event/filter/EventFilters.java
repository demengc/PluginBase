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

package dev.demeng.pluginbase.event.filter;


import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Defines standard event predicates for use in functional event handlers.
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventFilters {

  private static final Predicate<? extends Cancellable> IGNORE_CANCELLED = e -> !e.isCancelled();
  private static final Predicate<? extends Cancellable> IGNORE_UNCANCELLED = Cancellable::isCancelled;
  private static final Predicate<? extends PlayerLoginEvent> IGNORE_DISALLOWED_LOGIN = e ->
      e.getResult() == PlayerLoginEvent.Result.ALLOWED;
  private static final Predicate<? extends AsyncPlayerPreLoginEvent> IGNORE_DISALLOWED_PRE_LOGIN = e ->
      e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED;

  private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_BLOCK = e ->
      e.getFrom().getBlockX() != e.getTo().getBlockX() ||
          e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
          e.getFrom().getBlockY() != e.getTo().getBlockY() ||
          !e.getFrom().getWorld().equals(e.getTo().getWorld());

  private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_BLOCK_AND_Y = e ->
      e.getFrom().getBlockX() != e.getTo().getBlockX() ||
          e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
          !e.getFrom().getWorld().equals(e.getTo().getWorld());

  private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_CHUNK = e ->
      (e.getFrom().getBlockX() >> 4) != (e.getTo().getBlockX() >> 4) ||
          (e.getFrom().getBlockZ() >> 4) != (e.getTo().getBlockZ() >> 4) ||
          !e.getFrom().getWorld().equals(e.getTo().getWorld());

  /**
   * Returns a predicate which only returns true if the event isn't cancelled
   *
   * @param <T> the event type
   * @return a predicate which only returns true if the event isn't cancelled
   */
  @NotNull
  public static <T extends Cancellable> Predicate<T> ignoreCancelled() {
    return (Predicate<T>) IGNORE_CANCELLED;
  }

  /**
   * Returns a predicate which only returns true if the event is cancelled
   *
   * @param <T> the event type
   * @return a predicate which only returns true if the event is cancelled
   */
  @NotNull
  public static <T extends Cancellable> Predicate<T> ignoreNotCancelled() {
    return (Predicate<T>) IGNORE_UNCANCELLED;
  }

  /**
   * Returns a predicate which only returns true if the login is allowed
   *
   * @param <T> the event type
   * @return a predicate which only returns true if the login is allowed
   */
  @NotNull
  public static <T extends PlayerLoginEvent> Predicate<T> ignoreDisallowedLogin() {
    return (Predicate<T>) IGNORE_DISALLOWED_LOGIN;
  }

  /**
   * Returns a predicate which only returns true if the login is allowed
   *
   * @param <T> the event type
   * @return a predicate which only returns true if the login is allowed
   */
  @NotNull
  public static <T extends AsyncPlayerPreLoginEvent> Predicate<T> ignoreDisallowedPreLogin() {
    return (Predicate<T>) IGNORE_DISALLOWED_PRE_LOGIN;
  }

  /**
   * Returns a predicate which only returns true if the player has moved over a block
   *
   * @param <T> the event type
   * @return a predicate which only returns true if the player has moved over a block
   */
  @NotNull
  public static <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlock() {
    return (Predicate<T>) IGNORE_SAME_BLOCK;
  }

  /**
   * Returns a predicate which only returns true if the player has moved over a block, not including
   * movement directly up and down. (so jumping wouldn't return true)
   *
   * @param <T> the event type
   * @return a predicate which only returns true if the player has moved across a block border
   */
  @NotNull
  public static <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlockAndY() {
    return (Predicate<T>) IGNORE_SAME_BLOCK_AND_Y;
  }

  /**
   * Returns a predicate which only returns true if the player has moved over a chunk border
   *
   * @param <T> the event type
   * @return a predicate which only returns true if the player has moved over a chunk border
   */
  @NotNull
  public static <T extends PlayerMoveEvent> Predicate<T> ignoreSameChunk() {
    return (Predicate<T>) IGNORE_SAME_CHUNK;
  }

  /**
   * Returns a predicate which only returns true if the player has the given permission
   *
   * @param permission the permission
   * @param <T>        the event type
   * @return a predicate which only returns true if the player has the given permission
   */
  @NotNull
  public static <T extends PlayerEvent> Predicate<T> playerHasPermission(final String permission) {
    return e -> e.getPlayer().hasPermission(permission);
  }
}
