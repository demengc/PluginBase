package dev.demeng.pluginbase;

/**
 * Settings for the plugin base. Most of these methods should be overriden to suite your needs.
 * Apply the settings using {@link dev.demeng.pluginbase.plugin.DemPlugin}'s #setBaseSettings().
 */
public interface BaseSettings {

  /**
   * The prefix for chat and console messages. Colorized internally.
   *
   * @return The prefix
   */
  default String prefix() {
    return "&8[&bMyPlugin&8] ";
  }
}
