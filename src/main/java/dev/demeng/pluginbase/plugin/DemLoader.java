package dev.demeng.pluginbase.plugin;

import org.jetbrains.annotations.NotNull;

/** Simple class containg a method to get the {@link DemPlugin} the library is working with. */
public final class DemLoader {

  private static DemPlugin plugin = null;

  /**
   * Gets a never-null instance of the {@link DemPlugin} the library is currently working with.
   *
   * @return A never-null instance of the {@link DemPlugin} the library is currently working with.
   * @throws RuntimeException If the plugin is not set (main class does not extend {@link
   *     DemPlugin})
   */
  @NotNull
  public static DemPlugin getPlugin() {

    if (plugin == null) {
      throw new RuntimeException("Main class does not extend DemPlugin");
    }

    return plugin;
  }

  /**
   * Sets the {@link DemPlugin} that the library is currently working with.
   *
   * @param newPlugin The new instance of {@link DemPlugin}.
   */
  public static void setPlugin(DemPlugin newPlugin) {
    plugin = newPlugin;
  }
}
