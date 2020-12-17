package dev.demeng.pluginbase;

import dev.demeng.pluginbase.plugin.DemLoader;
import org.bukkit.Bukkit;

public class Common {

  public static String getName() {
    return DemLoader.getPlugin().getDescription().getName();
  }

  public static String getVersion() {
    return DemLoader.getPlugin().getDescription().getVersion();
  }

  public static void error(Throwable error, String description, boolean disable) {

    if (error != null) {
      error.printStackTrace();
    }

    //TODO Print console messages.

    if (disable && Bukkit.getPluginManager().isPluginEnabled(DemLoader.getPlugin())) {
      Bukkit.getPluginManager().disablePlugin(DemLoader.getPlugin());
    }
  }
}
