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
}
