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

package dev.demeng.pluginbase;

import dev.demeng.pluginbase.plugin.BasePlugin;
import java.util.Objects;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Settings for the plugin base. Most of these methods should be overriden to suite your needs.
 * Apply the settings using {@link BasePlugin}'s #setBaseSettings().
 */
public interface BaseSettings {

  /**
   * The prefix for chat and console messages. Not localized. Colorized internally.
   *
   * @return The prefix
   */
  default String prefix() {
    return "&r";
  }

  /**
   * Whether the prefix should be included on each line of chat messages. If this is enabled, the
   * plugin will split strings containing "\n" into individual lines, add the prefix to each line,
   * and then return the string recombined with the "\n" delimiter.
   *
   * @return Whether the prefix should be included on each line
   */
  default boolean includePrefixOnEachLine() {
    return true;
  }

  /**
   * The color scheme of the plugin.
   *
   * @return The color scheme
   */
  default ColorScheme colorScheme() {
    return null;
  }

  // ---------------------------------------------------------------------------------
  // TIME FORMATS
  // ---------------------------------------------------------------------------------

  /**
   * The format for dates and times combined.
   *
   * @return The date and time format
   */
  default String dateTimeFormat() {
    return "MMMM dd yyyy HH:mm z";
  }

  /**
   * The format for dates.
   *
   * @return The date format
   */
  default String dateFormat() {
    return "MMMM dd yyyy";
  }

  /**
   * A 3-color color scheme for messages or other text.
   */
  @Data
  class ColorScheme {

    /**
     * The primarily color, used as &p.
     */
    @NotNull private final String primary;

    /**
     * The secondary color, used as &s.
     */
    @NotNull private final String secondary;

    /**
     * The tertiary color, used as &t.
     */
    @NotNull private final String tertiary;

    /**
     * Gets the color scheme from a configuration section.
     *
     * @param section The configuration section containing the color scheme
     * @return The color scheme from the configuration section
     */
    @NotNull
    public static ColorScheme fromConfig(@NotNull final ConfigurationSection section) {
      return new ColorScheme(
          Objects.requireNonNull(section.getString("primary"), "Primary color is null"),
          Objects.requireNonNull(section.getString("secondary"), "Secondary color is null"),
          Objects.requireNonNull(section.getString("tertiary"), "Tertiary color is null"));
    }
  }
}
