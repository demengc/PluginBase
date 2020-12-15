package dev.demeng.pluginbase.plugin;

import dev.demeng.pluginbase.DemLoader;
import dev.demeng.pluginbase.libloader.FileRelocator;
import dev.demeng.pluginbase.libloader.PluginLib;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An extended version of JavaPlugin. Main class must extend this class in order to use PluginBase.
 */
public abstract class DemPlugin extends JavaPlugin {

  /** Code to perform at early plugin startup. */
  protected void load() {}

  /** Code to perform on plugin enable. */
  protected void enable() {}

  /** Code to perform on plugin disable. */
  protected void disable() {}

  @Override
  public final void onLoad() {
    DemLoader.setPlugin(this);
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

  static {
    FileRelocator.load(DemPlugin.class);
    PluginLib.loadLibs();
  }
}
