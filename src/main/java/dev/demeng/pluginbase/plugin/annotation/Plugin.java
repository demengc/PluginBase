package dev.demeng.pluginbase.plugin.annotation;

import org.bukkit.plugin.PluginLoadOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to automatically generate the plugin.yml. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Plugin {

  /**
   * The name of the plugin.
   *
   * @return The name of the plugin
   */
  String name();

  /**
   * The version of the plugin.
   *
   * @return The version of the plugin
   */
  String version();

  /**
   * The API version of the plugin.
   *
   * @return The API version of the plugin
   */
  String apiVersion() default "1.13";

  /**
   * A brief description of the plugin.
   *
   * @return A brief description of the plugin
   */
  String description() default "";

  /**
   * The load order of the plugin.
   *
   * @return The load order of the plugin
   */
  PluginLoadOrder load() default PluginLoadOrder.POSTWORLD;

  /**
   * The authors of the plugin.
   *
   * @return The authors of the plugin.
   */
  String[] authors() default {"Demeng"};

  /**
   * The website of the plugin.
   *
   * @return The website of the plugin
   */
  String website() default "";

  /**
   * A list of names of the plugins this plugin depends on.
   *
   * @return A list of dependency names
   */
  String[] depends() default {};

  /**
   * A list of names of soft (optional) dependencies for the plugin.
   *
   * @return A list of soft dependency names
   */
  String[] softdepends() default {};

  /**
   * A list of names of plugins to load before this plugin.
   *
   * @return A list of names of plugins to load before this plugin
   */
  String[] loadbefore() default {};
}
