package dev.demeng.pluginbase.plugin;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.libby.Library;
import dev.demeng.pluginbase.libby.managers.BukkitLibraryManager;
import dev.demeng.pluginbase.command.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An extended version of JavaPlugin. Main class must extend this class in order to use PluginBase.
 */
public abstract class BasePlugin extends JavaPlugin {

  // TODO Add more JavaDoc comments.

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

  /**
   * The command manager for the plugin.
   *
   * @return The command manager
   */
  public CommandManager getCommandManager() {
    return commandManager;
  }

  public abstract BaseSettings getBaseSettings();

  /** Code to perform at early plugin startup. */
  protected void load() {}

  /** Code to perform on plugin enable. */
  protected void enable() {}

  /** Code to perform on plugin disable. */
  protected void disable() {}
}
