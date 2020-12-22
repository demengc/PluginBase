package dev.demeng.pluginbase.plugin;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.libby.Library;
import dev.demeng.pluginbase.libby.managers.BukkitLibraryManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An extended version of JavaPlugin. Main class must extend this class in order to use PluginBase.
 */
public abstract class DemPlugin extends JavaPlugin {

  private BukkitLibraryManager libraryManager;

  @Override
  public final void onLoad() {
    DemLoader.setPlugin(this);
    libraryManager = new BukkitLibraryManager();
    load();
  }

  @Override
  public final void onEnable() {
    enable();
  }

  @Override
  public final void onDisable() {
    disable();
  }

  public void loadLib(
      String groupId, String artifactId, String version, String pattern, String shadedPattern) {

    if (groupId == null || artifactId == null || version == null) {
      throw new NullPointerException("Library group ID, artifact ID, or version is null");
    }

    final Library.Builder builder = Library.builder();

    builder.groupId(groupId).artifactId(artifactId).version(version);

    if (pattern != null && shadedPattern != null) {
      builder.relocate(pattern, shadedPattern);
    }

    libraryManager.loadLibrary(builder.build());
  }

  public BukkitLibraryManager getLibraryManager() {
    return libraryManager;
  }

  public abstract BaseSettings getBaseSettings();

  /** Code to perform at early plugin startup. */
  protected void load() {}

  /** Code to perform on plugin enable. */
  protected void enable() {}

  /** Code to perform on plugin disable. */
  protected void disable() {}
}
