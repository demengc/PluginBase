package dev.demeng.pluginbase.plugin;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.command.CommandManager;
import dev.demeng.pluginbase.libby.Library;
import dev.demeng.pluginbase.libby.managers.BukkitLibraryManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An extended version of JavaPlugin. Main class must extend this class in order to use PluginBase.
 */
public abstract class BasePlugin extends JavaPlugin {

  private BukkitLibraryManager libraryManager;
  private CommandManager commandManager;

  @Override
  public final void onLoad() {
    BaseLoader.setPlugin(this);
    libraryManager = new BukkitLibraryManager();
    commandManager = new CommandManager();
    load();
  }

  @Override
  public final void onEnable() {
    enable();
  }

  @Override
  public final void onDisable() {
    disable();
    commandManager.unregisterAll();
  }

  /**
   * Load a new library at runtime. Make sure to add the repository using the library manager before
   * using this method.
   *
   * @see BukkitLibraryManager#addMavenCentral()
   * @see BukkitLibraryManager#addRepository(String)
   * @param groupId The group ID of the dependency
   * @param artifactId The artifact ID of the dependency
   * @param version The version of the dependency
   * @param pattern The original packaging pattern, or null if you do not want to relocate
   * @param shadedPattern The new packaging pattern, or null if you do not want to relocate
   */
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

  /**
   * The library manager for the plugin.
   *
   * @return The library manager
   */
  public BukkitLibraryManager getLibraryManager() {
    return libraryManager;
  }

  /**
   * The command manager for the plugin.
   *
   * @return The command manager
   */
  public CommandManager getCommandManager() {
    return commandManager;
  }

  /**
   * The settings the base should use.
   *
   * @return The base settings
   */
  public abstract BaseSettings getBaseSettings();

  /** Code to perform at early plugin startup. */
  protected void load() {}

  /** Code to perform on plugin enable. */
  protected void enable() {}

  /** Code to perform on plugin disable. */
  protected void disable() {}
}
