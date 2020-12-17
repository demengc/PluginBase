package dev.demeng.pluginbase.libby.managers;

import dev.demeng.pluginbase.libby.classloader.URLClassLoaderHelper;
import dev.demeng.pluginbase.plugin.DemLoader;

import java.net.URLClassLoader;
import java.nio.file.Path;

/** A runtime dependency manager for Bukkit plugins. Intended for internal use. */
public final class BukkitLibraryManager extends LibraryManager {

  private final URLClassLoaderHelper classLoader;

  /** Creates a new Bukkit library manager. */
  public BukkitLibraryManager() {
    super(DemLoader.getPlugin().getDataFolder().toPath());
    classLoader =
        new URLClassLoaderHelper(
            (URLClassLoader) DemLoader.getPlugin().getClass().getClassLoader());
  }

  @Override
  protected void addToClasspath(Path file) {
    classLoader.addToClasspath(file);
  }
}
