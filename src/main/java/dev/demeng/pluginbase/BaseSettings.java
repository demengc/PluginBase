package dev.demeng.pluginbase;

import dev.demeng.pluginbase.plugin.BasePlugin;

/**
 * Settings for the plugin base. Most of these methods should be overriden to suite your needs.
 * Apply the settings using {@link BasePlugin}'s #setBaseSettings().
 */
public interface BaseSettings {

  /**
   * The prefix for chat and console messages. Colorized internally.
   *
   * @return The prefix
   */
  default String prefix() {
    return "&8[&b" + Common.getName() + "&8] &r";
  }

  /**
   * The message to send if the console attempts to execute a players-only command.
   *
   * @return The not player message
   */
  default String notPlayer() {
    return "&cYou must be a player to execute this command.";
  }

  /**
   * The message to send if the command executor does not have the required permission.
   *
   * <p>Use %permission% for the required permission.
   *
   * @return The insufficient permission message
   */
  default String insufficientPermission() {
    return "&cYou need the &f%permission% &cpermission to do this.";
  }

  /**
   * The message to send if a player uses a command incorrectly.
   *
   * <p>Use %usage% for the actual usage of the command.
   *
   * @return The incorrect usage message
   */
  default String incorrectUsage() {
    return "&cIncorrect usage. Did you mean: &f%usage%";
  }
}
