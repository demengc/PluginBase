/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.pluginbase.plugin;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.Schedulers;
import dev.demeng.pluginbase.ServerProperties;
import dev.demeng.pluginbase.Services;
import dev.demeng.pluginbase.command.BaseExceptionHandler;
import dev.demeng.pluginbase.command.BaseFailureHandler;
import dev.demeng.pluginbase.locale.Translator;
import dev.demeng.pluginbase.menu.MenuManager;
import dev.demeng.pluginbase.scheduler.BaseExecutors;
import dev.demeng.pluginbase.terminable.TerminableConsumer;
import dev.demeng.pluginbase.terminable.composite.CompositeTerminable;
import dev.demeng.pluginbase.terminable.module.TerminableModule;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

/**
 * An extended version of JavaPlugin. It is recommended that you extend this in your main class, but
 * you can also manually set the values in {@link BaseManager} should you wish to use your own
 * loading system.
 */
public abstract class BasePlugin extends JavaPlugin implements TerminableConsumer {

  /** The backing terminable registry for this plugin. */
  private CompositeTerminable terminableRegistry;

  @Override
  public final void onLoad() {
    BaseManager.setPlugin(this);
    this.terminableRegistry = CompositeTerminable.create();
    load();
  }

  @Override
  public final void onEnable() {

    // schedule cleanup of the registry
    Schedulers.builder()
        .async()
        .after(10, TimeUnit.SECONDS)
        .every(30, TimeUnit.SECONDS)
        .run(this.terminableRegistry::cleanup)
        .bindWith(this.terminableRegistry);

    BaseManager.setTranslator(Translator.create());
    BaseManager.setAdventure(BukkitAudiences.create(this));

    bindModule(new MenuManager());

    enable();
  }

  @Override
  public final void onDisable() {

    disable();

    if (getAdventure() != null) {
      getAdventure().close();
      BaseManager.setAdventure(null);
    }

    this.terminableRegistry.closeAndReportException();
    BaseExecutors.shutdown();

    ServerProperties.clearCache();
    BaseManager.setPlugin(null);
  }

  /** Executes at early plugin startup. */
  protected void load() {
    // Override if needed, otherwise nothing will be executed.
  }

  /** Executes on plugin enable. */
  protected void enable() {
    // Override if needed, otherwise nothing will be executed.
  }

  /** Executes on plugin disable. */
  protected void disable() {
    // Override if needed, otherwise nothing will be executed.
  }

  @NotNull
  @Override
  public <T extends AutoCloseable> T bind(@NotNull final T terminable) {
    return this.terminableRegistry.bind(terminable);
  }

  @NotNull
  @Override
  public <T extends TerminableModule> T bindModule(@NotNull final T module) {
    return this.terminableRegistry.bindModule(module);
  }

  /**
   * Register a listener with the server.
   *
   * <p>{@link dev.demeng.pluginbase.Events} should be used instead of this method in most cases.
   *
   * @param listener the listener to register
   * @param <T> the listener class type
   * @return the listener
   */
  @NotNull
  public <T extends Listener> T registerListener(@NotNull final T listener) {
    Objects.requireNonNull(listener, "listener");
    getServer().getPluginManager().registerEvents(listener, this);
    return listener;
  }

  /**
   * Gets a service provided by the ServiceManager.
   *
   * @param service The service class
   * @param <T> The class type
   * @return The service
   */
  @NotNull
  public <T> T getService(@NotNull final Class<T> service) {
    return Services.load(service);
  }

  /**
   * Provides a service to the ServiceManager, bound to this plugin.
   *
   * @param clazz The service class
   * @param instance The instance
   * @param priority The priority to register the service at
   * @param <T> The service class type
   * @return The instance
   */
  @NotNull
  public <T> T provideService(
      @NotNull final Class<T> clazz,
      @NotNull final T instance,
      @NotNull final ServicePriority priority) {
    return Services.provide(clazz, instance, this, priority);
  }

  /**
   * Provides a service to the ServiceManager, bound to this plugin at {@link
   * ServicePriority#Normal}.
   *
   * @param clazz The service class
   * @param instance The instance
   * @param <T> The service class type
   * @return The instance
   */
  @NotNull
  public <T> T provideService(@NotNull final Class<T> clazz, @NotNull final T instance) {
    return provideService(clazz, instance, ServicePriority.Normal);
  }

  /**
   * Gets if a given plugin is enabled.
   *
   * @param name The name of the plugin
   * @return If the plugin is enabled
   */
  public boolean isPluginPresent(@NotNull final String name) {
    return getServer().getPluginManager().getPlugin(name) != null;
  }

  /**
   * Gets the translator.
   *
   * @return The translator
   */
  public Translator getTranslator() {
    return BaseManager.getTranslator();
  }

  /**
   * Gets the BukkitAudiences instance to use for Adventure.
   *
   * @return The BukkitAudiences instance to use for Adventure
   */
  public BukkitAudiences getAdventure() {
    return BaseManager.getAdventure();
  }

  /**
   * Gets the settings the library should use.
   *
   * @return The settings the library should use
   */
  @NotNull
  public BaseSettings getBaseSettings() {
    return BaseManager.getBaseSettings();
  }

  /**
   * Sets the settings the library should use. Default will be used if not set.
   *
   * @param baseSettings The new base settings
   */
  public void setBaseSettings(@Nullable final BaseSettings baseSettings) {

    if (baseSettings == null) {
      BaseManager.setBaseSettings(new BaseSettings() {});
      return;
    }

    BaseManager.setBaseSettings(baseSettings);
  }

  /**
   * Creates a new Lamp command handler with PluginBase's settings pre-configured.
   *
   * @return A Lamp instance
   */
  @NotNull
  public Lamp<BukkitCommandActor> createCommandHandler() {
    return BukkitLamp.builder(this)
        .exceptionHandler(new BaseExceptionHandler())
        .dispatcherSettings(
            settings ->
                settings
                    .maximumFailedAttempts(10)
                    .failureHandler(BaseFailureHandler.baseFailureHandler()))
        .build();
  }
}
