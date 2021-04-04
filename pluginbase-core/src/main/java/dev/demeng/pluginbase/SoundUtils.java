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

package dev.demeng.pluginbase;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Various utilities for playing sound effects.
 */
@SuppressWarnings("unused")
public class SoundUtils {

  /**
   * The prefix that the util will search for, used to detemine if the plugin should play a vanilla
   * sound or a custom sound (such as one in a resource pack).
   */
  public static final String CUSTOM_PREFIX = "custom:";

  private SoundUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Play a sound to a player.
   *
   * @param player    The player that should hear the sound
   * @param soundName The name of the sound, either the enum name for vanilla sounds, or a custom
   *                  sound name prefixed with {@link #CUSTOM_PREFIX}
   * @param volume    The volume of the sound
   * @param pitch     The pitch of the sound
   */
  public static void playToPlayer(@NotNull final Player player, final String soundName, final float volume,
      final float pitch) {

    if (soundName == null || soundName.equalsIgnoreCase("none")) {
      return;
    }

    if (soundName.toLowerCase().startsWith(CUSTOM_PREFIX)) {
      playCustomToPlayer(player, soundName.toLowerCase().replace(CUSTOM_PREFIX, ""), volume, pitch);
      return;
    }

    final Sound sound;

    try {
      sound = Sound.valueOf(soundName);
    } catch (final IllegalArgumentException ex) {
      Common.error(ex, "Invalid sound: " + soundName, false);
      return;
    }

    playVanillaToPlayer(player, sound, volume, pitch);
  }

  /**
   * Plays the sound in the configuration section to the player.
   *
   * @param player  The player that will hear the sound
   * @param section The configuration section containing the sound
   */
  public static void playToPlayer(@NotNull final Player player, @NotNull final ConfigurationSection section) {
    playToPlayer(
        player,
        Objects.requireNonNull(section.getString("sound"), "Sound to play is null"),
        (float) section.getDouble("volume"),
        (float) section.getDouble("pitch"));
  }

  /**
   * Play a sound to a location.
   *
   * @param loc       The location the sound will be played
   * @param soundName The name of the sound, either the enum name for vanilla sounds, or a custom
   *                  sound name prefixed with {@link #CUSTOM_PREFIX}
   * @param volume    The volume of the sound
   * @param pitch     The pitch of the sound
   */
  public static void playToLocation(@NotNull final Location loc, final String soundName, final float volume,
      final float pitch) {

    if (soundName == null || soundName.equalsIgnoreCase("none")) {
      return;
    }

    if (soundName.toLowerCase().startsWith(CUSTOM_PREFIX)) {
      playCustomToLocation(loc, soundName.toLowerCase().replace(CUSTOM_PREFIX, ""), volume, pitch);
      return;
    }

    final Sound sound;

    try {
      sound = Sound.valueOf(soundName);
    } catch (final IllegalArgumentException ex) {
      Common.error(ex, "Invalid sound: " + soundName, false);
      return;
    }

    playVanillaToLocation(loc, sound, volume, pitch);
  }

  /**
   * Plays the sound in the configuration section to the location.
   *
   * @param loc     The location to play the sound
   * @param section The configuration section containing the sound
   */
  public static void playToLocation(@NotNull final Location loc, @NotNull final ConfigurationSection section) {
    playToLocation(
        loc,
        Objects.requireNonNull(section.getString("sound"), "Sound to play is null"),
        (float) section.getDouble("volume"),
        (float) section.getDouble("pitch"));
  }

  /**
   * Plays a vanilla sound to a player.
   *
   * @param player The player that will hear the sound
   * @param sound  The vanilla sound to play
   * @param volume The volume of the sound
   * @param pitch  The pitch of the sound
   */
  public static void playVanillaToPlayer(@NotNull final Player player, @NotNull final Sound sound, final float volume,
      final float pitch) {
    player.playSound(player.getLocation(), sound, volume, pitch);
  }

  /**
   * Plays a custom sound to a player.
   *
   * @param player The player that will hear the sound
   * @param sound  The custom sound to play
   * @param volume The volume of the sound
   * @param pitch  The pitch of the sound
   */
  public static void playCustomToPlayer(@NotNull final Player player, @NotNull final String sound, final float volume,
      final float pitch) {
    player.playSound(player.getLocation(), sound, volume, pitch);
  }

  /**
   * Plays a vanilla sound to a location.
   *
   * @param loc    The location where the sound will be played
   * @param sound  The vanilla sound to play
   * @param volume The volume of the sound
   * @param pitch  The pitch of the sound
   */
  public static void playVanillaToLocation(@NotNull final Location loc, @NotNull final Sound sound,
      final float volume, final float pitch) {
    Objects.requireNonNull(loc.getWorld(), "World is null").playSound(loc, sound, volume, pitch);
  }

  /**
   * Plays a custom sound to a location.
   *
   * @param loc    The location where the sound will be played
   * @param sound  The custom sound to play
   * @param volume The volume of the sound
   * @param pitch  The pitch of the sound
   */
  public static void playCustomToLocation(@NotNull final Location loc, @NotNull final String sound,
      final float volume, final float pitch) {
    Objects.requireNonNull(loc.getWorld(), "World is null").playSound(loc, sound, volume, pitch);
  }
}
