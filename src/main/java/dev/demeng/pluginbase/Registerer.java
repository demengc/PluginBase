package dev.demeng.pluginbase;

import dev.demeng.pluginbase.plugin.BaseLoader;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/** Utility class for quickly registering commands, listeners, and more. */
public class Registerer {

  /**
   * Registers a listener.
   *
   * @param listener The listener to register
   */
  public static void registerListener(Listener listener) {
    Bukkit.getPluginManager().registerEvents(listener, BaseLoader.getPlugin());
  }
}
