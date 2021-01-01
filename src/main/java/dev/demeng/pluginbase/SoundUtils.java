package dev.demeng.pluginbase;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Objects;

/** Various utilities for playing sound effects. */
public class SoundUtils {

  /**
   * Play a sound to a player.
   *
   * @param player The player that should hear the sound
   * @param soundName The name of the sound, either the enum name for vanilla sounds, or a custom
   *     sound name prefixed with "custom:"
   * @param volume The volume of the sound
   * @param pitch The pitch of the sound
   */
  public static void playToPlayer(Player player, String soundName, float volume, float pitch) {
    Objects.requireNonNull(player, "Player is null");

    if (soundName == null || soundName.equalsIgnoreCase("none")) {
      return;
    }

    if (soundName.toLowerCase().startsWith("custom:")) {
      playCustomToPlayer(player, soundName.toLowerCase().replace("custom:", ""), volume, pitch);
      return;
    }

    final Sound sound;

    try {
      sound = Sound.valueOf(soundName);
    } catch (IllegalArgumentException ex) {
      Common.error(ex, "Invalid sound: " + soundName, false);
      return;
    }

    playVanillaToPlayer(player, sound, volume, pitch);
  }

  /**
   * Play a sound to a location.
   *
   * @param loc The location the sound will be played
   * @param soundName The name of the sound, either the enum name for vanilla sounds, or a custom
   *     sound name prefixed with "custom:"
   * @param volume The volume of the sound
   * @param pitch The pitch of the sound
   */
  public static void playToLocation(Location loc, String soundName, float volume, float pitch) {
    Objects.requireNonNull(loc, "Location is null");

    if (soundName == null || soundName.equalsIgnoreCase("none")) {
      return;
    }

    if (soundName.toLowerCase().startsWith("custom:")) {
      playCustomToLocation(loc, soundName.toLowerCase().replace("custom:", ""), volume, pitch);
      return;
    }

    final Sound sound;

    try {
      sound = Sound.valueOf(soundName);
    } catch (IllegalArgumentException ex) {
      Common.error(ex, "Invalid sound: " + soundName, false);
      return;
    }

    playVanillaToLocation(loc, sound, volume, pitch);
  }

  /**
   * Plays the sound in the configuration section to the player.
   *
   * @param player The player that will hear the sound
   * @param section The configuration section containing the sound
   */
  public static void playToPlayer(Player player, ConfigurationSection section) {
    Objects.requireNonNull(player, "Player is null");
    Objects.requireNonNull(section, "Configuration section is null");

    playToPlayer(
        player,
        Objects.requireNonNull(section.getString("sound"), "Sound to play is null"),
        (float) section.getDouble("volume"),
        (float) section.getDouble("pitch"));
  }

  /**
   * Plays the sound in the configuration section to the location.
   *
   * @param player The location to play the sound
   * @param section The configuration section containing the sound
   */
  public static void playToLocation(Location loc, ConfigurationSection section) {
    Objects.requireNonNull(loc, "Location is null");
    Objects.requireNonNull(section, "Configuration section is null");

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
   * @param sound The vanilla sound to play
   * @param volume The volume of the sound
   * @param pitch The pitch of the sound
   */
  public static void playVanillaToPlayer(Player player, Sound sound, float volume, float pitch) {
    player.playSound(player.getLocation(), sound, volume, pitch);
  }

  /**
   * Plays a custom sound to a player.
   *
   * @param player The player that will hear the sound
   * @param sound The custom sound to play
   * @param volume The volume of the sound
   * @param pitch The pitch of the sound
   */
  public static void playCustomToPlayer(Player player, String sound, float volume, float pitch) {
    player.playSound(player.getLocation(), sound, volume, pitch);
  }

  /**
   * Plays a vanilla sound to a location.
   *
   * @param loc The location where the sound will be played
   * @param sound The vanilla sound to play
   * @param volume The volume of the sound
   * @param pitch The pitch of the sound
   */
  public static void playVanillaToLocation(Location loc, Sound sound, float volume, float pitch) {
    Objects.requireNonNull(loc.getWorld()).playSound(loc, sound, volume, pitch);
  }

  /**
   * Plays a custom sound to a location.
   *
   * @param loc The location where the sound will be played
   * @param sound The custom sound to play
   * @param volume The volume of the sound
   * @param pitch The pitch of the sound
   */
  public static void playCustomToLocation(Location loc, String sound, float volume, float pitch) {
    Objects.requireNonNull(loc.getWorld()).playSound(loc, sound, volume, pitch);
  }
}
