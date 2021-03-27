/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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
