package dev.demeng.pluginbase;

import com.cryptomorin.xseries.XMaterial;
import dev.demeng.pluginbase.plugin.DemLoader;
import org.bukkit.Bukkit;

/**
 * Commonly used methods and utilities.
 */
public class Common {

  // -----------------------------------------------------------------------------------------------------
  // PLUGIN INFORMATION
  // -----------------------------------------------------------------------------------------------------

  /**
   * Gets the name of the plugin, as defined in plugin.yml.
   *
   * @return The name of the plugin
   */
  public static String getName() {
    return DemLoader.getPlugin().getDescription().getName();
  }

  /**
   * Gets the version string, as defined in plugin.yml.
   *
   * @return The version of the plugin
   */
  public static String getVersion() {
    return DemLoader.getPlugin().getDescription().getVersion();
  }

  // -----------------------------------------------------------------------------------------------------
  // MINECRAFT VERSION
  // -----------------------------------------------------------------------------------------------------

  /**
   * Gets the server's major Minecraft version.
   *
   * <p>For example, if a server is running 1.16.4, this will return 16.
   *
   * @return The server's major Minecraft version.
   */
  public static int getServerMajorVersion() {
    return XMaterial.getVersion();
  }

  /**
   * Checks if the server's major version is at least the specified version.
   *
   * @param version The minimum major version
   * @return True if equal or greater to the provided version, false otherwise
   */
  public static boolean isServerVersionAtLeast(int version) {
    return getServerMajorVersion() >= version;
  }

  // -----------------------------------------------------------------------------------------------------
  // MISC
  // -----------------------------------------------------------------------------------------------------
  public static void error(Throwable error, String description, boolean disable) {

    if (error != null) {
      error.printStackTrace();
    }

    // TODO Print console messages.

    if (disable && Bukkit.getPluginManager().isPluginEnabled(DemLoader.getPlugin())) {
      Bukkit.getPluginManager().disablePlugin(DemLoader.getPlugin());
    }
  }
}
