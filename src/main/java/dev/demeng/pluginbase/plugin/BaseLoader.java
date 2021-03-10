package dev.demeng.pluginbase.plugin;

import org.jetbrains.annotations.NotNull;

/** Simple class containg a method to get the {@link BasePlugin} the library is working with. */
public final class BaseLoader {

  private static BasePlugin plugin = null;

  /**
   * Gets a never-null instance of the {@link BasePlugin} the library is currently working with.
   *
   * @return A never-null instance of the {@link BasePlugin} the library is currently working with.
   * @throws RuntimeException If the plugin is not set (main class does not extend {@link
   *     BasePlugin})
   */
  @NotNull
  public static BasePlugin getPlugin() {

    if (plugin == null) {
      throw new RuntimeException("Main class does not extend DemPlugin");
    }

    return plugin;
  }

  /**
   * Sets the {@link BasePlugin} that the library is currently working with.
   *
   * @param newPlugin The new instance of {@link BasePlugin}.
   */
  public static void setPlugin(BasePlugin newPlugin) {
    plugin = newPlugin;
  }
}
